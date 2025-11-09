# Script PowerShell pour nettoyer les alertes en double
# Exécuter: .\clean-alerts.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🧹 Nettoyage des alertes en double" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si MongoDB est en cours d'exécution
Write-Host "🔍 Vérification de MongoDB..." -ForegroundColor Yellow
$mongoProcess = Get-Process -Name "mongod" -ErrorAction SilentlyContinue

if ($null -eq $mongoProcess) {
    Write-Host "❌ MongoDB n'est pas en cours d'exécution!" -ForegroundColor Red
    Write-Host "   Démarrez MongoDB et réessayez." -ForegroundColor Red
    exit 1
}

Write-Host "✅ MongoDB est en cours d'exécution" -ForegroundColor Green
Write-Host ""

# Exécuter le script MongoDB
Write-Host "📊 Comptage des alertes avant nettoyage..." -ForegroundColor Yellow

$countBefore = & mongo demo_db --quiet --eval "db.kpiAlerts.countDocuments({ alertStatus: 'PENDING_DECISION' })"
Write-Host "   Alertes PENDING_DECISION: $countBefore" -ForegroundColor White

Write-Host ""
Write-Host "🗑️  Suppression des alertes en double..." -ForegroundColor Yellow

# Exécuter le script de nettoyage
& mongo demo_db clean-duplicate-alerts.js

Write-Host ""
Write-Host "📊 Comptage des alertes après nettoyage..." -ForegroundColor Yellow

$countAfter = & mongo demo_db --quiet --eval "db.kpiAlerts.countDocuments({ alertStatus: 'PENDING_DECISION' })"
Write-Host "   Alertes PENDING_DECISION: $countAfter" -ForegroundColor White

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Nettoyage terminé!" -ForegroundColor Green
Write-Host "   Avant: $countBefore alertes" -ForegroundColor White
Write-Host "   Après: $countAfter alertes" -ForegroundColor White
Write-Host "   Supprimées: $($countBefore - $countAfter) alertes" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Prochaines étapes:" -ForegroundColor Yellow
Write-Host "   1. Redémarrer l'application: mvn spring-boot:run" -ForegroundColor White
Write-Host "   2. Tester la création d'alertes" -ForegroundColor White
Write-Host "   3. Vérifier qu'il n'y a plus de doublons" -ForegroundColor White
Write-Host ""
