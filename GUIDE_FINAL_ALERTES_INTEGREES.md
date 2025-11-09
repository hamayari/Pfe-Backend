# ✅ SYSTÈME D'ALERTES KPI INTÉGRÉ - GUIDE COMPLET

## 🎯 Architecture Finale

### **Principe de Fonctionnement**

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUX DES ALERTES                          │
└─────────────────────────────────────────────────────────────┘

1️⃣ DÉTECTION AUTOMATIQUE
   ↓
   KPI dépasse le seuil
   ↓
2️⃣ CRÉATION ALERTE
   ↓
   Statut: PENDING_DECISION
   Visible par: Décideur uniquement
   ↓
3️⃣ DÉCISION DU DÉCIDEUR
   ↓
   Clic sur "Envoyer au Chef de Projet"
   ↓
4️⃣ NOTIFICATION
   ↓
   Statut: SENT_TO_PM
   Visible par: Chef de Projet
   Disparaît du dashboard Décideur
   ↓
5️⃣ TRAITEMENT
   ↓
   Chef de Projet: Prendre en charge → Résoudre
   Statut: IN_PROGRESS → RESOLVED
   ↓
6️⃣ ARCHIVAGE
   ↓
   Historique consultable par tous
```

---

## 📊 Statuts des Alertes

| Statut | Emoji | Description | Visible par |
|--------|-------|-------------|-------------|
| **PENDING_DECISION** | 🟠 | Alerte détectée, en attente de décision | Décideur uniquement |
| **SENT_TO_PM** | 🔵 | Envoyée au Chef de Projet | Chef de Projet |
| **IN_PROGRESS** | 🔵 | En cours de traitement | Chef de Projet |
| **RESOLVED** | 🟢 | Problème résolu | Historique |
| **ARCHIVED** | 📦 | Archivée | Historique |

---

## 🎨 Intégration dans les Dashboards

### **Dashboard Décideur**

**Section: "Alertes KPI en Attente de Décision"**

```
┌─────────────────────────────────────────────────────────┐
│ 🔔 Alertes KPI en Attente de Décision  [🔄 Actualiser] │
├─────────────────────────────────────────────────────────┤
│  [5 En attente]  [12 Envoyées au Chef de Projet]        │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ⚠️ Taux de retard - Sfax            [HIGH] [🟠]  │  │
│  │ Valeur: 18% | Seuil: 10%                         │  │
│  │ Recommandation: Contacter clients...             │  │
│  │ [📨 Envoyer au Chef de Projet] [📜 Historique]   │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  📜 Historique (7 derniers jours) [Développer ▼]        │
└─────────────────────────────────────────────────────────┘
```

**Accès:**
- Menu latéral: "Indicateurs Clés" → Section "Alertes KPI"
- OU scroll vers le bas du dashboard

### **Dashboard Chef de Projet**

**Section: "Alertes KPI Reçues"**

```
┌─────────────────────────────────────────────────────────┐
│ 🔔 Alertes KPI Reçues                  [🔄 Actualiser] │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ 🔴 Taux de retard - Sfax            [HIGH] [🔴]  │  │
│  │ Valeur: 18% | Reçu le: 23/10/2025 06:15         │  │
│  │ Recommandation: Contacter clients...             │  │
│  │ [👤 Prendre en charge] [✅ Résoudre] [📜 Hist.]  │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  📜 Historique (7 derniers jours) [Développer ▼]        │
└─────────────────────────────────────────────────────────┘
```

**Accès:**
- Menu latéral: "Gestion Alertes KPI"
- OU scroll vers la section

---

## 🚀 GUIDE DE TEST COMPLET

### **Étape 1: Démarrer les Services**

```bash
# Terminal 1 - Backend
cd c:/Users/eyaya/OneDrive/Desktop/commercial-pfe/demo
mvn spring-boot:run

