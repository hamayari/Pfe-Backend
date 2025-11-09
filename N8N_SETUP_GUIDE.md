# 🚀 Guide d'Installation et Configuration n8n

## 📋 Table des Matières
1. [Installation n8n](#installation)
2. [Démarrage n8n](#démarrage)
3. [Configuration des Workflows](#workflows)
4. [Intégration avec Spring Boot](#intégration)
5. [Tests](#tests)

---

## 1️⃣ Installation n8n

### **Méthode 1 : npm (Recommandée)**

Ouvrez PowerShell et exécutez :

```powershell
# Installer n8n globalement
npm install -g n8n

# Vérifier l'installation
n8n --version
```

### **Méthode 2 : Docker (Alternative)**

```powershell
docker run -it --rm --name n8n -p 5678:5678 n8nio/n8n
```

---

## 2️⃣ Démarrage n8n

### **Démarrer n8n**

```powershell
# Démarrer n8n
n8n

# Ou avec un dossier de données personnalisé
n8n start --tunnel
```

### **Accéder à l'interface**

Ouvrez votre navigateur : **http://localhost:5678**

**Première connexion :**
- Créez un compte (email + mot de passe)
- L'interface n8n s'ouvrira

---

## 3️⃣ Configuration des Workflows

### **Workflow 1 : CREATE Convention/Facture**

1. Cliquez sur **"New Workflow"**
2. Nommez-le : `Chatbot CRUD - CREATE`

**Étapes du workflow :**

```
┌─────────────┐
│  Webhook    │ → Reçoit le prompt du chatbot
└─────────────┘
       ↓
┌─────────────┐
│  Function   │ → Analyse les données
└─────────────┘
       ↓
┌─────────────┐
│  HTTP       │ → Appelle Spring Boot API
│  Request    │   POST /api/conventions ou /api/invoices
└─────────────┘
       ↓
┌─────────────┐
│  Respond    │ → Retourne la réponse au chatbot
│  to Webhook │
└─────────────┘
```

**Configuration du Webhook :**
- **Path** : `/chatbot-crud`
- **Method** : `POST`
- **Response Mode** : `When Last Node Finishes`

**Configuration HTTP Request :**
- **Method** : `POST`
- **URL** : `http://localhost:8085/api/conventions`
- **Headers** :
  ```json
  {
    "Content-Type": "application/json",
    "Authorization": "Bearer {{$json.token}}"
  }
  ```
- **Body** :
  ```json
  {
    "title": "{{$json.entities.structure}}",
    "amount": "{{$json.entities.amount}}",
    "status": "DRAFT"
  }
  ```

**Configuration Function Node :**
```javascript
// Extraire les données du prompt
const intent = $input.item.json.intent;
const entityType = $input.item.json.entityType;
const entities = $input.item.json.entities;

// Déterminer l'endpoint
let endpoint = '';
if (entityType === 'CONVENTION') {
  endpoint = 'http://localhost:8085/api/conventions';
} else if (entityType === 'INVOICE') {
  endpoint = 'http://localhost:8085/api/invoices';
}

// Préparer les données
return {
  json: {
    endpoint: endpoint,
    data: entities,
    intent: intent
  }
};
```

---

### **Workflow 2 : READ (Lister)**

```
Webhook → Function → HTTP GET → Respond
```

**HTTP Request :**
- **Method** : `GET`
- **URL** : `http://localhost:8085/api/conventions`

---

### **Workflow 3 : UPDATE**

```
Webhook → Function → HTTP PUT → Respond
```

**HTTP Request :**
- **Method** : `PUT`
- **URL** : `http://localhost:8085/api/invoices/{{$json.id}}`
- **Body** :
  ```json
  {
    "status": "{{$json.status}}"
  }
  ```

---

### **Workflow 4 : DELETE**

```
Webhook → Function → HTTP DELETE → Respond
```

**HTTP Request :**
- **Method** : `DELETE`
- **URL** : `http://localhost:8085/api/conventions/{{$json.id}}`

---

## 4️⃣ Intégration avec Spring Boot

### **Architecture**

```
User Prompt
    ↓
Angular (Frontend)
    ↓
Spring Boot (ChatbotNLPService)
    ↓
n8n (Webhook)
    ↓
n8n (Workflow Processing)
    ↓
Spring Boot (API CRUD)
    ↓
MongoDB
    ↓
Response → n8n → Spring Boot → Angular → User
```

### **Configuration Spring Boot**

Le fichier `application.properties` contient déjà :

```properties
n8n.webhook.url=http://localhost:5678/webhook
```

### **Service N8nService**

Le service `N8nService.java` est déjà créé et gère :
- ✅ Communication avec n8n
- ✅ Webhooks pour CREATE, READ, UPDATE, DELETE
- ✅ Fallback si n8n non disponible
- ✅ Vérification de disponibilité

---

## 5️⃣ Tests

### **Test 1 : Vérifier n8n**

```powershell
# Dans un navigateur
http://localhost:5678
```

### **Test 2 : Tester le Webhook**

```powershell
# Avec curl
curl -X POST http://localhost:5678/webhook/chatbot-crud `
  -H "Content-Type: application/json" `
  -d '{
    "prompt": "Créer une convention",
    "intent": "CREATE",
    "entityType": "CONVENTION",
    "entities": {
      "structure": "Alpha",
      "amount": 5000
    }
  }'
```

### **Test 3 : Via le Chatbot**

1. Démarrer Spring Boot : `mvn spring-boot:run`
2. Démarrer n8n : `n8n`
3. Accéder au chatbot : `http://localhost:4200/decideur/operational-chatbot`
4. Taper : `"Crée une convention pour Alpha montant 5000"`

**Logs attendus :**
```
🧠 [NLP] Analyse: crée une convention pour alpha montant 5000
🔗 [NLP] Utilisation de n8n pour le traitement
🔗 [n8n] Envoi vers n8n: http://localhost:5678/webhook
✅ [n8n] Réponse reçue: {...}
```

---

## 📊 Workflows n8n Recommandés

### **Workflow Complet : Chatbot CRUD**

```json
{
  "name": "Chatbot CRUD",
  "nodes": [
    {
      "name": "Webhook",
      "type": "n8n-nodes-base.webhook",
      "position": [250, 300],
      "webhookId": "chatbot-crud",
      "parameters": {
        "path": "chatbot-crud",
        "method": "POST"
      }
    },
    {
      "name": "Switch Intent",
      "type": "n8n-nodes-base.switch",
      "position": [450, 300],
      "parameters": {
        "dataPropertyName": "intent",
        "rules": {
          "rules": [
            {"value": "CREATE"},
            {"value": "READ"},
            {"value": "UPDATE"},
            {"value": "DELETE"}
          ]
        }
      }
    },
    {
      "name": "HTTP Create",
      "type": "n8n-nodes-base.httpRequest",
      "position": [650, 200],
      "parameters": {
        "method": "POST",
        "url": "http://localhost:8085/api/conventions"
      }
    },
    {
      "name": "Respond",
      "type": "n8n-nodes-base.respondToWebhook",
      "position": [850, 300]
    }
  ]
}
```

---

## ✅ Checklist de Configuration

- [ ] n8n installé (`npm install -g n8n`)
- [ ] n8n démarré (`n8n`)
- [ ] Interface accessible (http://localhost:5678)
- [ ] Compte créé
- [ ] Workflow CREATE créé
- [ ] Workflow READ créé
- [ ] Workflow UPDATE créé
- [ ] Workflow DELETE créé
- [ ] Webhooks testés
- [ ] Spring Boot configuré
- [ ] Tests via chatbot réussis

---

## 🎉 Résultat Final

**Avec n8n, votre chatbot peut maintenant :**
- ✅ Exécuter des workflows visuels
- ✅ Intégrer des services tiers (Slack, Email, etc.)
- ✅ Automatiser des processus complexes
- ✅ Gérer des conditions et boucles
- ✅ Logger toutes les actions
- ✅ Fallback automatique si n8n indisponible

**Votre architecture est maintenant complète !** 🚀
