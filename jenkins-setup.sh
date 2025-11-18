#!/bin/bash

echo "========================================="
echo "🚀 CONFIGURATION JENKINS CI/CD"
echo "========================================="

# Couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo ""
echo -e "${BLUE}📋 PRÉREQUIS À CONFIGURER DANS JENKINS${NC}"
echo "========================================="

echo ""
echo -e "${YELLOW}1. OUTILS (Manage Jenkins > Tools)${NC}"
echo "   - Maven: 'Maven-3.9'"
echo "   - JDK: 'JDK-17'"

echo ""
echo -e "${YELLOW}2. CREDENTIALS (Manage Jenkins > Credentials)${NC}"
echo "   - ID: 'github-credentials'"
echo "     Type: Username with password"
echo "     Username: votre-username-github"
echo "     Password: votre-token-github"
echo ""
echo "   - ID: 'sonarqube-token'"
echo "     Type: Secret text"
echo "     Secret: votre-token-sonarqube"
echo ""
echo "   - ID: 'dockerhub-credentials' (optionnel)"
echo "     Type: Username with password"
echo "     Username: votre-username-dockerhub"
echo "     Password: votre-password-dockerhub"

echo ""
echo -e "${YELLOW}3. SONARQUBE (Manage Jenkins > System)${NC}"
echo "   - Name: 'SonarQube'"
echo "   - Server URL: http://localhost:9000"
echo "   - Server authentication token: utiliser 'sonarqube-token'"

echo ""
echo -e "${YELLOW}4. CRÉER LE JOB JENKINS${NC}"
echo "   - New Item > Pipeline"
echo "   - Name: 'Commercial-Backend-CI'"
echo "   - Pipeline script from SCM"
echo "   - SCM: Git"
echo "   - Repository URL: votre-repo-github"
echo "   - Credentials: github-credentials"
echo "   - Branch: */main"
echo "   - Script Path: demo/Jenkinsfile.BACKEND-CI"

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}✅ CONFIGURATION TERMINÉE${NC}"
echo -e "${GREEN}=========================================${NC}"

echo ""
echo -e "${BLUE}📝 COMMANDES UTILES${NC}"
echo "   - Démarrer Jenkins: sudo systemctl start jenkins"
echo "   - Voir logs Jenkins: sudo journalctl -u jenkins -f"
echo "   - Démarrer SonarQube: docker-compose -f sonarqube-docker-compose.yml up -d"
echo "   - Voir images Docker: docker images"

echo ""
echo -e "${YELLOW}🔗 URLS${NC}"
echo "   - Jenkins: http://localhost:8080"
echo "   - SonarQube: http://localhost:9000"
echo "   - Backend: http://localhost:8081"

echo ""
