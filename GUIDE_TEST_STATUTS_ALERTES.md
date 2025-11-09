# ✅ SYSTÈME DE STATUTS D'ALERTES - GUIDE DE TEST

## 🎯 Implémentation Complète

### **Flux des Statuts**

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUX COMPLET                              │
└─────────────────────────────────────────────────────────────┘

1️⃣ DÉTECTION AUTOMATIQUE
   ↓
   Système détecte: Taux de retard = 18% > Seuil 10%
   ↓
2️⃣ CRÉATION DANS MONGODB
   ↓
   {
     "alertStatus": "PENDING_DECISION",
     "notificationSent": false,
     "kpiName": "TAUX_RETARD",
     "currentValue": 18.0,
     "message": "Taux de retard à 18%..."
   }
   ↓
3️⃣ VISIBLE PAR DÉCIDEUR
   ↓
   Dashboard Décideur → Section "Alertes en Attente"
   Badge: 🟠 EN ATTENTE
   Bouton: "📨 Envoyer au Chef de Projet"
   ↓
4️⃣ DÉCIDEUR ENVOIE L'ALERTE
   ↓
   Clic sur "Envoyer au Chef de Projet"
   ↓
5️⃣ MISE À JOUR STATUT
   ↓
   {
     "alertStatus": "SENT_TO_PM",
     "notificationSent": true,
     "notificationSentAt": "2025-10-23T06:15:00",
     "actionHistory": [
       {
         "actionType": "SENT_TO_PM",
         "performedBy": "decideur-id",
         "performedByName": "M. Ben Youssef",
         "previousStatus": "PENDING_DECISION",
         "newStatus": "SENT_TO_PM"
       }
     ]
   }
   ↓
6️⃣ DISPARAÎT DU DASHBOARD DÉCIDEUR
   ↓
   L'alerte n'est plus visible dans "Alertes en Attente"
   Mais reste dans l'historique
   ↓
7️⃣ APPARAÎT CHEZ LE CHEF DE PROJET
   ↓
   Dashboard Chef de Projet → Section "Alertes KPI Reçues"
   Badge: 🔵 SENT_TO_PM
   ↓
8️⃣ TRAITEMENT PAR LE CHEF DE PROJET
   ↓
   Prendre en charge → IN_PROGRESS
   Résoudre → RESOLVED
   ↓
9️⃣ ARCHIVAGE
   ↓
   Historique consultable par tous
```

---

## 🧪 TEST COMPLET - SCÉNARIO RÉEL

### **PARTIE 1: Vue Décideur**

#### **Étape 1: Démarrer et Se Connecter**

```bash
# Backend
mvn spring-boot:run

# Frontend
ng serve
```

```
URL: http://localhost:4200/auth/login-decision-maker
Username: decisionmaker
Password: dm123456
```

#### **Étape 2: Créer des Alertes**

```
http://localhost:8080/api/kpi-alerts/check-now
```

**Console Backend:**
```
========================================
🔍 [AUTO KPI] Vérification automatique des KPI
📊 Taux de retard calculé: 58.3% (7/12)
🚨 Anomalie détectée: TAUX_RETARD = 58.3
💾 Alerte sauvegardée dans MongoDB: 67890abc...
   alertStatus: PENDING_DECISION ✅
========================================
```

#### **Étape 3: Voir les Alertes en Attente**

- Scrollez vers le bas du dashboard
- Section: "🔔 Alertes KPI en Attente de Décision"

**Résultat attendu:**
```
┌─────────────────────────────────────────────────────┐
│ 🔔 Alertes KPI en Attente de Décision              │
├─────────────────────────────────────────────────────┤
│  [3 En attente]  [0 Envoyées]                       │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │ ⚠️ Taux de retard - Global      [HIGH] [🟠]  │  │
│  │ Valeur: 58.3% | Seuil: 10%                   │  │
│  │ Détecté: 23/10/2025 06:00                    │  │
│  │ Recommandation: Contacter clients...         │  │
│  │ [📨 Envoyer au Chef de Projet] [📜 Hist.]    │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

#### **Étape 4: Envoyer au Chef de Projet**

- Cliquez sur "📨 Envoyer au Chef de Projet"
- Confirmez dans le popup

**Résultat attendu:**

**1. Message de confirmation:**
```
✅ Alerte envoyée au Chef de Projet avec succès
```

**2. L'alerte disparaît de la liste:**
```
┌─────────────────────────────────────────────────────┐
│ 🔔 Alertes KPI en Attente de Décision              │
├─────────────────────────────────────────────────────┤
│  [2 En attente]  [1 Envoyée] ← Compteur mis à jour │
│                                                      │
│  ✅ L'alerte "Taux de retard" n'est plus visible   │
└─────────────────────────────────────────────────────┘
```

**3. Console Backend:**
```
📨 Alerte 67890abc... envoyée au Chef de Projet par M. Ben Youssef
✅ Statut changé: PENDING_DECISION → SENT_TO_PM
```

