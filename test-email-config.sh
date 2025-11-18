#!/bin/bash
# Test de configuration email Brevo

echo "🧪 Test de configuration email Brevo"
echo "===================================="
echo ""

# Test 1: Vérifier la connexion SMTP
echo "📡 Test 1: Connexion SMTP Brevo"
nc -zv smtp-relay.brevo.com 587 2>&1 | grep -q "succeeded" && echo "✅ Connexion OK" || echo "❌ Connexion échouée"
echo ""

# Test 2: Tester l'endpoint forgot-password
echo "📧 Test 2: Endpoint forgot-password"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8085/api/auth/forgot-password?email=eyayari123@gmail.com")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

echo "Status: $HTTP_CODE"
echo "Response: $BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Requête réussie"
else
    echo "❌ Erreur HTTP $HTTP_CODE"
fi
echo ""

echo "💡 Vérifiez les logs du backend pour plus de détails"
echo "💡 Vérifiez aussi: https://app.brevo.com/log"
