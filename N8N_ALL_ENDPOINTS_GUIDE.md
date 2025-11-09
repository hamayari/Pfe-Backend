# 🚀 n8n - TOUS LES ENDPOINTS DE L'APPLICATION

## 📋 Vue d'Ensemble

**n8n gère maintenant TOUS les endpoints CRUD de l'application :**

✅ Conventions
✅ Factures (Invoices)
✅ Utilisateurs (Users)
✅ Structures
✅ Applications (Candidatures)
✅ Notifications
✅ Paiements (Payments)

---

## 🏗️ Architecture Globale

```
User Prompt
    ↓
Angular Frontend
    ↓
Spring Boot NLP
    ↓
n8n Workflows (HUB CENTRAL)
    ↓
Spring Boot CRUD APIs
    ↓
MongoDB
```

---

## 📊 Workflows n8n à Créer

### **1. CONVENTIONS**

#### **Workflow : Convention CRUD**

**Nodes :**
```
Webhook (/chatbot-crud)
    ↓
Switch (intent)
    ├─ CREATE → HTTP POST /api/conventions
    ├─ READ → HTTP GET /api/conventions
    ├─ UPDATE → HTTP PUT /api/conventions/{id}
    └─ DELETE → HTTP DELETE /api/conventions/{id}
    ↓
Respond to Webhook
```

**Exemples de prompts :**
```
"Crée une convention pour Alpha montant 5000"
"Montre toutes les conventions"
"Mets à jour la convention CONV-123 : statut active"
"Supprime la convention CONV-456"
```

---

### **2. FACTURES (INVOICES)**

#### **Workflow : Invoice CRUD**

**Nodes :**
```
Webhook (/chatbot-crud)
    ↓
Switch (intent)
    ├─ CREATE → HTTP POST /api/invoices
    ├─ READ → HTTP GET /api/invoices
    ├─ UPDATE → HTTP PUT /api/invoices/{id}
    └─ DELETE → HTTP DELETE /api/invoices/{id}
    ↓
Respond to Webhook
```

**Exemples de prompts :**
```
"Crée une facture de 2000 DT"
"Montre les factures non payées"
"Mets à jour la facture INV-123 : statut payée"
"Supprime la facture INV-456"
```

---

### **3. UTILISATEURS (USERS)**

#### **Workflow : User CRUD**

**Nodes :**
```
Webhook (/chatbot-crud)
    ↓
Switch (intent)
    ├─ CREATE → HTTP POST /api/users
    ├─ READ → HTTP GET /api/users
    ├─ UPDATE → HTTP PUT /api/users/{id}
    └─ DELETE → HTTP DELETE /api/users/{id}
    ↓
Respond to Webhook
```

**Exemples de prompts :**
```
"Crée un utilisateur commercial"
"Montre tous les utilisateurs"
"Mets à jour l'utilisateur USER-123 : rôle admin"
"Supprime l'utilisateur USER-456"
```

---

### **4. STRUCTURES**

#### **Workflow : Structure CRUD**

**Nodes :**
```
Webhook (/chatbot-crud)
    ↓
Switch (intent)
    ├─ CREATE → HTTP POST /api/structures
    ├─ READ → HTTP GET /api/structures
    ├─ UPDATE → HTTP PUT /api/structures/{id}
    └─ DELETE → HTTP DELETE /api/structures/{id}
    ↓
Respond to Webhook
```

**Exemples de prompts :**
```
"Crée une structure Alpha"
"Montre toutes les structures"
"Mets à jour la structure STR-123"
"Supprime la structure STR-456"
```

---

### **5. APPLICATIONS (CANDIDATURES)**

#### **Workflow : Application CRUD**

**Nodes :**
```
Webhook (/chatbot-crud)
    ↓
Switch (intent)
    ├─ CREATE → HTTP POST /api/applications
    ├─ READ → HTTP GET /api/applications
    ├─ UPDATE → HTTP PUT /api/applications/{id}
    └─ DELETE → HTTP DELETE /api/applications/{id}
    ↓
Respond to Webhook
```

**Exemples de prompts :**
```
"Crée une application pour la structure Beta"
"Montre toutes les candidatures"
"Mets à jour l'application APP-123 : statut approuvée"
"Supprime l'application APP-456"
```

---

### **6. NOTIFICATIONS**

#### **Workflow : Notification Management**

**Nodes :**
```
Webhook (/chatbot-crud)
    ↓
Switch (intent)
    ├─ SEND → HTTP POST /api/notifications
    └─ READ → HTTP GET /api/notifications
    ↓
Respond to Webhook
```

**Exemples de prompts :**
```
"Envoie une notification à l'utilisateur X"
"Montre toutes les notifications"
```

---

### **7. PAIEMENTS (PAYMENTS)**

#### **Workflow : Payment Management**

**Nodes :**
```
Webhook (/chatbot-crud)
    ↓
Switch (intent)
    ├─ CREATE → HTTP POST /api/payments
    ├─ READ → HTTP GET /api/payments
    └─ VALIDATE → HTTP PUT /api/payments/{id}/validate
    ↓
Respond to Webhook
```

