# Script de test pour vérifier la clé API Gemini
# Usage: .\test-gemini-api.ps1

Write-Host "🧪 Test de la clé API Gemini..." -ForegroundColor Cyan
Write-Host ""

$apiKey = "AIzaSyDorFkpQFRXVft3jIn-2xO5xdli-XVkufQ"
$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

$body = @{
    contents = @(
        @{
            parts = @(
                @{
                    text = "Dis juste 'Bonjour, je fonctionne !'"
                }
            )
        }
    )
} | ConvertTo-Json -Depth 10

try {
    Write-Host "📡 Envoi de la requête à Gemini..." -ForegroundColor Yellow
    
    $response = Invoke-RestMethod -Uri $url -Method POST -Body $body -ContentType "application/json"
    
    Write-Host "✅ SUCCÈS ! La clé API est valide !" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 Réponse de Gemini :" -ForegroundColor Cyan
    Write-Host $response.candidates[0].content.parts[0].text -ForegroundColor White
    Write-Host ""
    Write-Host "✅ Votre chatbot devrait fonctionner correctement !" -ForegroundColor Green
    
} catch {
    Write-Host "❌ ERREUR ! La clé API n'est PAS valide !" -ForegroundColor Red
    Write-Host ""
    Write-Host "Détails de l'erreur :" -ForegroundColor Yellow
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "📋 Solutions :" -ForegroundColor Cyan
    Write-Host "1. Allez sur https://makersuite.google.com/app/apikey" -ForegroundColor White
    Write-Host "2. Créez une nouvelle clé API" -ForegroundColor White
    Write-Host "3. Remplacez la clé dans application-dev.properties" -ForegroundColor White
    Write-Host "4. Redémarrez le backend" -ForegroundColor White
}

Write-Host ""
Write-Host "Appuyez sur Entrée pour continuer..."
Read-Host
