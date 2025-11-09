# Script d'automatisation complète - Build, Docker, Test
# =========================================================

Write-Host "🚀 DÉMARRAGE DU PROCESSUS AUTOMATIQUE" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# Étape 1 : Arrêter tout
Write-Host "🛑 Étape 1/6 : Arrêt des services existants..." -ForegroundColor Yellow
docker-compose down 2>$null
Write-Host "✅ Services arrêtés" -ForegroundColor Green
Write-Host ""

# Étape 2 : Build Maven
Write-Host "🔨 Étape 2/6 : Build Maven (2-3 minutes)..." -ForegroundColor Yellow
$buildStart = Get-Date

mvn clean package -DskipTests -q

if ($LASTEXITCODE -eq 0) {
    $buildTime = ((Get-Date) - $buildStart).TotalSeconds
    Write-Host "✅ Build Maven réussi en $([math]::Round($buildTime, 0)) secondes" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur lors du build Maven" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Étape 3 : Vérifier le JAR
Write-Host "📦 Étape 3/6 : Vérification du JAR..." -ForegroundColor Yellow
if (Test-Path "target\demo-0.0.1-SNAPSHOT.jar") {
    $jarSize = (Get-Item "target\demo-0.0.1-SNAPSHOT.jar").Length / 1MB
    Write-Host "✅ JAR trouvé : $([math]::Round($jarSize, 2)) MB" -ForegroundColor Green
} else {
    Write-Host "❌ JAR non trouvé" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Étape 4 : Build Docker
Write-Host "🐳 Étape 4/6 : Build image Docker..." -ForegroundColor Yellow
$dockerStart = Get-Date

docker-compose build backend 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    $dockerTime = ((Get-Date) - $dockerStart).TotalSeconds
    Write-Host "✅ Image Docker créée en $([math]::Round($dockerTime, 0)) secondes" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur lors du build Docker" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Étape 5 : Démarrer les services
Write-Host "🚀 Étape 5/6 : Démarrage des services..." -ForegroundColor Yellow
docker-compose up -d

Start-Sleep 5

# Vérifier le statut
$services = docker-compose ps --format json | ConvertFrom-Json
Write-Host "✅ Services démarrés :" -ForegroundColor Green
foreach ($service in $services) {
    Write-Host "   - $($service.Service) : $($service.State)" -ForegroundColor Cyan
}
Write-Host ""

# Étape 6 : Tests de santé
Write-Host "🧪 Étape 6/6 : Tests de santé (attente 60 secondes)..." -ForegroundColor Yellow
Write-Host "   Attente du démarrage complet..." -ForegroundColor Gray

for ($i = 1; $i -le 12; $i++) {
    Start-Sleep 5
    Write-Host "   ⏱️  $($i * 5) secondes..." -ForegroundColor Gray
    
    # Test MongoDB
    $mongoStatus = docker-compose ps mongodb --format json | ConvertFrom-Json
    if ($mongoStatus.Health -eq "healthy") {
        Write-Host "   ✅ MongoDB : Healthy" -ForegroundColor Green
    }
    
    # Test Backend
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "   ✅ Backend : Healthy" -ForegroundColor Green
            break
        }
    } catch {
        # Continue à attendre
    }
}

Write-Host ""
Write-Host "🎯 TESTS FINAUX" -ForegroundColor Cyan
Write-Host "===============" -ForegroundColor Cyan
Write-Host ""

# Test 1 : MongoDB
Write-Host "Test 1 : MongoDB..." -ForegroundColor Yellow
try {
    $mongoTest = docker-compose exec -T mongodb mongosh --eval "db.adminCommand('ping')" 2>$null
    if ($mongoTest -match "ok.*1") {
        Write-Host "✅ MongoDB fonctionne" -ForegroundColor Green
    } else {
        Write-Host "⚠️  MongoDB : Réponse inattendue" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ MongoDB : Erreur" -ForegroundColor Red
}

# Test 2 : Backend API
Write-Host "Test 2 : Backend API..." -ForegroundColor Yellow
try {
    $backendTest = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    if ($backendTest.StatusCode -eq 200) {
        Write-Host "✅ Backend API fonctionne" -ForegroundColor Green
        $content = $backendTest.Content | ConvertFrom-Json
        Write-Host "   Status: $($content.status)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ Backend API : Non accessible" -ForegroundColor Red
    Write-Host "   Vérification des logs..." -ForegroundColor Yellow
    docker-compose logs --tail=20 backend
}

# Test 3 : Frontend
Write-Host "Test 3 : Frontend..." -ForegroundColor Yellow
try {
    $frontendTest = Invoke-WebRequest -Uri "http://localhost:80" -TimeoutSec 5
    if ($frontendTest.StatusCode -eq 200) {
        Write-Host "✅ Frontend fonctionne" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Frontend : Non accessible" -ForegroundColor Red
}

Write-Host ""
Write-Host "📊 RÉSUMÉ" -ForegroundColor Cyan
Write-Host "=========" -ForegroundColor Cyan
docker-compose ps

Write-Host ""
Write-Host "🔗 URLS D'ACCÈS" -ForegroundColor Cyan
Write-Host "===============" -ForegroundColor Cyan
Write-Host "Frontend : http://localhost:80" -ForegroundColor White
Write-Host "Backend  : http://localhost:8080" -ForegroundColor White
Write-Host "Health   : http://localhost:8080/actuator/health" -ForegroundColor White

Write-Host ""
Write-Host "📋 COMMANDES UTILES" -ForegroundColor Cyan
Write-Host "===================" -ForegroundColor Cyan
Write-Host "Voir les logs backend  : docker-compose logs -f backend" -ForegroundColor White
Write-Host "Voir les logs frontend : docker-compose logs -f frontend" -ForegroundColor White
Write-Host "Arrêter tout          : docker-compose down" -ForegroundColor White
Write-Host "Redémarrer            : docker-compose restart" -ForegroundColor White

Write-Host ""
Write-Host "✅ PROCESSUS AUTOMATIQUE TERMINÉ !" -ForegroundColor Green
