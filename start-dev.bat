@echo off
setlocal
set ROOT=%~dp0

rem Start backend
start "backend" cmd /k "cd /d %ROOT%ecommerce-backend && mvn -DskipTests spring-boot:run"

rem Start frontend client
start "client" cmd /k "cd /d %ROOT%ecommerce-front\client && npm run dev"

echo Backend and client are starting in new terminals...
endlocal
