# Test authentication debug endpoint
Write-Host "Testing Authentication Debug Endpoint" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Get token from user
$token = Read-Host "Enter your JWT token (from browser localStorage or login response)"

if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Host "No token provided. Exiting." -ForegroundColor Red
    exit
}

# Test the debug endpoint
Write-Host "Testing /api/auth/debug/current-user..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-RestMethod -Uri "http://localhost:8085/api/auth/debug/current-user" -Method Get -Headers $headers
    
    Write-Host "`nAuthentication Details:" -ForegroundColor Green
    Write-Host "======================" -ForegroundColor Green
    Write-Host "Authenticated: $($response.authenticated)" -ForegroundColor White
    Write-Host "Username: $($response.username)" -ForegroundColor White
    Write-Host "Principal Type: $($response.principal)" -ForegroundColor White
    Write-Host "`nAuthorities/Roles:" -ForegroundColor Cyan
    $response.authorities | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor White
    }
    
    Write-Host "`n✅ Debug endpoint working!" -ForegroundColor Green
    
} catch {
    Write-Host "`n❌ Error calling debug endpoint:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Yellow
    }
}

Write-Host "`n`nTesting /api/structures..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-RestMethod -Uri "http://localhost:8085/api/structures" -Method Get -Headers $headers
    Write-Host "✅ Structures endpoint accessible! Found $($response.Count) structures" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error calling structures endpoint:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    }
}

Write-Host "`n`nTesting /api/zones-geographiques..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-RestMethod -Uri "http://localhost:8085/api/zones-geographiques" -Method Get -Headers $headers
    Write-Host "✅ Zones géographiques endpoint accessible! Found $($response.Count) zones" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error calling zones-geographiques endpoint:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    }
}
