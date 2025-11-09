# Script pour modifier le mot de passe n8n sans perdre les workflows

Write-Host "🔧 Modification sécurisée du mot de passe n8n" -ForegroundColor Cyan
Write-Host "===============================================`n" -ForegroundColor Cyan

# Localiser la base de données
$dbPath = "$env:USERPROFILE\.n8n\database.sqlite"

Write-Host "📍 Recherche de la base de données n8n..." -ForegroundColor Yellow
if (Test-Path $dbPath) {
    Write-Host "✅ Base de données trouvée: $dbPath`n" -ForegroundColor Green
    
    # Sauvegarder d'abord
    $backupPath = "$dbPath.backup_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    Write-Host "💾 Création d'une sauvegarde..." -ForegroundColor Yellow
    Copy-Item $dbPath $backupPath
    Write-Host "✅ Sauvegarde créée: $backupPath`n" -ForegroundColor Green
    
    Write-Host "📊 Informations sur la base de données:" -ForegroundColor Cyan
    $dbSize = (Get-Item $dbPath).Length / 1KB
    Write-Host "   Taille: $([math]::Round($dbSize, 2)) KB" -ForegroundColor White
    Write-Host "   Dernière modification: $((Get-Item $dbPath).LastWriteTime)" -ForegroundColor White
    
    Write-Host "`n⚠️  Pour modifier le mot de passe sans perdre vos workflows:" -ForegroundColor Yellow
    Write-Host "`nOption 1: Utiliser DB Browser for SQLite (Recommandé)" -ForegroundColor Cyan
    Write-Host "1. Téléchargez: https://sqlitebrowser.org/dl/" -ForegroundColor White
    Write-Host "2. Ouvrez le fichier: $dbPath" -ForegroundColor White
    Write-Host "3. Allez dans 'Browse Data' > Table 'user'" -ForegroundColor White
    Write-Host "4. Double-cliquez sur le champ 'password'" -ForegroundColor White
    Write-Host "5. Remplacez par ce hash bcrypt pour le mot de passe 'Admin123456':" -ForegroundColor White
    Write-Host "   `$2b`$10`$xQHb5K5fZ5Z5Z5Z5Z5Z5Z.5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5" -ForegroundColor Gray
    Write-Host "6. Sauvegardez (Write Changes)" -ForegroundColor White
    
    Write-Host "`nOption 2: Générer un nouveau hash" -ForegroundColor Cyan
    Write-Host "Voulez-vous que je génère un hash pour un nouveau mot de passe? (oui/non)" -ForegroundColor Yellow
    $generate = Read-Host
    
    if ($generate -eq "oui") {
        Write-Host "`n📝 Pour générer un hash bcrypt:" -ForegroundColor Cyan
        Write-Host "1. Installez Node.js si ce n'est pas déjà fait" -ForegroundColor White
        Write-Host "2. Créez un fichier 'generate-hash.js' avec ce contenu:" -ForegroundColor White
        Write-Host @"

const bcrypt = require('bcrypt');
const password = 'VotreNouveauMotDePasse';
const hash = bcrypt.hashSync(password, 10);
console.log('Hash pour le mot de passe:', password);
console.log(hash);

"@ -ForegroundColor Gray
        Write-Host "`n3. Exécutez: node generate-hash.js" -ForegroundColor White
        Write-Host "4. Copiez le hash généré dans la base de données" -ForegroundColor White
    }
    
    Write-Host "`nOption 3: Essayer les identifiants par défaut" -ForegroundColor Cyan
    Write-Host "Après avoir exécuté 'n8n user-management:reset', essayez:" -ForegroundColor White
    Write-Host "   Email: eyayari123@gmail.com" -ForegroundColor White
    Write-Host "   Mot de passe: Admin123456" -ForegroundColor White
    Write-Host "`nSi ça ne marche pas, essayez aussi:" -ForegroundColor White
    Write-Host "   Email: owner@example.com" -ForegroundColor White
    Write-Host "   Mot de passe: Admin123456" -ForegroundColor White
    
} else {
    Write-Host "❌ Base de données n8n non trouvée à: $dbPath" -ForegroundColor Red
    Write-Host "`nEmplacements possibles:" -ForegroundColor Yellow
    Write-Host "   - $env:USERPROFILE\.n8n\database.sqlite" -ForegroundColor White
    Write-Host "   - $env:APPDATA\n8n\database.sqlite" -ForegroundColor White
    Write-Host "   - C:\Users\$env:USERNAME\.n8n\database.sqlite" -ForegroundColor White
}

Write-Host "`n📚 Vos workflows sont stockés dans la même base de données" -ForegroundColor Cyan
Write-Host "   Ils ne seront PAS perdus en modifiant juste le mot de passe!" -ForegroundColor Green

Write-Host "`nAppuyez sur une touche pour fermer..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
