#!/bin/bash

# ========================================
# Script de déploiement complet
# ========================================

set -e  # Arrêter en cas d'erreur

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Vérifier les prérequis
log_step "1/8 - Vérification des prérequis..."

if ! command -v docker &> /dev/null; then
    log_error "Docker n'est pas installé!"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    log_error "Docker Compose n'est pas installé!"
    exit 1
fi

log_info "✅ Docker et Docker Compose sont installés"

# Charger les variables d'environnement
log_step "2/8 - Chargement des variables d'environnement..."

if [ -f ".env" ]; then
    source .env
    log_info "✅ Variables d'environnement chargées"
else
    log_warning "⚠️  Fichier .env non trouvé, utilisation des valeurs par défaut"
fi

# Arrêter les conteneurs existants
log_step "3/8 - Arrêt des conteneurs existants..."
docker-compose down
log_info "✅ Conteneurs arrêtés"

# Build des images Docker
log_step "4/8 - Build des images Docker..."

log_info "Building Backend..."
docker-compose build backend

log_info "Building Frontend..."
docker-compose build frontend

log_info "✅ Images Docker créées"

# Démarrer MongoDB
log_step "5/8 - Démarrage de MongoDB..."
docker-compose up -d mongodb

log_info "Attente du démarrage de MongoDB..."
sleep 10

# Vérifier que MongoDB est prêt
until docker-compose exec -T mongodb mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; do
    log_info "En attente de MongoDB..."
    sleep 5
done

log_info "✅ MongoDB est prêt"

# Démarrer le Backend
log_step "6/8 - Démarrage du Backend..."
docker-compose up -d backend

log_info "Attente du démarrage du Backend..."
sleep 20

# Health check Backend
until curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; do
    log_info "En attente du Backend..."
    sleep 5
done

log_info "✅ Backend est prêt"

# Démarrer le Frontend
log_step "7/8 - Démarrage du Frontend..."
docker-compose up -d frontend

log_info "Attente du démarrage du Frontend..."
sleep 10

# Health check Frontend
until curl -f http://localhost:80 > /dev/null 2>&1; do
    log_info "En attente du Frontend..."
    sleep 5
done

log_info "✅ Frontend est prêt"

# Smoke tests
log_step "8/8 - Exécution des smoke tests..."

log_info "Test Backend API..."
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    log_info "✅ Backend API répond"
else
    log_error "❌ Backend API ne répond pas"
    exit 1
fi

log_info "Test Frontend..."
if curl -f http://localhost:80 > /dev/null 2>&1; then
    log_info "✅ Frontend répond"
else
    log_error "❌ Frontend ne répond pas"
    exit 1
fi

log_info "Test MongoDB..."
if docker-compose exec -T mongodb mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
    log_info "✅ MongoDB répond"
else
    log_error "❌ MongoDB ne répond pas"
    exit 1
fi

# Afficher le résumé
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}✅ DÉPLOIEMENT RÉUSSI!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🌐 Application accessible sur:"
echo "   Frontend: http://localhost:80"
echo "   Backend:  http://localhost:8080"
echo "   MongoDB:  mongodb://localhost:27017"
echo ""
echo "📊 Commandes utiles:"
echo "   Logs:     docker-compose logs -f"
echo "   Status:   docker-compose ps"
echo "   Stop:     docker-compose down"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
