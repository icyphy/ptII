# Start Ptolemy II Agent backend (single flash-tier model, no v4-pro,
# no iterative MEGA pipeline).
#
# Usage (PowerShell, from anywhere):
#   .\agent-output\start-agent-flash.ps1
#   .\agent-output\start-agent-flash.ps1 -Compile
#   .\agent-output\start-agent-flash.ps1 -CompileOnly
#   .\agent-output\start-agent-flash.ps1 -Dev          # backend + frontend
#   .\agent-output\start-agent-flash.ps1 -CheckOnly    # print env + exit
#
# Requires: JDK 17+, DEEPSEEK_API_KEY (User or Machine env) for LLM calls.
# Optional: setx DEEPSEEK_API_KEY "..."  then open a NEW terminal.

param(
    [int]$Port = 7777,
    [switch]$Compile,
    [switch]$CompileOnly,
    [switch]$Dev,
    [switch]$BackendOnly,
    [switch]$CheckOnly,
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$DeepSeekKey = $env:DEEPSEEK_API_KEY
)

$ErrorActionPreference = "Stop"

function Resolve-PtII {
    if ($env:PTII -and (Test-Path $env:PTII)) {
        return (Resolve-Path $env:PTII).Path
    }
    $root = if ($PSScriptRoot) { $PSScriptRoot } else { Split-Path -Parent $PSCommandPath }
    $candidate = (Resolve-Path (Join-Path $root "..")).Path
    if (Test-Path (Join-Path $candidate "org\ptolemy\agent\server\AgentServerMain.java")) {
        return $candidate
    }
    throw "Cannot find repo root. Set `$env:PTII to your ptII clone."
}

function Find-Java {
    if ($JavaHome -and (Test-Path (Join-Path $JavaHome "bin\java.exe"))) {
        return (Join-Path $JavaHome "bin\java.exe")
    }
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    throw "java.exe not found. Set JAVA_HOME or add Java to PATH."
}

function Set-AgentEnv {
    if (-not $env:DEEPSEEK_MODEL_FLASH) {
        $env:DEEPSEEK_MODEL_FLASH = "deepseek-v4-flash"
    }
    if (-not $env:DEEPSEEK_BASE_URL) {
        $env:DEEPSEEK_BASE_URL = "https://api.deepseek.com"
    }
    if (-not $env:AGENT_MAX_STEPS) { $env:AGENT_MAX_STEPS = "0" }
    if (-not $env:AGENT_MAX_TURN_MS) { $env:AGENT_MAX_TURN_MS = "600000" }
}

function Invoke-CompileAgent {
    param([string]$PtII, [string]$JavaExe)
    $javac = Join-Path (Split-Path $JavaExe -Parent) "javac.exe"
    if (-not (Test-Path $javac)) { throw "javac.exe not found next to $JavaExe" }
    Write-Host "Compiling org/ptolemy/agent ..." -ForegroundColor Cyan
    $sources = Get-ChildItem -Path (Join-Path $PtII "org\ptolemy\agent") -Recurse -Filter "*.java" |
        ForEach-Object { $_.FullName }
    if ($sources.Count -eq 0) {
        throw "No agent Java sources under $PtII\org\ptolemy\agent"
    }
    $cp = "$PtII;$PtII\lib\*"
    $argList = @(
        "-encoding", "UTF-8",
        "-cp", $cp,
        "-d", $PtII,
        "-Xlint:none"
    ) + $sources
    & $javac $argList
    if ($LASTEXITCODE -ne 0) {
        throw "javac failed with exit code $LASTEXITCODE"
    }
    Write-Host "Compile OK ($($sources.Count) files)." -ForegroundColor Green
}

function Wait-Health {
    param([int]$Port, [int]$Seconds = 60)
    $url = "http://localhost:$Port/api/v1/health"
    $deadline = (Get-Date).AddSeconds($Seconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $r = Invoke-RestMethod -Uri $url -TimeoutSec 3
            if ($r.ok) { return $true }
        } catch { Start-Sleep -Milliseconds 500 }
    }
    return $false
}

function Show-AgentStatus {
    param([int]$Port)
    $status = Invoke-RestMethod -Uri "http://localhost:$Port/api/v1/agent/status" -TimeoutSec 10
    $routing = $status.llmRouting
    Write-Host ""
    Write-Host "=== /api/v1/agent/status ===" -ForegroundColor Cyan
    Write-Host ("strategy:    " + $routing.strategy)
    Write-Host ("model:       " + $routing.chat)
    Write-Host ("baseUrl:     " + $routing.baseUrl)
    if ($routing.smokeProbes) {
        Write-Host ("smokeProbe:  " + $routing.smokeProbes.flash)
    }
    if ($routing.strategy -ne "single-flash" -and $routing.strategy -ne "single-model") {
        Write-Host "WARNING: expected strategy=single-flash" -ForegroundColor Red
    }
    $models = @(
        $routing.chat, $routing.single, $routing.planner,
        $routing.builder, $routing.refactor, $routing.reviewer
    ) | Select-Object -Unique
    $models = @($models)
    if ($models.Count -ne 1) {
        Write-Host "WARNING: roles use more than one model: $($models -join ', ')" -ForegroundColor Red
    } else {
        Write-Host "OK: all roles use $($models[0])" -ForegroundColor Green
    }
    return $status
}

