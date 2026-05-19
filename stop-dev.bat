@echo off
setlocal

call :killPort 8080
call :killPort 5173

echo Backend and client processes stopped (if they were running).
endlocal
exit /b 0

:killPort
set PORT=%~1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do (
  echo Stopping process on port %PORT% (PID %%a)
  taskkill /F /PID %%a >nul 2>&1
)
exit /b 0
