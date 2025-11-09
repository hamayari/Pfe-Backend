# 🔄 SYSTÈME DE GESTION DU CYCLE DE VIE DES ALERTES KPI

## 🎯 Problème Résolu

**AVANT:**
- ❌ Les alertes s'accumulent dans le dashboard
- ❌ Pas de traçabilité des actions
- ❌ Encombrement visuel
- ❌ Pas de suivi de résolution

**APRÈS:**
- ✅ Cycle de vie complet: NEW → IN_PROGRESS → RESOLVED → ARCHIVED
- ✅ Traçabilité totale de toutes les actions
- ✅ Dashboard propre (seules les alertes actives)
- ✅ Historique complet consultable
- ✅ Système professionnel avec audit trail

---

## 📊 Cycle de Vie d'une Alerte

```
┌─────────────────────────────────────────────────────────────┐
│                   CYCLE DE VIE COMPLET                       │
└─────────────────────────────────────────────────────────────┘

1️⃣ NEW (Nouvelle)
   ↓
   └─> Détectée automatiquement par le système
   └─> Apparaît dans le dashboard du Chef de Projet
   └─> Badge de notification +1
   
2️⃣ IN_PROGRESS (En cours)
   ↓
   └─> Chef de Projet prend en charge
   └─> Ajoute un commentaire
   └─> Reste visible dans "Alertes actives"
   
3️⃣ RESOLVED (Résolue)
   ↓
   └─> Chef de Projet résout le problème
   └─> Ajoute: commentaire + actions prises
   └─> Disparaît du dashboard principal
   └─> Visible dans "Alertes résolues" (7 jours)
   
4️⃣ ARCHIVED (Archivée)
   ↓
   └─> Après 30 jours ou manuellement
   └─> Stockée dans l'historique
   └─> Consultable pour audit
   └─> Traçabilité complète préservée
```

---

## 🗂️ Structure de Données

### **Modèle KpiAlert**

```java
{
  "id": "67890abc...",
  "kpiName": "TAUX_RETARD",
  "currentValue": 18.5,
  "severity": "HIGH",
  "alertStatus": "NEW",  // NEW, IN_PROGRESS, RESOLVED, ARCHIVED
  "message": "Taux de retard à 18.5%, seuil critique dépassé",
  "recommendation": "Contacter immédiatement les clients...",
  "recipients": ["pm-id-1", "pm-id-2"],
  "detectedAt": "2025-10-23T06:00:00",
  "resolvedAt": null,
  "resolvedBy": null,
  "resolvedByName": null,
  "resolutionComment": null,
  "actionsTaken": null,
  "archivedAt": null,
  "archivedBy": null,
  "priority": "CRITICAL",
  "actionHistory": [
    {
      "actionType": "CREATED",
      "performedBy": "system",
      "performedByName": "System",
      "performedAt": "2025-10-23T06:00:00",
      "comment": "Alerte créée automatiquement",
      "previousStatus": null,
      "newStatus": "NEW"
    }
  ]
}
```

---

## 🔧 API Endpoints

### **1. Obtenir les alertes actives**
```http
GET /api/kpi-alerts/manage/active
Authorization: Bearer {TOKEN}
```

**Réponse:**
```json
{
  "status": "success",
  "count": 3,
  "alerts": [
    {
      "id": "alert-123",
      "kpiName": "TAUX_RETARD",
      "currentValue": 18.5,
      "severity": "HIGH",
      "alertStatus": "NEW",
      "message": "...",
      "detectedAt": "2025-10-23T06:00:00"
    }
  ]
}
```

### **2. Marquer comme "En cours"**
```http
POST /api/kpi-alerts/manage/{alertId}/in-progress
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "comment": "Je prends en charge cette alerte"
}
```

### **3. Résoudre une alerte**
```http
POST /api/kpi-alerts/manage/{alertId}/resolve
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "resolutionComment": "Problème résolu après contact avec les clients",
  "actionsTaken": "1. Contacté 7 clients en retard\n2. Négocié nouveaux délais\n3. Mis en place rappels automatiques"
}
```

### **4. Archiver une alerte**
```http
POST /api/kpi-alerts/manage/{alertId}/archive
Authorization: Bearer {TOKEN}
```

