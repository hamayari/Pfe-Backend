# Script PowerShell pour FORCER la suppression de TOUTES les alertes

Write-Host "🗑️  SUPPRESSION FORCÉE DE TOUTES LES ALERTES" -ForegroundColor Red
Write-Host "============================================" -ForegroundColor Red
Write-Host ""

Write-Host "⚠️  Ce script va:" -ForegroundColor Yellow
Write-Host "   1. Arrêter le backend (si en cours)" -ForegroundColor White
Write-Host "   2. Supprimer TOUTES les alertes de MongoDB" -ForegroundColor White
Write-Host "   3. Vérifier la suppression" -ForegroundColor White
Write-Host ""

$confirmation = Read-Host "Voulez-vous continuer? (OUI pour confirmer)"

if ($confirmation -ne "OUI") {
    Write-Host "❌ Opération annulée" -ForegroundColor Yellow
    exit 0
}

Write-Host ""

# Étape 1: Compter les alertes
Write-Host "📊 Étape 1: Comptage des alertes..." -ForegroundColor Cyan
try {
    $count = & mongosh gestionpro --quiet --eval "db.kpi_alerts.countDocuments({})"
    Write-Host "   Alertes trouvées: $count" -ForegroundColor White
} catch {
    Write-Host "❌ Erreur: Impossible de se connecter à MongoDB" -ForegroundColor Red
    Write-Host "   Assurez-vous que MongoDB est démarré" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Étape 2: Supprimer toutes les alertes
Write-Host "🗑️  Étape 2: Suppression de toutes les alertes..." -ForegroundColor Cyan
try {
    & mongosh gestionpro --quiet --eval "db.kpi_alerts.deleteMany({})" | Out-Null
    Write-Host "   ✅ Commande de suppression exécutée" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors de la suppression" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Étape 3: Vérifier la suppression
Write-Host "📊 Étape 3: Vérification..." -ForegroundColor Cyan
$countAfter = & mongosh gestionpro --quiet --eval "db.kpi_alerts.countDocuments({})"
Write-Host "   Alertes restantes: $countAfter" -ForegroundColor White

Write-Host ""

if ($countAfter -eq "0") {
    Write-Host "✅ SUCCÈS: Toutes les alertes ont été supprimées!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Prochaines étapes:" -ForegroundColor Cyan
    Write-Host "   1. NE PAS redémarrer le backend tout de suite" -ForegroundColor Yellow
    Write-Host "   2. Vérifiez qu'il n'y a pas de scheduler automatique" -ForegroundColor Yellow
    Write-Host "   3. Quand vous redémarrez, surveillez les logs" -ForegroundColor Yellow
    Write-Host "   4. Utilisez trigger-kpi-analysis.ps1 pour créer les alertes manuellement" -ForegroundColor Yellow
} else {
    Write-Host "⚠️  ATTENTION: Il reste encore $countAfter alertes!" -ForegroundColor Red
    Write-Host "   Essayez de redémarrer MongoDB et réessayez" -ForegroundColor Yellow
}

Write-Host ""

# Étape 4: Afficher toutes les collections pour debug
Write-Host "📋 Collections dans la base de données:" -ForegroundColor Cyan
& mongosh gestionpro --quiet --eval "db.getCollectionNames().forEach(function(c) { print('   - ' + c + ': ' + db[c].countDocuments({}) + ' documents'); })"

Write-Host ""
