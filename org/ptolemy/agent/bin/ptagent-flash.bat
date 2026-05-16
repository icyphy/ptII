@echo off
REM Flash-only agent backend (no deepseek-v4-pro). Delegates to start-agent-flash.ps1.
setlocal
if "%PTII%"=="" (
    pushd "%~dp0\..\..\..\.."
    set "PTII=%CD%"
    popd
)
set "SCRIPT=%PTII%\agent-output\start-agent-flash.ps1"
if not exist "%SCRIPT%" (
    echo Missing %SCRIPT%
    exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" -Dev %*
endlocal
