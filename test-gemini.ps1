$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    contents = @(
        @{
            parts = @(
                @{
                    text = "Dis bonjour en français"
                }
            )
        }
    )
} | ConvertTo-Json -Depth 10

$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyDorFkpQFRXVft3jIn-2xO5xdli-XVkufQ"

try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $body
    Write-Host "✅ API Gemini fonctionne !" -ForegroundColor Green
    Write-Host "Réponse:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "❌ Erreur API Gemini:" -ForegroundColor Red
    Write-Host $_.Exception.Message
    Write-Host $_.ErrorDetails.Message
}