### **5. Ajouter un commentaire**
```http
POST /api/kpi-alerts/manage/{alertId}/comment
Authorization: Bearer {TOKEN}
Content-Type: application/json

{
  "comment": "En attente de retour client ABC"
}
```

### **6. Obtenir l'historique**
```http
GET /api/kpi-alerts/manage/{alertId}/history
Authorization: Bearer {TOKEN}
```

**Réponse:**
```json
{
  "status": "success",
  "count": 4,
  "history": [
    {
      "actionType": "CREATED",
      "performedBy": "system",
      "performedByName": "System",
      "performedAt": "2025-10-23T06:00:00",
      "comment": "Alerte créée automatiquement",
      "newStatus": "NEW"
    },
    {
      "actionType": "IN_PROGRESS",
      "performedBy": "pm-123",
      "performedByName": "Jean Dupont",
      "performedAt": "2025-10-23T06:15:00",
      "comment": "Je prends en charge",
      "previousStatus": "NEW",
      "newStatus": "IN_PROGRESS"
    },
    {
      "actionType": "COMMENTED",
      "performedBy": "pm-123",
      "performedByName": "Jean Dupont",
      "performedAt": "2025-10-23T07:30:00",
      "comment": "Contacté 3 clients, en attente de retour"
    },
    {
      "actionType": "RESOLVED",
      "performedBy": "pm-123",
      "performedByName": "Jean Dupont",
      "performedAt": "2025-10-23T10:00:00",
      "comment": "Problème résolu",
      "previousStatus": "IN_PROGRESS",
      "newStatus": "RESOLVED"
    }
  ]
}
```

### **7. Obtenir les statistiques**
```http
GET /api/kpi-alerts/manage/statistics
Authorization: Bearer {TOKEN}
```

**Réponse:**
```json
{
  "status": "success",
  "statistics": {
    "new": 5,
    "inProgress": 3,
    "resolved": 12,
    "archived": 45,
    "total": 65,
    "active": 8
  }
}
```

---

## 🧪 Scénarios de Test

### **Scénario 1: Cycle de vie complet**

**Étape 1: Alerte détectée automatiquement**
```bash
# Le système détecte une anomalie
POST http://localhost:8080/api/kpi-alerts/check-now
```

**Résultat:**
- ✅ Alerte créée dans MongoDB
- ✅ Statut: NEW
- ✅ Notification envoyée au Chef de Projet
- ✅ Apparaît dans le dashboard

**Étape 2: Chef de Projet prend en charge**
```bash
POST http://localhost:8080/api/kpi-alerts/manage/alert-123/in-progress
{
  "comment": "Je m'en occupe immédiatement"
}
```

**Résultat:**
- ✅ Statut: IN_PROGRESS
- ✅ Action ajoutée à l'historique
- ✅ Reste visible dans "Alertes actives"

**Étape 3: Ajout de commentaires**
```bash
POST http://localhost:8080/api/kpi-alerts/manage/alert-123/comment
{
  "comment": "Contacté 5 clients, 3 ont confirmé le paiement"
}
```

**Résultat:**
- ✅ Commentaire ajouté à l'historique
- ✅ Traçabilité complète

**Étape 4: Résolution**
```bash
POST http://localhost:8080/api/kpi-alerts/manage/alert-123/resolve
{
  "resolutionComment": "Tous les clients ont payé",
  "actionsTaken": "1. Contacté 7 clients\n2. Négocié délais\n3. Reçu 5 paiements"
}
```

**Résultat:**
- ✅ Statut: RESOLVED
- ✅ Disparaît du dashboard principal
- ✅ Visible dans "Alertes résolues" (7 jours)
- ✅ Historique complet préservé

**Étape 5: Archivage (automatique après 30 jours)**
```bash
# Automatique ou manuel:
POST http://localhost:8080/api/kpi-alerts/manage/alert-123/archive
```

**Résultat:**
- ✅ Statut: ARCHIVED
- ✅ Stockée dans l'historique
- ✅ Consultable pour audit

---

## 📱 Intégration Frontend

### **Composant Angular - Gestion des Alertes**

