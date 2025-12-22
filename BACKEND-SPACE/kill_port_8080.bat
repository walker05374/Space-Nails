@echo off
echo ==========================================
echo    ENCERRANDO PROCESSOS NA PORTA 8080
echo ==========================================
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do taskkill /f /pid %%a >nul 2>&1
echo Porta 8080 liberada (se estava em uso).
echo.
