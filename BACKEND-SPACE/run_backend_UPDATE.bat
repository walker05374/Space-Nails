@echo off
echo ==========================================
echo    INICIANDO BACKEND (MODO UPDATE)
echo    Seus dados serao MANTIDOS.
echo ==========================================
call kill_port_8080.bat
call mvnw spring-boot:run
pause