```typescript
// Obtenir les alertes actives
getActiveAlerts() {
  this.http.get('/api/kpi-alerts/manage/active').subscribe(
    (response: any) => {
      this.activeAlerts = response.alerts;
      this.alertCount = response.count;
    }
  );
}

// Marquer comme en cours
markAsInProgress(alertId: string) {
  this.http.post(`/api/kpi-alerts/manage/${alertId}/in-progress`, {
    comment: 'Prise en charge'
  }).subscribe(
    () => {
      this.showSuccess('Alerte prise en charge');
      this.refreshAlerts();
    }
  );
}

// Résoudre
resolveAlert(alertId: string, comment: string, actions: string) {
  this.http.post(`/api/kpi-alerts/manage/${alertId}/resolve`, {
    resolutionComment: comment,
    actionsTaken: actions
  }).subscribe(
    () => {
      this.showSuccess('Alerte résolue');
      this.refreshAlerts();
    }
  );
}
```

### **Interface Utilisateur**

```
┌─────────────────────────────────────────────────────────┐
│  ALERTES KPI                                    [5] 🔴  │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  📊 Alertes Actives (5)                                 │
│  ┌────────────────────────────────────────────────┐    │
│  │ 🔴 Taux de retard: 18.5%                       │    │
│  │ Détectée: Il y a 2h                            │    │
│  │ [Prendre en charge] [Voir détails]             │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │ 🟡 Montant impayé: 45,000 TND                  │    │
│  │ En cours par Jean Dupont                       │    │
│  │ [Ajouter commentaire] [Résoudre]               │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
│  📝 Alertes Résolues (12)                               │
│  ┌────────────────────────────────────────────────┐    │
│  │ ✅ Délai paiement: Résolu                      │    │
│  │ Résolu par: Marie Martin                       │    │
│  │ Le: 22/10/2025 à 14:30                         │    │
│  │ [Voir historique] [Archiver]                   │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
│  📦 Historique (45 archivées)                           │
│  [Consulter l'historique complet]                       │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## ✅ Avantages du Système

### **1. Traçabilité Complète**
- ✅ Chaque action est enregistrée
- ✅ Qui a fait quoi et quand
- ✅ Historique complet consultable
- ✅ Audit trail professionnel

### **2. Dashboard Propre**
- ✅ Seules les alertes actives visibles
- ✅ Pas d'encombrement visuel
- ✅ Meilleure UX
- ✅ Focus sur ce qui est important

### **3. Gestion Professionnelle**
- ✅ Workflow structuré
- ✅ Responsabilités claires
- ✅ Suivi de résolution
- ✅ Métriques de performance

### **4. Conformité & Audit**
- ✅ Toutes les actions tracées
- ✅ Historique immuable
- ✅ Rapports d'audit possibles
- ✅ Conformité réglementaire

---

## 📈 Métriques Disponibles

```typescript
{
  "new": 5,              // Nouvelles alertes non traitées
  "inProgress": 3,       // Alertes en cours de traitement
  "resolved": 12,        // Alertes résolues (7 derniers jours)
  "archived": 45,        // Alertes archivées
  "total": 65,           // Total toutes alertes
  "active": 8            // Alertes actives (NEW + IN_PROGRESS)
}
```

**KPIs de performance:**
- Temps moyen de résolution
- Taux de résolution
- Nombre d'alertes par chef de projet
- Alertes récurrentes

---

## 🚀 Démarrage Rapide

### **1. Démarrer le backend**
```bash
cd c:/Users/eyaya/OneDrive/Desktop/commercial-pfe/demo
mvn spring-boot:run
```

### **2. Tester la création d'alerte**
```bash
POST http://localhost:8080/api/kpi-alerts/check-now
```

### **3. Voir les alertes actives**
```bash
GET http://localhost:8080/api/kpi-alerts/manage/active
Authorization: Bearer {TOKEN}
```

### **4. Résoudre une alerte**
```bash
POST http://localhost:8080/api/kpi-alerts/manage/{alertId}/resolve
{
  "resolutionComment": "Problème résolu",
  "actionsTaken": "Actions prises..."
}
```

---

## 🎯 Résumé

✅ **Cycle de vie complet**: NEW → IN_PROGRESS → RESOLVED → ARCHIVED  
✅ **Traçabilité totale**: Chaque action enregistrée  
✅ **Dashboard propre**: Seules les alertes actives  
✅ **Historique complet**: Consultable pour audit  
✅ **Système professionnel**: Workflow structuré  
✅ **Archivage automatique**: Après 30 jours  
✅ **Métriques de performance**: Temps de résolution, taux de résolution  

**Le système est maintenant professionnel avec traçabilité complète!** 🚀
