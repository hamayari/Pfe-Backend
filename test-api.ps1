$apiKey = "AIzaSyCt96z6HULFne7HOT2YmIRKgp7XDIl0kDY"
# Test avec gemini-1.5-flash (modèle valide en v1beta)
$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

$body = '{"contents":[{"parts":[{"text":"Dis bonjour"}]}]}'

try {
    $response = Invoke-RestMethod -Uri $url -Method POST -Body $body -ContentType "application/json"
    Write-Host "SUCCESS - API Key is valid!" -ForegroundColor Green
    Write-Host "Response:" $response.candidates[0].content.parts[0].text
} catch {
    Write-Host "ERROR - API Key is NOT valid!" -ForegroundColor Red
    Write-Host "Error:" $_.Exception.Message
}
