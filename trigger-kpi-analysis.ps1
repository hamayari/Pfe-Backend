# Script PowerShell pour déclencher l'analyse des KPI et créer les alertes

Write-Host "🔍 Déclenchement de l'Analyse des KPI" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que le backend est accessible
Write-Host "📡 Vérification du backend..." -ForegroundColor Yellow
try {
    $testResponse = Invoke-RestMethod -Uri "http://localhost:8085/api/kpi/test" -Method Get -ErrorAction Stop
    Write-Host "✅ Backend accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend non accessible sur http://localhost:8085" -ForegroundColor Red
    Write-Host "   Assurez-vous que le backend est démarré avec: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Déclencher l'analyse
Write-Host "🔍 Lancement de l'analyse des KPI..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8085/api/kpi/analyze" -Method Post -ErrorAction Stop
    
    Write-Host "✅ Analyse terminée avec succès!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📊 Résultats:" -ForegroundColor Cyan
    Write-Host "   Alertes créées: $($response.alertsCreated)" -ForegroundColor White
    Write-Host "   Message: $($response.message)" -ForegroundColor White
    
    if ($response.alertsCreated -gt 0) {
        Write-Host ""
        Write-Host "📋 Alertes créées:" -ForegroundColor Cyan
        foreach ($alert in $response.alerts) {
            $icon = if ($alert.severity -eq "HIGH") { "🔴" } elseif ($alert.severity -eq "MEDIUM") { "🟡" } else { "🟢" }
            Write-Host "   $icon $($alert.dimensionValue) - Retard: $($alert.currentValue) jour(s)" -ForegroundColor White
        }
    }
    
} catch {
    Write-Host "❌ Erreur lors de l'analyse: $_" -ForegroundColor Red
    Write-Host "   Vérifiez les logs du backend pour plus de détails" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "✅ Opération terminée!" -ForegroundColor Green
Write-Host "   Rafraîchissez le frontend pour voir les nouvelles alertes" -ForegroundColor White
Write-Host ""
