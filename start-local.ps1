# ============================================================
# start-local.ps1 — Inicia Backend (H2) + Frontend com um comando
# Tunnels Cloudflare:
#   space.walkerteste.site    -> localhost:5858  (Frontend)
#   spaceapi.walkerteste.site -> localhost:5555  (Backend)
# ============================================================

$ROOT = $PSScriptRoot

Write-Host ""
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "   SPACE NAILS - Ambiente Local" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# --- BACKEND ---
Write-Host "[1/2] Iniciando Backend na porta 5555 (H2)..." -ForegroundColor Yellow
$backendJob = Start-Job -ScriptBlock {
    param($path)
    Set-Location $path
    & ".\mvnw.cmd" spring-boot:run "-Dspring-boot.run.profiles=local"
} -ArgumentList "$ROOT\BACKEND-SPACE"

# Aguarda o backend iniciar (30s)
Write-Host "      Aguardando backend subir..." -ForegroundColor DarkGray
Start-Sleep -Seconds 30

# --- FRONTEND ---
Write-Host "[2/2] Iniciando Frontend na porta 5858..." -ForegroundColor Yellow
$frontendJob = Start-Job -ScriptBlock {
    param($path)
    Set-Location $path
    npm run dev
} -ArgumentList "$ROOT\FRONEND-SPACE"

Start-Sleep -Seconds 5

Write-Host ""
Write-Host "=======================================" -ForegroundColor Green
Write-Host "   PRONTO! Servicos rodando:" -ForegroundColor Green
Write-Host ""
Write-Host "   Backend  -> http://localhost:5555" -ForegroundColor White
Write-Host "   H2 Console -> http://localhost:5555/h2-console" -ForegroundColor White
Write-Host "   Frontend -> http://localhost:5858" -ForegroundColor White
Write-Host ""
Write-Host "   Tunnels Cloudflare:" -ForegroundColor White
Write-Host "   API  -> https://spaceapi.walkerteste.site" -ForegroundColor White
Write-Host "   Site -> https://space.walkerteste.site" -ForegroundColor White
Write-Host "=======================================" -ForegroundColor Green
Write-Host ""
Write-Host "Pressione Ctrl+C para encerrar tudo." -ForegroundColor DarkGray

# Mostra logs enquanto os jobs rodam
try {
    while ($true) {
        Receive-Job -Job $backendJob  | ForEach-Object { Write-Host "[BACKEND]  $_" -ForegroundColor DarkCyan }
        Receive-Job -Job $frontendJob | ForEach-Object { Write-Host "[FRONTEND] $_" -ForegroundColor DarkMagenta }

        if ($backendJob.State -eq 'Failed') {
            Write-Host "[ERRO] Backend encerrou com falha!" -ForegroundColor Red
            Receive-Job -Job $backendJob | Write-Host
            break
        }

        Start-Sleep -Seconds 3
    }
}
finally {
    Write-Host ""
    Write-Host "Encerrando servicos..." -ForegroundColor Red
    Stop-Job  -Job $backendJob, $frontendJob
    Remove-Job -Job $backendJob, $frontendJob -Force
}
