#!/bin/bash
# Script pour installer Docker dans le conteneur Jenkins

echo "🐳 Installation de Docker dans Jenkins..."

# Trouver le nom du conteneur Jenkins
JENKINS_CONTAINER=$(docker ps --filter "ancestor=jenkins/jenkins" --format "{{.Names}}" | head -1)

if [ -z "$JENKINS_CONTAINER" ]; then
    echo "❌ Conteneur Jenkins non trouvé!"
    echo "Essayez de trouver manuellement avec: docker ps"
    exit 1
fi

echo "✅ Conteneur Jenkins trouvé: $JENKINS_CONTAINER"

# Installer Docker dans Jenkins
echo "📦 Installation de Docker CLI..."
docker exec -u root $JENKINS_CONTAINER bash -c "
    apt-get update && \
    apt-get install -y docker.io && \
    usermod -aG docker jenkins && \
    echo '✅ Docker installé avec succès!'
"

# Redémarrer Jenkins
echo "🔄 Redémarrage de Jenkins..."
docker restart $JENKINS_CONTAINER

echo "✅ Installation terminée!"
echo "⏳ Attendez 30 secondes que Jenkins redémarre..."
sleep 30

echo "🎉 Docker est maintenant disponible dans Jenkins!"
