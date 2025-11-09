#!/bin/bash

# ========================================
# Script pour exécuter les tests Backend
# ========================================

set -e  # Arrêter en cas d'erreur

echo "🧪 Exécution des tests Backend..."

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

# Vérifier que Maven est installé
if ! command -v mvn &> /dev/null; then
    log_error "Maven n'est pas installé!"
    exit 1
fi

log_info "Maven version:"
mvn --version

# Nettoyer les builds précédents
log_info "Nettoyage des builds précédents..."
mvn clean

# Compiler le projet
log_info "Compilation du projet..."
mvn compile

# Exécuter les tests unitaires
log_info "Exécution des tests unitaires..."
mvn test

# Vérifier le code de sortie
if [ $? -eq 0 ]; then
    log_info "✅ Tests unitaires réussis!"
else
    log_error "❌ Tests unitaires échoués!"
    exit 1
fi

# Exécuter les tests d'intégration
log_info "Exécution des tests d'intégration..."
mvn verify -Dtest=*Integration*

if [ $? -eq 0 ]; then
    log_info "✅ Tests d'intégration réussis!"
else
    log_warning "⚠️  Certains tests d'intégration ont échoué"
fi

# Générer le rapport de couverture JaCoCo
log_info "Génération du rapport de couverture..."
mvn jacoco:report

# Afficher le résumé
log_info "📊 Résumé des tests:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
find target/surefire-reports -name "*.xml" | wc -l | xargs echo "Fichiers de tests:"
echo "Rapport de couverture: target/site/jacoco/index.html"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

log_info "✅ Tests Backend terminés avec succès!"