function Start-BackendWindow {
    param(
        [Parameter(Mandatory)][string]$PtII,
        [int]$Port = 7777
    )
    $bat = Join-Path $env:TEMP "ptolemy-agent-$Port.bat"
    @"
@echo off
title ptolemy-agent (port $Port)
cd /d "$PtII"
if not defined DEEPSEEK_MODEL_FLASH set DEEPSEEK_MODEL_FLASH=deepseek-v4-flash
if not defined DEEPSEEK_BASE_URL set DEEPSEEK_BASE_URL=https://api.deepseek.com
echo PTII=$PtII
echo DEEPSEEK_MODEL_FLASH=%DEEPSEEK_MODEL_FLASH%
echo Starting AgentServerMain on port $Port ...
java -classpath "%PTII%" org.ptolemy.agent.server.AgentServerMain $Port
pause
"@ | Set-Content -Path $bat -Encoding ASCII
    Start-Process cmd.exe -ArgumentList "/k", $bat
}

function Start-FrontendWindow {
    param([Parameter(Mandatory)][string]$PtII)
    $frontend = Join-Path $PtII "frontend"
    if (-not (Test-Path (Join-Path $frontend "package.json"))) {
        Write-Host "No frontend/package.json - skipping UI." -ForegroundColor Yellow
        return
    }
    $bat = Join-Path $env:TEMP "ptolemy-agent-frontend.bat"
    @"
@echo off
title ptolemy-agent-frontend
cd /d "$frontend"
if not exist node_modules (
  echo npm install ...
  call npm install --no-audit --no-fund
)
npm run dev
pause
"@ | Set-Content -Path $bat -Encoding ASCII
    Start-Process cmd.exe -ArgumentList "/k", $bat
}

# -------- main --------
$PtII = Resolve-PtII
$env:PTII = $PtII
$Java = Find-Java

Set-AgentEnv

Write-Host "Ptolemy Agent [single flash model, no v4-pro, no MEGA]" -ForegroundColor Cyan
Write-Host "PTII:  $PtII"
Write-Host "Port:  $Port"
Write-Host ""
Write-Host "Env:"
Write-Host "  DEEPSEEK_MODEL_FLASH=$($env:DEEPSEEK_MODEL_FLASH)"
Write-Host "  DEEPSEEK_BASE_URL=$($env:DEEPSEEK_BASE_URL)"
Write-Host "  AGENT_MAX_STEPS=$($env:AGENT_MAX_STEPS)"
Write-Host "  AGENT_MAX_TURN_MS=$($env:AGENT_MAX_TURN_MS)"

if (-not $DeepSeekKey) {
    Write-Host ""
    Write-Host "DEEPSEEK_API_KEY is not set - LLM will be offline." -ForegroundColor Yellow
    Write-Host "  setx DEEPSEEK_API_KEY sk-...   (then restart terminal)"
}

if ($Compile -or $CompileOnly) {
    Invoke-CompileAgent -PtII $PtII -JavaExe $Java
}
if ($CheckOnly -or $CompileOnly) { exit 0 }

# Kill stale listener on port (best-effort).
$existing = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique
foreach ($listenerPid in $existing) {
    if ($listenerPid -and $listenerPid -ne 0) {
        Write-Host "Stopping previous listener PID $listenerPid on port $Port ..."
        Stop-Process -Id $listenerPid -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

if ($Dev -or $BackendOnly) {
    Write-Host ""
    Write-Host "Starting backend in new window ..." -ForegroundColor Cyan
    Start-BackendWindow -PtII $PtII -Port $Port
    if (-not (Wait-Health -Port $Port)) {
        throw "Backend did not respond on port $Port within 60s"
    }
    Show-AgentStatus -Port $Port | Out-Null
    if ($Dev -and -not $BackendOnly) {
        Start-FrontendWindow -PtII $PtII
        Write-Host ""
        Write-Host "Frontend: http://localhost:5173" -ForegroundColor Green
    }
    Write-Host "Backend:  http://localhost:$Port/api/v1/health" -ForegroundColor Green
    Write-Host "Close the backend console window to stop."
    exit 0
}

# Foreground backend (blocks this terminal)
Write-Host ""
Write-Host "Starting backend (foreground, Ctrl+C to stop) ..." -ForegroundColor Cyan
$cp = "$PtII;$PtII\lib\*"
& $Java -cp $cp org.ptolemy.agent.server.AgentServerMain $Port
