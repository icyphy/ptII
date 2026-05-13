@echo off
REM Launch the Ptolemy II Auto-Modeling Agent backend on Windows.
REM Requires the PTII environment variable to point to the Ptolemy II
REM root (this repo). The agent classes live under %PTII%.

setlocal

if "%PTII%"=="" (
    REM Default to repo root assuming this script lives at $PTII\org\ptolemy\agent\bin
    pushd "%~dp0\..\..\..\.."
    set "PTII=%CD%"
    popd
)

set "PORT=%1"
if "%PORT%"=="" set "PORT=7777"

echo Using PTII=%PTII%
echo Starting backend on port %PORT% ...

java -classpath "%PTII%" org.ptolemy.agent.server.AgentServerMain %PORT%

endlocal
