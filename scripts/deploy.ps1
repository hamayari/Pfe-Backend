# ========================================
# Script de déploiement PowerShell
# ========================================

$ErrorActionPreference = "Stop"

# Couleurs
function Write-Info { Write-Host "[INFO] $args" -ForegroundColor Green }
function Write-Error-Custom { Write-Host "[ERROR] $args" -ForegroundColor Red }
function Write-Warning-Custom { Write-Host "[WARNING] $args" -ForegroundColor Yellow }
function Write-Step { Write-Host "[STEP] $args" -ForegroundColor Blue }

# Vérifier les prérequis
Write-Step "1/8 - Vérification des prérequis..."

if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error-Custom "Docker n'est pas installé!"
    exit 1
}

if (!(Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Error-Custom "Docker Compose n'est pas installé!"
    exit 1
}

Write-Info "✅ Docker et Docker Compose sont installés"

# Charger les variables d'environnement
Write-Step "2/8 - Chargement des variables d'environnement..."

if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^=]+)=(.*)$") {
            [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2])
        }
    }
    Write-Info "✅ Variables d'environnement chargées"
} else {
    Write-Warning-Custom "⚠️  Fichier .env non trouvé"
}

# Arrêter les conteneurs existants
Write-Step "3/8 - Arrêt des conteneurs existants..."
docker-compose down
Write-Info "✅ Conteneurs arrêtés"

# Build des images Docker
Write-Step "4/8 - Build des images Docker..."

Write-Info "Building Backend..."
docker-compose build backend

Write-Info "Building Frontend..."
docker-compose build frontend

Write-Info "✅ Images Docker créées"

# Démarrer MongoDB
Write-Step "5/8 - Démarrage de MongoDB..."
docker-compose up -d mongodb

Write-Info "Attente du démarrage de MongoDB..."
Start-Sleep -Seconds 10

# Vérifier MongoDB
$mongoReady = $false
$attempts = 0
while (-not $mongoReady -and $attempts -lt 30) {
    try {
        docker-compose exec -T mongodb mongosh --eval "db.adminCommand('ping')" 2>$null | Out-Null
        $mongoReady = $true
    } catch {
        Write-Info "En attente de MongoDB..."
        Start-Sleep -Seconds 5
        $attempts++
    }
}

if ($mongoReady) {
    Write-Info "✅ MongoDB est prêt"
} else {
    Write-Error-Custom "❌ MongoDB n'a pas démarré"
    exit 1
}

# Démarrer le Backend
Write-Step "6/8 - Démarrage du Backend..."
docker-compose up -d backend

Write-Info "Attente du démarrage du Backend..."
Start-Sleep -Seconds 20

# Health check Backend
$backendReady = $false
$attempts = 0
while (-not $backendReady -and $attempts -lt 30) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            $backendReady = $true
        }
    } catch {
        Write-Info "En attente du Backend..."
        Start-Sleep -Seconds 5
        $attempts++
    }
}

if ($backendReady) {
    Write-Info "✅ Backend est prêt"
} else {
    Write-Error-Custom "❌ Backend n'a pas démarré"
    exit 1
}

# Démarrer le Frontend
Write-Step "7/8 - Démarrage du Frontend..."
docker-compose up -d frontend

Write-Info "Attente du démarrage du Frontend..."
Start-Sleep -Seconds 10

# Health check Frontend
$frontendReady = $false
$attempts = 0
while (-not $frontendReady -and $attempts -lt 30) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:80" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            $frontendReady = $true
        }
    } catch {
        Write-Info "En attente du Frontend..."
        Start-Sleep -Seconds 5
        $attempts++
    }
}

if ($frontendReady) {
    Write-Info "✅ Frontend est prêt"
} else {
    Write-Error-Custom "❌ Frontend n'a pas démarré"
    exit 1
}

# Smoke tests
Write-Step "8/8 - Exécution des smoke tests..."

Write-Info "Test Backend API..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
    Write-Info "✅ Backend API répond"
} catch {
    Write-Error-Custom "❌ Backend API ne répond pas"
    exit 1
}

Write-Info "Test Frontend..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:80" -UseBasicParsing
    Write-Info "✅ Frontend répond"
} catch {
    Write-Error-Custom "❌ Frontend ne répond pas"
    exit 1
}

# Afficher le résumé
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host "✅ DÉPLOIEMENT RÉUSSI!" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Application accessible sur:"
Write-Host "   Frontend: http://localhost:80"
Write-Host "   Backend:  http://localhost:8080"
Write-Host "   MongoDB:  mongodb://localhost:27017"
Write-Host ""
Write-Host "📊 Commandes utiles:"
Write-Host "   Logs:     docker-compose logs -f"
Write-Host "   Status:   docker-compose ps"
Write-Host "   Stop:     docker-compose down"
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
