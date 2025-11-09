#!/bin/bash

# ========================================
# Script pour exécuter les tests Frontend
# ========================================

set -e  # Arrêter en cas d'erreur

echo "🧪 Exécution des tests Frontend..."

# Couleurs pour les messages
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Aller dans le dossier frontend
cd app-frontend-new

# Vérifier que Node.js est installé
if ! command -v node &> /dev/null; then
    log_error "Node.js n'est pas installé!"
    exit 1
fi

log_info "Node.js version:"
node --version
npm --version

# Installer les dépendances si nécessaire
if [ ! -d "node_modules" ]; then
    log_info "Installation des dépendances..."
    npm ci --legacy-peer-deps
fi

# Exécuter le linter
log_info "Exécution du linter..."
npm run lint || log_warning "⚠️  Linter a trouvé des problèmes"

# Exécuter les tests unitaires avec couverture
log_info "Exécution des tests unitaires..."
npm run test -- --watch=false --code-coverage --browsers=ChromeHeadless

# Vérifier le code de sortie
if [ $? -eq 0 ]; then
    log_info "✅ Tests unitaires réussis!"
else
    log_error "❌ Tests unitaires échoués!"
    exit 1
fi

# Afficher le résumé de la couverture
log_info "📊 Résumé de la couverture:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ -f "coverage/index.html" ]; then
    echo "Rapport de couverture: coverage/index.html"
    
    # Extraire les statistiques de couverture
    if command -v grep &> /dev/null; then
        echo ""
        grep -A 5 "Coverage summary" coverage/lcov-report/index.html 2>/dev/null || true
    fi
else
    log_warning "Rapport de couverture non trouvé"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

log_info "✅ Tests Frontend terminés avec succès!"

# Retourner au dossier racine
cd ..
