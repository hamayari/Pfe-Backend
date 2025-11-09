# Script simple de réinitialisation n8n

Write-Host "🔧 Réinitialisation du mot de passe n8n" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Demander les informations
Write-Host "Entrez votre email n8n (celui que vous avez utilisé pour créer le compte):" -ForegroundColor Yellow
$email = Read-Host "Email"

Write-Host "`nEntrez votre nouveau mot de passe (minimum 8 caractères):" -ForegroundColor Yellow
$password = Read-Host "Nouveau mot de passe"

Write-Host "`n🔄 Réinitialisation en cours...`n" -ForegroundColor Cyan

# Exécuter la commande de réinitialisation
n8n user-management:reset --email=$email --password=$password

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ SUCCÈS ! Mot de passe réinitialisé" -ForegroundColor Green
    Write-Host "`n📝 Vos nouvelles informations de connexion:" -ForegroundColor Cyan
    Write-Host "   URL: http://localhost:5678" -ForegroundColor White
    Write-Host "   Email: $email" -ForegroundColor White
    Write-Host "   Mot de passe: $password" -ForegroundColor White
    Write-Host "`n🚀 Vous pouvez maintenant vous connecter à n8n" -ForegroundColor Green
} else {
    Write-Host "`n❌ Erreur lors de la réinitialisation" -ForegroundColor Red
    Write-Host "Vérifiez que l'email correspond à un compte existant" -ForegroundColor Yellow
}

Write-Host "`nAppuyez sur une touche pour fermer..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
