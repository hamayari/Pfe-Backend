# 🤖 Configuration du Chatbot Décisionnel avec Gemini Flash 1.5

## 📋 Vue d'ensemble

Le chatbot décisionnel utilise **Google Gemini Flash 1.5** pour analyser les données de conventions et factures et fournir des insights au décideur.

## 🔑 Obtenir une clé API Gemini

### Étape 1 : Accéder à Google AI Studio
1. Allez sur : https://makersuite.google.com/app/apikey
2. Connectez-vous avec votre compte Google
3. Acceptez les conditions d'utilisation

### Étape 2 : Créer une clé API
1. Cliquez sur **"Create API Key"**
2. Sélectionnez un projet Google Cloud (ou créez-en un nouveau)
3. Copiez la clé API générée (format : `AIza...`)

### Étape 3 : Configurer l'application
Ouvrez le fichier `src/main/resources/application.properties` et remplacez :

```properties
gemini.api.key=VOTRE_CLE_API_GEMINI_ICI
```

Par votre vraie clé :

```properties
gemini.api.key=AIzaSyC_VotreCléAPIIci...
```

## 🚀 Démarrage du Chatbot

### 1. Redémarrer le backend
```bash
cd demo
mvn spring-boot:run
```

### 2. Tester l'endpoint
```bash
curl -X GET http://localhost:8085/api/decideur/health
```

Réponse attendue :
```json
{
  "status": "ok",
  "service": "Chatbot Décisionnel",
  "version": "1.0"
}
```

### 3. Tester une question
```bash
curl -X POST http://localhost:8085/api/decideur/ask \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT" \
  -d '{"question": "Montre-moi les factures en retard ce mois-ci"}'
```

## 📊 Exemples de questions

### Questions sur les factures
- "Montre-moi les factures en retard ce mois-ci"
- "Quel est le taux de factures payées depuis janvier ?"
- "Combien de factures sont en attente ?"
- "Quel est le montant total des factures non payées ?"

### Questions sur les conventions
- "Combien de conventions sont actives ?"
- "Montre-moi les conventions expirées"
- "Quelle est la valeur totale des conventions actives ?"

### Questions géographiques
- "Quelle région a le plus de conventions ?"
- "Répartition des montants par gouvernorat"

## 🎨 Format de réponse

Le chatbot retourne toujours un JSON structuré :

```json
{
  "texte": "15 factures en retard totalisant 45 000 DT. Les principales régions concernées sont Tunis (5), Sfax (3) et Nabeul (2).",
  "kpi": {
    "total_factures": 15,
    "montant_total": 45000,
    "taux_retard": 23.5
  },
  "graphique": {
    "type": "bar",
    "labels": ["Tunis", "Sfax", "Nabeul"],
    "values": [5, 3, 2]
  }
}
```

## 🔒 Sécurité

- ✅ Accessible uniquement aux utilisateurs avec le rôle **DECISION_MAKER** ou **DECIDEUR**
- ✅ Nécessite un token JWT valide
- ✅ Les données sont filtrées selon les permissions

## 🛠️ Dépannage

### Erreur : "Clé API Gemini non configurée"
➡️ Vérifiez que vous avez bien ajouté votre clé dans `application.properties`

### Erreur : "403 Forbidden"
➡️ Vérifiez que votre compte a le rôle DECISION_MAKER

### Erreur : "API quota exceeded"
➡️ Gemini Flash 1.5 a des limites gratuites. Attendez ou passez à un plan payant.

## 📈 Limites de l'API Gratuite

- **Requêtes par minute** : 60
- **Requêtes par jour** : 1500
- **Tokens par requête** : 32,000

## 🔗 Ressources

- Documentation Gemini : https://ai.google.dev/docs
- Google AI Studio : https://makersuite.google.com
- Tarification : https://ai.google.dev/pricing

## 💡 Conseils

1. **Testez d'abord avec des questions simples**
2. **Soyez précis dans vos questions** pour de meilleurs résultats
3. **Utilisez le contexte** : "ce mois-ci", "depuis janvier", etc.
4. **Demandez des graphiques** : "montre-moi un graphique de..."

## 🎯 Prochaines étapes

Une fois le chatbot configuré, vous pouvez :
1. Créer le composant Angular pour l'interface utilisateur
2. Intégrer les graphiques avec ngx-charts
3. Ajouter des questions prédéfinies
4. Personnaliser les prompts pour votre domaine
