# Script pour réinitialiser le mot de passe n8n

Write-Host "🔧 Réinitialisation du mot de passe n8n" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Option 1: Réinitialiser via Docker (si n8n est dans Docker)" -ForegroundColor Yellow
Write-Host "Commande: docker exec -it n8n n8n user-management:reset --email=votre-email@example.com --password=nouveau-mot-de-passe" -ForegroundColor White
Write-Host ""

Write-Host "Option 2: Réinitialiser via npm (si n8n est installé localement)" -ForegroundColor Yellow
Write-Host "Commande: n8n user-management:reset --email=votre-email@example.com --password=nouveau-mot-de-passe" -ForegroundColor White
Write-Host ""

Write-Host "Option 3: Supprimer la base de données n8n pour recommencer" -ForegroundColor Yellow
Write-Host "Emplacement typique: ~/.n8n/database.sqlite" -ForegroundColor White
Write-Host "Attention: Cela supprimera tous vos workflows!" -ForegroundColor Red
Write-Host ""

Write-Host "Option 4: Accéder directement à la base de données SQLite" -ForegroundColor Yellow
Write-Host "1. Installer SQLite Browser ou utiliser sqlite3" -ForegroundColor White
Write-Host "2. Ouvrir ~/.n8n/database.sqlite" -ForegroundColor White
Write-Host "3. Modifier la table 'user'" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Quelle option voulez-vous utiliser? (1/2/3/4)"

switch ($choice) {
    "1" {
        Write-Host "`nVérification des conteneurs Docker..." -ForegroundColor Cyan
        docker ps -a | Select-String "n8n"
        Write-Host ""
        $email = Read-Host "Entrez votre email n8n"
        $password = Read-Host "Entrez le nouveau mot de passe" -AsSecureString
        $passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))
        
        Write-Host "`nExécution de la commande..." -ForegroundColor Yellow
        docker exec -it n8n n8n user-management:reset --email=$email --password=$passwordPlain
    }
    "2" {
        $email = Read-Host "Entrez votre email n8n"
        $password = Read-Host "Entrez le nouveau mot de passe" -AsSecureString
        $passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))
        
        Write-Host "`nExécution de la commande..." -ForegroundColor Yellow
        n8n user-management:reset --email=$email --password=$passwordPlain
    }
    "3" {
        Write-Host "`n⚠️  ATTENTION: Cette action supprimera tous vos workflows!" -ForegroundColor Red
        $confirm = Read-Host "Êtes-vous sûr? (oui/non)"
        if ($confirm -eq "oui") {
            $n8nPath = "$env:USERPROFILE\.n8n\database.sqlite"
            if (Test-Path $n8nPath) {
                Remove-Item $n8nPath -Force
                Write-Host "✅ Base de données supprimée. Redémarrez n8n pour créer un nouveau compte." -ForegroundColor Green
            } else {
                Write-Host "❌ Base de données non trouvée à: $n8nPath" -ForegroundColor Red
            }
        }
    }
    "4" {
        $n8nPath = "$env:USERPROFILE\.n8n\database.sqlite"
        Write-Host "`nChemin de la base de données: $n8nPath" -ForegroundColor Cyan
        if (Test-Path $n8nPath) {
            Write-Host "✅ Base de données trouvée!" -ForegroundColor Green
            Write-Host "`nVous pouvez l'ouvrir avec:" -ForegroundColor Yellow
            Write-Host "- DB Browser for SQLite: https://sqlitebrowser.org/" -ForegroundColor White
            Write-Host "- Ou utiliser sqlite3 en ligne de commande" -ForegroundColor White
        } else {
            Write-Host "❌ Base de données non trouvée" -ForegroundColor Red
        }
    }
    default {
        Write-Host "Option invalide" -ForegroundColor Red
    }
}

Write-Host "`n📝 Note: Après réinitialisation, redémarrez n8n" -ForegroundColor Cyan
