# Script pour nettoyer TOUTES les alertes et repartir à zéro
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🧹 NETTOYAGE COMPLET DES ALERTES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8085"

# 1. Supprimer TOUTES les alertes
Write-Host "`n🗑️  Suppression de toutes les alertes..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/kpi/alerts" -Method Delete
    Write-Host "✅ Alertes supprimées: $($response.deletedCount)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
}

# 2. Attendre 2 secondes
Start-Sleep -Seconds 2

# 3. Recréer les alertes proprement
Write-Host "`n🔄 Recréation des alertes pour les factures OVERDUE..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/kpi/analyze" -Method Post
    Write-Host "✅ Alertes créées: $($response.alertsCreated)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "✅ NETTOYAGE TERMINÉ" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
