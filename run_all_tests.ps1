$ErrorActionPreference = "Stop"

Write-Host "STARTING TEST SUITE..."

# 1. BACKEND
Write-Host "Running Backend Tests..."
$backendDir = Join-Path $PSScriptRoot "BACKEND-SPACE"
Set-Location $backendDir

try {
    .\mvnw.cmd test -Dtest=AgendamentoIntegrationTest
    if ($LASTEXITCODE -ne 0) { throw "Backend Tests Failed!" }
    Write-Host "Backend Tests Passed!"
}
catch {
    Write-Host "Error running backend tests: $_"
    exit 1
}

# 2. FRONTEND
Write-Host "Running Frontend Tests (Cypress)..."
$frontendDir = Join-Path $PSScriptRoot "FRONEND-SPACE"
Set-Location $frontendDir

try {
    # Assume que o servidor já está rodando na porta 5173 (npm run dev)
    Write-Host "   Using active Frontend Server (Port 5173)..."

    Write-Host "   Executing Cypress..."
    # Usa npx.cmd
    cmd /c "npx.cmd cypress run --spec cypress/e2e/booking.cy.js"
    
    if ($LASTEXITCODE -ne 0) { throw "Cypress Tests Failed!" }
    Write-Host "Frontend Tests Passed!"
}
catch {
    Write-Host "Error running frontend tests: $_"
    exit 1
}

Write-Host "ALL TESTS COMPLETED SUCCESSFULLY!"
