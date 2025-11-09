# Script de réinitialisation rapide du mot de passe n8n

Write-Host "🔧 Réinitialisation du mot de passe n8n" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Vérifier si n8n est installé
$n8nInstalled = Get-Command n8n -ErrorAction SilentlyContinue

if ($n8nInstalled) {
    Write-Host "✅ n8n est installé localement`n" -ForegroundColor Green
    
    Write-Host "Entrez les informations pour réinitialiser le mot de passe:" -ForegroundColor Yellow
    $email = Read-Host "Email de votre compte n8n"
    $newPassword = Read-Host "Nouveau mot de passe (min 8 caractères)"
    
    Write-Host "`n🔄 Réinitialisation en cours..." -ForegroundColor Cyan
    
    try {
        # Arrêter n8n s'il tourne
        Write-Host "Arrêt de n8n si actif..." -ForegroundColor Yellow
        Stop-Process -Name "node" -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
        
        # Réinitialiser le mot de passe
        $result = n8n user-management:reset --email=$email --password=$newPassword 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n✅ Mot de passe réinitialisé avec succès!" -ForegroundColor Green
            Write-Host "`n📝 Nouvelles informations de connexion:" -ForegroundColor Cyan
            Write-Host "   Email: $email" -ForegroundColor White
            Write-Host "   Mot de passe: $newPassword" -ForegroundColor White
            Write-Host "`n🚀 Redémarrez n8n avec: n8n start" -ForegroundColor Yellow
        } else {
            Write-Host "`n❌ Erreur lors de la réinitialisation" -ForegroundColor Red
            Write-Host $result -ForegroundColor Red
        }
    } catch {
        Write-Host "`n❌ Erreur: $_" -ForegroundColor Red
    }
    
} else {
    Write-Host "❌ n8n n'est pas installé localement`n" -ForegroundColor Red
    Write-Host "Vérification de Docker..." -ForegroundColor Yellow
    
    $dockerRunning = docker ps 2>&1 | Select-String "n8n"
    
    if ($dockerRunning) {
        Write-Host "✅ n8n trouvé dans Docker`n" -ForegroundColor Green
        
        $email = Read-Host "Email de votre compte n8n"
        $newPassword = Read-Host "Nouveau mot de passe (min 8 caractères)"
        
        Write-Host "`n🔄 Réinitialisation en cours..." -ForegroundColor Cyan
        
        try {
            docker exec -it n8n n8n user-management:reset --email=$email --password=$newPassword
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "`n✅ Mot de passe réinitialisé avec succès!" -ForegroundColor Green
                Write-Host "`n📝 Nouvelles informations de connexion:" -ForegroundColor Cyan
                Write-Host "   Email: $email" -ForegroundColor White
                Write-Host "   Mot de passe: $newPassword" -ForegroundColor White
                Write-Host "`n🔄 Redémarrage de n8n..." -ForegroundColor Yellow
                docker restart n8n
                Write-Host "✅ n8n redémarré!" -ForegroundColor Green
            }
        } catch {
            Write-Host "`n❌ Erreur: $_" -ForegroundColor Red
        }
        
    } else {
        Write-Host "❌ n8n n'est pas trouvé dans Docker non plus`n" -ForegroundColor Red
        Write-Host "Solutions alternatives:" -ForegroundColor Yellow
        Write-Host "1. Installer n8n: npm install -g n8n" -ForegroundColor White
        Write-Host "2. Ou utiliser Docker: docker run -it --rm n8n n8n user-management:reset" -ForegroundColor White
    }
}

Write-Host "`n📚 Pour plus d'aide, consultez: RECUPERATION_N8N.md" -ForegroundColor Cyan
