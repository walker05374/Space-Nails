@echo off
TITLE Space Nails - Iniciador
color 0B

echo ===================================================
echo       INICIANDO SPACE NAILS (CLICK UNITARIO)
echo ===================================================
echo.
echo Aguarde, o Backend e o Frontend estao sendo abertos...
echo.

:: Executa o script PowerShell existente parando a politica de execucao
powershell.exe -ExecutionPolicy Bypass -WindowStyle Normal -File "%~dp0start-local.ps1"

pause
