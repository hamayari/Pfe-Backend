# Script pour nettoyer le fichier project-manager-dashboard.component.ts
# Supprime tout le code après la ligne 3258 (fermeture de classe)

$filePath = "app-frontend-new/src/app/dashboard/project-manager-dashboard/project-manager-dashboard.component.ts"

Write-Host "🔧 Nettoyage du fichier project-manager-dashboard.component.ts..." -ForegroundColor Cyan

# Lire les 3258 premières lignes
$content = Get-Content $filePath -TotalCount 3258

# Écrire le contenu nettoyé
$content | Set-Content $filePath -Encoding UTF8

Write-Host "✅ Fichier nettoyé! Code dupliqué supprimé." -ForegroundColor Green
Write-Host "📊 Lignes conservées: 3258" -ForegroundColor Yellow
Write-Host ""
Write-Host "Vérifiez maintenant les erreurs TypeScript..." -ForegroundColor Cyan