**Exemples de prompts :**
```
"Crée un paiement de 3000 DT"
"Montre tous les paiements"
"Valide le paiement PAY-123"
```

---

## 🎯 Configuration n8n Universelle

### **Workflow Master : Chatbot CRUD Universal**

Ce workflow unique gère TOUTES les entités :

```
┌─────────────────┐
│  Webhook        │
│  /chatbot-crud  │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Function       │
│  Parse Request  │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Switch         │
│  Entity Type    │
└────────┬────────┘
         │
    ┌────┴────┬────────┬──────┬────────┬──────┬────────┐
    │         │        │      │        │      │        │
    ↓         ↓        ↓      ↓        ↓      ↓        ↓
CONVENTION INVOICE  USER  STRUCTURE APP  NOTIF  PAYMENT
    │         │        │      │        │      │        │
    └────┬────┴────────┴──────┴────────┴──────┴────────┘
         │
         ↓
┌─────────────────┐
│  Switch         │
│  Intent (CRUD)  │
└────────┬────────┘
         │
    ┌────┴────┬────────┬────────┐
    │         │        │        │
    ↓         ↓        ↓        ↓
 CREATE    READ    UPDATE   DELETE
    │         │        │        │
    └────┬────┴────────┴────────┘
         │
         ↓
┌─────────────────┐
│  HTTP Request   │
│  Spring Boot    │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Respond to     │
│  Webhook        │
└─────────────────┘
```

---

## 📝 Configuration Function Node

```javascript
// Parse Request
const intent = $input.item.json.intent;
const entityType = $input.item.json.entityType;
const entities = $input.item.json.entities;

// Déterminer l'endpoint
let baseUrl = 'http://localhost:8085/api';
let endpoint = '';

switch(entityType) {
  case 'CONVENTION':
    endpoint = baseUrl + '/conventions';
    break;
  case 'INVOICE':
    endpoint = baseUrl + '/invoices';
    break;
  case 'USER':
    endpoint = baseUrl + '/users';
    break;
  case 'STRUCTURE':
    endpoint = baseUrl + '/structures';
    break;
  case 'APPLICATION':
    endpoint = baseUrl + '/applications';
    break;
  case 'NOTIFICATION':
    endpoint = baseUrl + '/notifications';
    break;
  case 'PAYMENT':
    endpoint = baseUrl + '/payments';
    break;
  default:
    endpoint = baseUrl + '/unknown';
}

// Déterminer la méthode HTTP
let method = 'GET';
switch(intent) {
  case 'CREATE':
    method = 'POST';
    break;
  case 'READ':
    method = 'GET';
    break;
  case 'UPDATE':
    method = 'PUT';
    if (entities.id) {
      endpoint += '/' + entities.id;
    }
    break;
  case 'DELETE':
    method = 'DELETE';
    if (entities.id) {
      endpoint += '/' + entities.id;
    }
    break;
}

return {
  json: {
    endpoint: endpoint,
    method: method,
    data: entities,
    intent: intent,
    entityType: entityType
  }
};
```

---

## 🔧 Configuration HTTP Request Node

**Settings :**
- **Method** : `{{$json.method}}`
- **URL** : `{{$json.endpoint}}`
- **Headers** :
  ```json
  {
    "Content-Type": "application/json",
    "Authorization": "Bearer {{$json.token}}"
  }
  ```
- **Body** : `{{$json.data}}`

---

## 📊 Tableau Récapitulatif

| Entité | Endpoint Base | CREATE | READ | UPDATE | DELETE |
|--------|--------------|--------|------|--------|--------|
| **Convention** | `/api/conventions` | ✅ | ✅ | ✅ | ✅ |
| **Invoice** | `/api/invoices` | ✅ | ✅ | ✅ | ✅ |
| **User** | `/api/users` | ✅ | ✅ | ✅ | ✅ |
| **Structure** | `/api/structures` | ✅ | ✅ | ✅ | ✅ |
| **Application** | `/api/applications` | ✅ | ✅ | ✅ | ✅ |
| **Notification** | `/api/notifications` | ✅ | ✅ | ❌ | ❌ |
| **Payment** | `/api/payments` | ✅ | ✅ | ✅ (validate) | ❌ |

---

## ✅ Checklist de Configuration

- [ ] n8n installé et démarré
- [ ] Workflow Universal créé
- [ ] Webhook `/chatbot-crud` configuré
- [ ] Function Node configuré
- [ ] Switch Entity Type configuré
- [ ] Switch Intent configuré
- [ ] HTTP Request Node configuré
- [ ] Respond to Webhook configuré
- [ ] Tests pour chaque entité réussis

---

## 🎉 Résultat Final

**Avec cette configuration, votre chatbot peut gérer :**

✅ **7 types d'entités**
✅ **4 opérations CRUD** (CREATE, READ, UPDATE, DELETE)
✅ **28 actions différentes** (7 × 4)
✅ **Workflows visuels** dans n8n
✅ **Extensibilité illimitée**
✅ **Monitoring complet**

**Votre application est maintenant 100% pilotée par n8n !** 🚀