**4. MongoDB:**
```json
{
  "_id": "67890abc...",
  "alertStatus": "SENT_TO_PM",
  "notificationSent": true,
  "notificationSentAt": "2025-10-23T06:15:00",
  "actionHistory": [
    {
      "actionType": "CREATED",
      "performedBy": "system",
      "newStatus": "PENDING_DECISION"
    },
    {
      "actionType": "SENT_TO_PM",
      "performedBy": "decideur-id",
      "performedByName": "M. Ben Youssef",
      "previousStatus": "PENDING_DECISION",
      "newStatus": "SENT_TO_PM",
      "performedAt": "2025-10-23T06:15:00"
    }
  ]
}
```

#### **Étape 5: Vérifier l'Historique**

- Développez "📜 Historique (7 derniers jours)"

**Résultat attendu:**
```
┌─────────────────────────────────────────────────────────┐
│ KPI           | Zone   | Date       | État      | Par  │
├─────────────────────────────────────────────────────────┤
│ Taux de retard| Global | 23/10/2025 | 🔵 SENT   | -    │
└─────────────────────────────────────────────────────────┘
```

---

### **PARTIE 2: Vue Chef de Projet**

#### **Étape 1: Se Connecter**

```
URL: http://localhost:4200/auth/login-project-manager
Username: projectmanager
Password: pm123456
```

#### **Étape 2: Accéder aux Alertes KPI**

- Cliquez sur "🔔 Gestion Alertes KPI" dans le menu
- OU scrollez vers la section

**Résultat attendu:**
```
┌─────────────────────────────────────────────────────┐
│ 🔔 Alertes KPI Reçues                               │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │ 🔴 Taux de retard - Global      [HIGH] [🔵]  │  │
│  │ Valeur: 58.3% | Reçu: 23/10/2025 06:15      │  │
│  │ Envoyé par: M. Ben Youssef                   │  │
│  │ Recommandation: Contacter clients...         │  │
│  │ [👤 Prendre en charge] [✅ Résoudre]         │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

#### **Étape 3: Voir l'Historique Complet**

- Cliquez sur "📜 Historique"

**Résultat attendu:**
```
Timeline:
1. CREATED
   Par: System
   Le: 23/10/2025 06:00
   Alerte créée automatiquement
   → PENDING_DECISION

2. SENT_TO_PM
   Par: M. Ben Youssef
   Le: 23/10/2025 06:15
   Alerte envoyée au Chef de Projet
   PENDING_DECISION → SENT_TO_PM
```

#### **Étape 4: Traiter l'Alerte**

- Cliquez sur "👤 Prendre en charge"
- Puis "✅ Résoudre"
- Entrez: "Problème résolu après contact avec les clients"

**Résultat attendu:**
```
Timeline mise à jour:
1. CREATED → PENDING_DECISION
2. SENT_TO_PM
3. IN_PROGRESS
   Par: Jean Dupont
   Le: 23/10/2025 06:30
4. RESOLVED
   Par: Jean Dupont
   Le: 23/10/2025 10:00
   Commentaire: "Problème résolu..."
```

---

## 📊 Vérification MongoDB

### **Requête pour Voir les Alertes en Attente (Décideur)**

```javascript
db.kpi_alerts.find({ alertStatus: "PENDING_DECISION" })
```

### **Requête pour Voir les Alertes Envoyées (Chef de Projet)**

```javascript
db.kpi_alerts.find({ alertStatus: "SENT_TO_PM" })
```

### **Requête pour Voir l'Historique Complet**

```javascript
db.kpi_alerts.find({
  alertStatus: { $in: ["SENT_TO_PM", "IN_PROGRESS", "RESOLVED", "ARCHIVED"] }
}).sort({ detectedAt: -1 })
```

---

## ✅ Checklist de Vérification

### **Backend**
- [x] Méthode `sendToProjectManager()` créée
- [x] Endpoint `/send-to-pm` ajouté
- [x] Statut `PENDING_DECISION` par défaut
- [x] Changement vers `SENT_TO_PM` fonctionnel
- [x] Historique enregistré

### **Frontend**
- [x] Méthode `sendToProjectManager()` dans le service
- [x] Bouton "Envoyer au Chef de Projet" visible (Décideur)
- [x] Confirmation avant envoi
- [x] Alerte disparaît après envoi
- [x] Compteurs mis à jour
- [x] Historique visible

### **Flux Complet**
- [x] Création avec `PENDING_DECISION`
- [x] Visible uniquement par Décideur
- [x] Envoi change le statut
- [x] Disparaît du dashboard Décideur
- [x] Apparaît chez Chef de Projet
- [x] Traçabilité complète
- [x] Historique consultable

---

## 🎯 RÉSUMÉ

✅ **Statut PENDING_DECISION** - Alertes en attente de décision  
✅ **Bouton "Envoyer au Chef de Projet"** - Fonctionnel  
✅ **Changement automatique** vers SENT_TO_PM  
✅ **Disparition du dashboard Décideur** - Après envoi  
✅ **Apparition chez Chef de Projet** - Automatique  
✅ **Traçabilité complète** - Historique de toutes les actions  
✅ **Collections MongoDB** - Séparation logique possible  

**Le système de statuts est maintenant complètement implémenté et fonctionnel!** 🚀
