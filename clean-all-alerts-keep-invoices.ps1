# Script PowerShell pour supprimer TOUTES les alertes sauf celles des factures OVERDUE

Write-Host "🧹 Nettoyage Complet des Alertes" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# 1. Compter les alertes actuelles
Write-Host "📊 Comptage des alertes actuelles..." -ForegroundColor Yellow
$currentCount = mongo gestionpro --eval "db.kpiAlerts.count()" --quiet
Write-Host "   Total alertes: $currentCount" -ForegroundColor White

$invoiceCount = mongo gestionpro --eval "db.kpiAlerts.count({ dimension: 'FACTURE' })" --quiet
Write-Host "   Alertes factures: $invoiceCount" -ForegroundColor White

$otherCount = $currentCount - $invoiceCount
Write-Host "   Autres alertes: $otherCount" -ForegroundColor White

Write-Host ""

# 2. Demander confirmation
Write-Host "⚠️  Cette opération va:" -ForegroundColor Yellow
Write-Host "   ✅ GARDER les $invoiceCount alertes de factures OVERDUE" -ForegroundColor Green
Write-Host "   ❌ SUPPRIMER les $otherCount autres alertes (KPI consolidés)" -ForegroundColor Red
Write-Host ""
$confirmation = Read-Host "Voulez-vous continuer? (O/N)"

if ($confirmation -ne "O" -and $confirmation -ne "o") {
    Write-Host "❌ Opération annulée" -ForegroundColor Red
    exit 0
}

Write-Host ""

# 3. Supprimer toutes les alertes SAUF les factures
Write-Host "🗑️  Suppression des alertes non-factures..." -ForegroundColor Yellow
mongo gestionpro --eval "db.kpiAlerts.deleteMany({ dimension: { `$ne: 'FACTURE' } })" --quiet
Write-Host "✅ Alertes non-factures supprimées" -ForegroundColor Green

Write-Host ""

# 4. Vérifier le résultat
Write-Host "📊 Vérification..." -ForegroundColor Yellow
$finalCount = mongo gestionpro --eval "db.kpiAlerts.count()" --quiet
Write-Host "   Alertes restantes: $finalCount" -ForegroundColor White

Write-Host ""

# 5. Afficher les alertes de factures restantes
Write-Host "📋 Alertes de factures OVERDUE:" -ForegroundColor Cyan
mongo gestionpro --eval "db.kpiAlerts.find({ dimension: 'FACTURE' }, { dimensionValue: 1, currentValue: 1, severity: 1, _id: 0 }).forEach(function(doc) { print(JSON.stringify(doc)); })" --quiet | ForEach-Object {
    try {
        $alert = $_ | ConvertFrom-Json
        $icon = if ($alert.severity -eq "HIGH") { "🔴" } elseif ($alert.severity -eq "MEDIUM") { "🟡" } else { "🟢" }
        Write-Host "   $icon Facture: $($alert.dimensionValue) - Retard: $($alert.currentValue) jour(s)" -ForegroundColor White
    } catch {
        # Ignorer les erreurs de parsing
    }
}

Write-Host ""
Write-Host "✅ Nettoyage terminé!" -ForegroundColor Green
Write-Host "   Vous avez maintenant uniquement les alertes de factures OVERDUE" -ForegroundColor White
Write-Host ""
