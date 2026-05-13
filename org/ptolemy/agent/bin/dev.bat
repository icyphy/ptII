@echo off
REM One-shot launcher for Windows: starts the agent backend and the
REM React dev server in two separate console windows. Close them
REM individually to stop.

setlocal

if "%PTII%"=="" (
    pushd "%~dp0\..\..\..\.."
    set "PTII=%CD%"
    popd
)
echo Using PTII=%PTII%

start "ptolemy-agent-backend" cmd /k "cd /d %PTII% && java -classpath %PTII% org.ptolemy.agent.server.AgentServerMain 7777"

if exist "%PTII%\frontend\package.json" (
    if not exist "%PTII%\frontend\node_modules" (
        echo Installing frontend deps...
        pushd "%PTII%\frontend"
        call npm install --no-audit --no-fund
        popd
    )
    start "ptolemy-agent-frontend" cmd /k "cd /d %PTII%\frontend && npm run dev"
)

echo Backend should be live at http://localhost:7777/api/v1/health
echo Frontend should be live at http://localhost:5173

endlocal
