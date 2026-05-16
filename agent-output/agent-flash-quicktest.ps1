# Quick smoke: session + one streaming chat, flash-only backend must already run.
param(
    [int]$Port = 7777,
    [string]$Message = "build a simple Ramp connected to a Recorder",
    [string]$Mode = ""   # empty = auto route; "single" | "pipeline" | "mega"
)

$ErrorActionPreference = "Stop"
$base = "http://localhost:$Port/api/v1"

$session = (Invoke-RestMethod -Method Post -Uri "$base/sessions").id
Write-Host "session: $session"

$bodyObj = @{ message = $Message }
if ($Mode) { $bodyObj.mode = $Mode }
$body = $bodyObj | ConvertTo-Json -Compress

$url = "$base/sessions/$session/agent/chat/stream"
Write-Host "POST $url"
Write-Host "body: $body"
Write-Host ""

$req = [System.Net.HttpWebRequest]::Create($url)
$req.Method = "POST"
$req.ContentType = "application/json; charset=utf-8"
$req.Timeout = 600000
$req.ReadWriteTimeout = 600000
$bytes = [System.Text.Encoding]::UTF8.GetBytes($body)
$req.ContentLength = $bytes.Length
$rs = $req.GetRequestStream()
$rs.Write($bytes, 0, $bytes.Length)
$rs.Close()

$resp = $req.GetResponse()
$reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
$stepCount = 0
while ($null -ne ($line = $reader.ReadLine())) {
    if ([string]::IsNullOrWhiteSpace($line)) { continue }
    $stepCount++
    if ($stepCount -le 8 -or $line -match '"event":"done"') {
        Write-Host $line
    } elseif ($stepCount -eq 9) {
        Write-Host "... ($stepCount+ lines, showing milestones only) ..."
    }
}
$reader.Close()
$resp.Close()
Write-Host ""
Write-Host "Done. Total NDJSON lines: $stepCount"
