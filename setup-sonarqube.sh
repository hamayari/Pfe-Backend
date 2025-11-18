#!/bin/bash
# Script pour configurer SonarQube avec Jenkins

echo "🔍 Configuration de SonarQube..."

# Vérifier si SonarQube existe déjà
if docker ps -a | grep -q sonarqube; then
    echo "⚠️  SonarQube existe déjà, suppression..."
    docker stop sonarqube 2>/dev/null
    docker rm sonarqube 2>/dev/null
fi

# Démarrer SonarQube
echo "🚀 Démarrage de SonarQube..."
docker run -d \
    --name sonarqube \
    -p 9000:9000 \
    -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
    sonarqube:latest

echo "⏳ Attente du démarrage de SonarQube (2-3 minutes)..."
echo "Vous pouvez suivre les logs avec: docker logs -f sonarqube"

# Attendre que SonarQube soit prêt
for i in {1..60}; do
    if curl -s http://localhost:9000/api/system/status | grep -q "UP"; then
        echo "✅ SonarQube est prêt!"
        break
    fi
    echo "⏳ Attente... ($i/60)"
    sleep 5
done

echo ""
echo "📋 Prochaines étapes:"
echo "1. Accédez à http://localhost:9000"
echo "2. Connectez-vous avec admin/admin"
echo "3. Changez le mot de passe"
echo "4. Allez dans My Account → Security → Generate Token"
echo "5. Créez un token nommé 'jenkins'"
echo "6. Copiez le token"
echo ""
echo "Dans Jenkins:"
echo "1. Manage Jenkins → Configure System"
echo "2. Section SonarQube servers"
echo "3. Server URL: http://172.17.0.1:9000"
echo "4. Server authentication token: Collez le token"
echo ""
echo "🎉 SonarQube est maintenant accessible!"
