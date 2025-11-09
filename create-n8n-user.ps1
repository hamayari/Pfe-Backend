# Script pour créer un nouvel utilisateur n8n

Write-Host "🔧 Création d'un nouvel utilisateur n8n" -ForegroundColor Cyan
Write-Host "======================================`n" -ForegroundColor Cyan

Write-Host "⚠️  ATTENTION: Cette commande va supprimer tous les utilisateurs existants" -ForegroundColor Yellow
Write-Host "et créer un nouveau compte administrateur.`n" -ForegroundColor Yellow

$confirm = Read-Host "Voulez-vous continuer? (oui/non)"

if ($confirm -ne "oui") {
    Write-Host "Opération annulée." -ForegroundColor Red
    exit
}

Write-Host "`nEntrez les informations pour le nouveau compte:" -ForegroundColor Cyan
$email = Read-Host "Email"
$firstName = Read-Host "Prénom"
$lastName = Read-Host "Nom"
$password = Read-Host "Mot de passe (min 8 caractères)"

Write-Host "`n🔄 Création du compte en cours...`n" -ForegroundColor Yellow

# Créer le nouvel utilisateur
n8n user-management:reset --email=$email --password=$password

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Compte créé avec succès!" -ForegroundColor Green
    Write-Host "`n📝 Informations de connexion:" -ForegroundColor Cyan
    Write-Host "   URL: http://localhost:5678" -ForegroundColor White
    Write-Host "   Email: $email" -ForegroundColor White
    Write-Host "   Mot de passe: $password" -ForegroundColor White
    Write-Host "`n🚀 Vous pouvez maintenant vous connecter!" -ForegroundColor Green
} else {
    Write-Host "`n❌ Erreur lors de la création du compte" -ForegroundColor Red
}

Write-Host "`nAppuyez sur une touche pour fermer..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
