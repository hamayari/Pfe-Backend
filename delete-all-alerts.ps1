# Script PowerShell pour supprimer TOUTES les alertes de MongoDB

Write-Host "🗑️  Suppression de TOUTES les alertes KPI" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""

# Compter les alertes avant
Write-Host "📊 Comptage des alertes..." -ForegroundColor Yellow
try {
    $countBefore = & mongosh gestionpro --quiet --eval "db.kpiAlerts.countDocuments({})"
    Write-Host "   Alertes actuelles: $countBefore" -ForegroundColor White
} catch {
    Write-Host "❌ Erreur: MongoDB n'est pas accessible" -ForegroundColor Red
    Write-Host "   Assurez-vous que MongoDB est démarré" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Demander confirmation
Write-Host "⚠️  ATTENTION: Cette opération va supprimer TOUTES les $countBefore alertes!" -ForegroundColor Red
$confirmation = Read-Host "Êtes-vous sûr de vouloir continuer? (OUI pour confirmer)"

if ($confirmation -ne "OUI") {
    Write-Host "❌ Opération annulée" -ForegroundColor Yellow
    exit 0
}

Write-Host ""

# Supprimer toutes les alertes
Write-Host "🗑️  Suppression en cours..." -ForegroundColor Yellow
try {
    $result = & mongosh gestionpro --quiet --eval "db.kpiAlerts.deleteMany({})"
    Write-Host "✅ Suppression terminée" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors de la suppression: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Vérifier après suppression
Write-Host "📊 Vérification..." -ForegroundColor Yellow
$countAfter = & mongosh gestionpro --quiet --eval "db.kpiAlerts.countDocuments({})"
Write-Host "   Alertes restantes: $countAfter" -ForegroundColor White

Write-Host ""

if ($countAfter -eq 0) {
    Write-Host "✅ Toutes les alertes ont été supprimées avec succès!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🔄 Prochaines étapes:" -ForegroundColor Cyan
    Write-Host "   1. Redémarrez le backend: mvn spring-boot:run" -ForegroundColor White
    Write-Host "   2. Le système créera automatiquement 3 alertes (une par facture OVERDUE)" -ForegroundColor White
} else {
    Write-Host "⚠️  Il reste encore $countAfter alertes" -ForegroundColor Yellow
}

Write-Host ""
