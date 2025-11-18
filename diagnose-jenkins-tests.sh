#!/bin/bash

echo "🔍 Diagnostic des tests Jenkins"
echo "================================"
echo ""

echo "📋 Configuration Java:"
java -version
echo ""

echo "📋 Configuration Maven:"
mvn -version
echo ""

echo "📋 Variables d'environnement:"
echo "JAVA_HOME: $JAVA_HOME"
echo "MAVEN_OPTS: $MAVEN_OPTS"
echo "USER: $USER"
echo "PWD: $PWD"
echo ""

echo "📋 Espace disque:"
df -h
echo ""

echo "📋 Mémoire disponible:"
free -h
echo ""

echo "🧪 Exécution des tests avec logs détaillés..."
mvn clean test \
    -Dspring.profiles.active=test \
    -Dsurefire.useFile=false \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -X \
    2>&1 | tee test-output.log

echo ""
echo "📊 Résumé des tests:"
grep -A 10 "Tests run:" test-output.log | tail -20

echo ""
echo "❌ Tests échoués:"
grep "FAILURE!" test-output.log || echo "Aucun test échoué trouvé"

echo ""
echo "✅ Diagnostic terminé. Voir test-output.log pour les détails complets"
