# Script pour déclencher manuellement l'analyse des alertes
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔍 DÉCLENCHEMENT MANUEL DE L'ANALYSE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8085"

Write-Host "`n📊 Appel de l'endpoint /api/kpi/analyze..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/kpi/analyze" -Method Post -ContentType "application/json"
    
    Write-Host "`n✅ SUCCÈS !" -ForegroundColor Green
    Write-Host "Message: $($response.message)" -ForegroundColor Green
    Write-Host "Alertes créées: $($response.alertsCreated)" -ForegroundColor Green
    
    if ($response.alertsCreated -gt 0) {
        Write-Host "`n📋 Détails des alertes:" -ForegroundColor Cyan
        foreach ($alert in $response.alerts) {
            Write-Host "  - $($alert.dimensionValue): $($alert.severity)" -ForegroundColor White
        }
    }
}
catch {
    Write-Host "`n❌ ERREUR !" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "✅ TERMINÉ" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
