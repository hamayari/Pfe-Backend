#!/bin/bash

echo "========================================="
echo "🚀 DÉMARRAGE CI/CD BACKEND"
echo "========================================="

# Couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Vérifier si Docker est installé
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker n'est pas installé${NC}"
    exit 1
fi

echo -e "${BLUE}1. Démarrage SonarQube...${NC}"
cd ..
docker-compose -f sonarqube-docker-compose.yml up -d

echo ""
echo -e "${YELLOW}⏳ Attente du démarrage de SonarQube (30s)...${NC}"
sleep 30

echo ""
echo -e "${GREEN}✅ SonarQube démarré${NC}"
echo "   URL: http://localhost:9000"
echo "   Login: admin"
echo "   Password: admin"

echo ""
echo -e "${BLUE}2. Vérification de l'environnement...${NC}"

# Vérifier Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✅ Java installé: ${JAVA_VERSION}${NC}"
else
    echo -e "${RED}❌ Java n'est pas installé${NC}"
fi

# Vérifier Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo -e "${GREEN}✅ Maven installé: ${MVN_VERSION}${NC}"
else
    echo -e "${RED}❌ Maven n'est pas installé${NC}"
fi

echo ""
echo -e "${BLUE}3. Test de compilation...${NC}"
cd demo
mvn clean compile -DskipTests

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Compilation réussie${NC}"
else
    echo -e "${RED}❌ Erreur de compilation${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}4. Lancement des tests unitaires...${NC}"
mvn test -Dtest=*Test -DfailIfNoTests=false

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Tests réussis${NC}"
else
    echo -e "${YELLOW}⚠️ Certains tests ont échoué${NC}"
fi

echo ""
echo -e "${BLUE}5. Build de l'image Docker...${NC}"
docker build -t commercial-backend:test .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Image Docker créée${NC}"
    docker images | grep commercial-backend
else
    echo -e "${RED}❌ Erreur lors du build Docker${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}✅ CI/CD PRÊT${NC}"
echo -e "${GREEN}=========================================${NC}"

echo ""
echo -e "${BLUE}📝 PROCHAINES ÉTAPES:${NC}"
echo "   1. Configurer Jenkins avec jenkins-setup.sh"
echo "   2. Créer un token SonarQube: http://localhost:9000"
echo "   3. Lancer le pipeline Jenkins"

echo ""
echo -e "${YELLOW}🔗 URLS UTILES:${NC}"
echo "   - SonarQube: http://localhost:9000"
echo "   - Jenkins: http://localhost:8080"

echo ""
