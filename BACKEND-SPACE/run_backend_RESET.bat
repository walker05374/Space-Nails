@echo off
echo ==========================================
echo    INICIANDO BACKEND (MODO RESET)
echo    ATENCAO: O BANCO SERA APAGADO E RECRIADO!
echo ==========================================
call kill_port_8080.bat
call mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.jpa.hibernate.ddl-auto=create"
pause
