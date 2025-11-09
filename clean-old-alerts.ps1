# Script PowerShell pour nettoyer les anciennes alertes et régénérer les alertes consolidées

Write-Host "🧹 Nettoyage des Anciennes Alertes KPI" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier que MongoDB est accessible
Write-Host "📊 Vérification de MongoDB..." -ForegroundColor Yellow
try {
    $mongoTest = mongo --eval "db.version()" --quiet 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ MongoDB n'est pas accessible. Assurez-vous que MongoDB est démarré." -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ MongoDB est accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors de la vérification de MongoDB: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 2. Compter les alertes actuelles
Write-Host "📈 Comptage des alertes actuelles..." -ForegroundColor Yellow
$currentCount = mongo gestionpro --eval "db.kpiAlerts.count()" --quiet
Write-Host "   Nombre d'alertes actuelles: $currentCount" -ForegroundColor White

Write-Host ""

# 3. Demander confirmation
Write-Host "⚠️  ATTENTION: Cette opération va supprimer toutes les anciennes alertes non consolidées." -ForegroundColor Yellow
$confirmation = Read-Host "Voulez-vous continuer? (O/N)"

if ($confirmation -ne "O" -and $confirmation -ne "o") {
    Write-Host "❌ Opération annulée" -ForegroundColor Red
    exit 0
}

Write-Host ""

# 4. Supprimer les anciennes alertes
Write-Host "🗑️  Suppression des anciennes alertes..." -ForegroundColor Yellow
$deleteResult = mongo gestionpro --eval "db.kpiAlerts.deleteMany({ kpiName: { `$not: /^ALERTE_CONSOLIDEE_/ } })" --quiet
Write-Host "✅ Anciennes alertes supprimées" -ForegroundColor Green

Write-Host ""

# 5. Vérifier le nombre d'alertes restantes
Write-Host "📊 Vérification..." -ForegroundColor Yellow
$remainingCount = mongo gestionpro --eval "db.kpiAlerts.count()" --quiet
Write-Host "   Alertes restantes: $remainingCount" -ForegroundColor White

Write-Host ""

# 6. Régénérer les alertes consolidées
Write-Host "🔄 Régénération des alertes consolidées..." -ForegroundColor Yellow
Write-Host "   Appel de l'API d'analyse des KPI..." -ForegroundColor White

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8085/api/kpi/analyze" -Method Post -ErrorAction Stop
    Write-Host "✅ Alertes consolidées régénérées avec succès" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Erreur lors de l'appel API: $_" -ForegroundColor Yellow
    Write-Host "   Assurez-vous que le backend est démarré sur le port 8085" -ForegroundColor White
    Write-Host "   Vous pouvez régénérer manuellement avec: curl -X POST http://localhost:8085/api/kpi/analyze" -ForegroundColor White
}

Write-Host ""

# 7. Afficher le résumé
Write-Host "📊 Résumé de l'opération" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan
$newCount = mongo gestionpro --eval "db.kpiAlerts.count()" --quiet
Write-Host "   Alertes avant:  $currentCount" -ForegroundColor White
Write-Host "   Alertes après:  $newCount" -ForegroundColor White
$reduction = [math]::Round((($currentCount - $newCount) / $currentCount) * 100, 1)
Write-Host "   Réduction:      $reduction%" -ForegroundColor Green

Write-Host ""

# 8. Afficher les alertes consolidées
Write-Host "📋 Alertes consolidées créées:" -ForegroundColor Cyan
mongo gestionpro --eval "db.kpiAlerts.find({ kpiName: /^ALERTE_CONSOLIDEE_/ }, { kpiName: 1, dimension: 1, dimensionValue: 1, severity: 1, currentValue: 1, _id: 0 }).forEach(function(doc) { print(JSON.stringify(doc)); })" --quiet | ForEach-Object {
    $alert = $_ | ConvertFrom-Json
    $icon = if ($alert.severity -eq "HIGH") { "🔴" } elseif ($alert.severity -eq "MEDIUM") { "🟡" } else { "🟢" }
    Write-Host "   $icon $($alert.dimension): $($alert.dimensionValue) - $($alert.currentValue) KPI(s) problématique(s)" -ForegroundColor White
}

Write-Host ""
Write-Host "✅ Nettoyage terminé avec succès!" -ForegroundColor Green
Write-Host ""