# Terminal 2 - Frontend
cd c:/Users/eyaya/OneDrive/Desktop/commercial-pfe/demo/app-frontend-new
ng serve
```

---

### **TEST SCÉNARIO 1: Vue Décideur**

**Étape 1: Se connecter comme Décideur**
```
URL: http://localhost:4200/auth/login-decision-maker
Username: decisionmaker
Password: dm123456
```

**Étape 2: Créer des alertes**
```
http://localhost:8080/api/kpi-alerts/check-now
```

**Étape 3: Accéder aux alertes**
- Cliquez sur "Indicateurs Clés" dans le menu
- OU scrollez vers le bas du dashboard
- Vous verrez la section "Alertes KPI en Attente de Décision"

**Résultat attendu:**
- ✅ Statistiques: "5 En attente"
- ✅ Liste des alertes avec statut 🟠 EN ATTENTE
- ✅ Bouton "Envoyer au Chef de Projet" visible
- ✅ Détails: Valeur actuelle, Seuil, Recommandation

**Étape 4: Envoyer une alerte au Chef de Projet**
- Cliquez sur "📨 Envoyer au Chef de Projet"

**Résultat attendu:**
- ✅ Message: "📨 Notification envoyée au Chef de Projet"
- ✅ L'alerte disparaît de la liste "En attente"
- ✅ Compteur "Envoyées" s'incrémente
- ✅ L'alerte apparaît dans l'historique avec statut 🔵 SENT_TO_PM

---

### **TEST SCÉNARIO 2: Vue Chef de Projet**

**Étape 1: Se connecter comme Chef de Projet**
```
URL: http://localhost:4200/auth/login-project-manager
Username: projectmanager
Password: pm123456
```

**Étape 2: Accéder aux alertes KPI**
- Cliquez sur "Gestion Alertes KPI" dans le menu latéral
- OU scrollez vers la section

**Résultat attendu:**
- ✅ Section "Alertes KPI Reçues" visible
- ✅ Liste des alertes envoyées par le Décideur
- ✅ Statut 🔴 Nouvelle ou 🔵 En cours
- ✅ Boutons: Prendre en charge, Résoudre, Historique

**Étape 3: Prendre en charge une alerte**
- Cliquez sur "👤 Prendre en charge"

**Résultat attendu:**
- ✅ Message: "✅ Alerte prise en charge"
- ✅ Statut change: 🔵 EN COURS
- ✅ Bouton "Prendre en charge" disparaît

**Étape 4: Résoudre l'alerte**
- Cliquez sur "✅ Résoudre"
- Entrez un commentaire: "Problème résolu après contact avec les clients"

**Résultat attendu:**
- ✅ Message: "✅ Alerte résolue"
- ✅ L'alerte disparaît de la liste active
- ✅ L'alerte apparaît dans l'historique avec statut 🟢 RESOLVED

---

### **TEST SCÉNARIO 3: Historique**

**Étape 1: Développer l'historique**
- Cliquez sur "📜 Historique (7 derniers jours)"

**Résultat attendu:**
- ✅ Tableau avec toutes les alertes des 7 derniers jours
- ✅ Colonnes: KPI, Zone, Date, État, Résolu par
- ✅ Filtrage par statut possible

**Étape 2: Voir l'historique détaillé**
- Cliquez sur "📜 Historique" sur une alerte

**Résultat attendu:**
- ✅ Timeline complète des actions:
  ```
  1. CREATED - System - 23/10/2025 06:00
  2. SENT_TO_PM - Décideur - 23/10/2025 06:15
  3. IN_PROGRESS - Jean Dupont - 23/10/2025 06:30
  4. RESOLVED - Jean Dupont - 23/10/2025 10:00
  ```

---

## 📋 Collections MongoDB

### **Collection: kpi_alerts**

```json
{
  "_id": "67890abc...",
  "kpiName": "TAUX_RETARD",
  "currentValue": 18.5,
  "thresholdValue": 10.0,
  "severity": "HIGH",
  "alertStatus": "PENDING_DECISION",
  "dimension": "GOUVERNORAT",
  "dimensionValue": "Sfax",
  "message": "Taux de retard à 18.5%, seuil critique dépassé",
  "recommendation": "Contacter immédiatement les clients...",
  "recipients": ["pm-id-1"],
  "detectedAt": "2025-10-23T06:00:00",
  "notificationSent": false,
  "notificationSentAt": null,
  "actionHistory": [
    {
      "actionType": "CREATED",
      "performedBy": "system",
      "performedByName": "System",
      "performedAt": "2025-10-23T06:00:00",
      "comment": "Alerte créée automatiquement",
      "newStatus": "PENDING_DECISION"
    }
  ]
}
```

**Après envoi au Chef de Projet:**
```json
{
  "alertStatus": "SENT_TO_PM",
  "notificationSent": true,
  "notificationSentAt": "2025-10-23T06:15:00",
  "actionHistory": [
    // ... actions précédentes
    {
      "actionType": "SENT_TO_PM",
      "performedBy": "decideur-id",
      "performedByName": "M. Ben Youssef",
      "performedAt": "2025-10-23T06:15:00",
      "comment": "Notification envoyée au Chef de Projet",
      "previousStatus": "PENDING_DECISION",
      "newStatus": "SENT_TO_PM"
    }
  ]
}
```

---

## ✅ Avantages de cette Architecture

### **1. Séparation des Responsabilités**
- ✅ Décideur: Valide et envoie les alertes importantes
- ✅ Chef de Projet: Traite les alertes reçues
- ✅ Pas de surcharge d'informations

### **2. Traçabilité Complète**
- ✅ Chaque action enregistrée
- ✅ Qui a fait quoi et quand
- ✅ Audit trail complet

### **3. Dashboard Propre**
- ✅ Décideur voit uniquement les alertes en attente
- ✅ Chef de Projet voit uniquement ses alertes
- ✅ Historique séparé

### **4. Workflow Clair**
- ✅ PENDING_DECISION → SENT_TO_PM → IN_PROGRESS → RESOLVED
- ✅ Pas de confusion sur l'état
- ✅ Responsabilités claires

---

## 🎯 RÉSUMÉ

✅ **Intégration dans les dashboards** (pas de page séparée)  
✅ **Système de statuts** PENDING_DECISION → SENT_TO_PM → RESOLVED  
✅ **Vue Décideur** uniquement alertes en attente  
✅ **Vue Chef de Projet** uniquement alertes reçues  
✅ **Historique complet** consultable par tous  
✅ **Traçabilité totale** de toutes les actions  
✅ **Rafraîchissement automatique** toutes les 30 secondes  

**Votre système d'alertes KPI est maintenant complètement intégré dans les dashboards!** 🚀
