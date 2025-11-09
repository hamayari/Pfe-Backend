# 🚀 GUIDE DE TEST - Notifications Automatiques KPI

## 📋 Vue d'ensemble

Ce système détecte **automatiquement** les anomalies KPI et notifie le Chef de Projet **sans intervention du Décideur**.

### Principe de fonctionnement

```
┌─────────────────────────────────────────────────────────────┐
│                    SYSTÈME AUTOMATIQUE                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. ⏰ Toutes les 5 minutes                                 │
│     └─> Vérification automatique des KPI                    │
│                                                              │
│  2. 📊 Comparaison avec seuils                              │
│     └─> Valeur actuelle vs Seuil de référence              │
│                                                              │
│  3. 🚨 Détection d'anomalie                                 │
│     └─> Si dépassement → Alerte automatique                │
│                                                              │
│  4. 📨 Notification immédiate                               │
│     └─> Envoi WebSocket au Chef de Projet                  │
│                                                              │
│  5. 🔔 Affichage dans le panneau                            │
│     └─> Badge + Notification + Snackbar                    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Seuils KPI Configurés

| KPI | Seuil Avertissement | Seuil Critique | Type |
|-----|---------------------|----------------|------|
| **Taux de retard** | 10% | 15% | Plus bas = pire |
| **Montant impayé** | 20,000 TND | 30,000 TND | Plus haut = pire |
| **Taux de régularisation** | 70% | 60% | Plus bas = pire |
| **Délai moyen de paiement** | 30 jours | 45 jours | Plus haut = pire |
| **Taux de conversion** | 15% | 12% | Plus bas = pire |

---

## 🧪 TEST 1: Déclenchement Manuel (Recommandé)

### Étape 1: Démarrer les services

```bash
# Backend
cd c:/Users/eyaya/OneDrive/Desktop/commercial-pfe/demo
mvn spring-boot:run

# Frontend (autre terminal)
cd c:/Users/eyaya/OneDrive/Desktop/commercial-pfe/demo/app-frontend-new
ng serve
```

### Étape 2: Se connecter comme Chef de Projet

- URL: `http://localhost:4200/login`
- Username: `projectmanager`
- Password: `pm123456`

### Étape 3: Ouvrir la console navigateur (F12)

Vérifiez que vous voyez:
```
✅ WebSocket connected for project manager dashboard
🔔 [KPI ALERTS] Abonnement aux alertes KPI pour: projectmanager
```

### Étape 4: Déclencher la vérification manuelle

**Option A: Via Postman**
```http
POST http://localhost:8080/api/kpi-alerts/check-now
Authorization: Bearer {VOTRE_TOKEN}
```

**Option B: Via navigateur (nouvel onglet)**
```
http://localhost:8080/api/kpi-alerts/check-now
```

### Étape 5: Vérifier les résultats

**Console Backend:**
```
========================================
🔍 [AUTO KPI] Vérification automatique des KPI
⏰ Heure: 2025-10-23T05:58:00
========================================
🚨 Anomalie détectée: TAUX_RETARD = 18.0
🚨 Anomalie détectée: MONTANT_IMPAYE = 35000.0
🚨 Anomalie détectée: TAUX_REGULARISATION = 55.0
📢 [AUTO KPI] 3 anomalie(s) détectée(s)
========================================
📨 [AUTO NOTIFICATION] Envoi au Chef de Projet
✅ Notification envoyée au topic général
✅ Notification personnelle envoyée à: projectmanager
✅ Notification interne créée
========================================
```

**Console Frontend (F12):**
```
========================================
🚨 [KPI ALERT] Nouvelle alerte KPI reçue via WebSocket
📊 Alerte: {
  type: "KPI_ALERT",
  kpiName: "Taux de factures en retard",
  severity: "HIGH",
  message: "Taux de factures en retard a atteint 18.0%, au-dessus du seuil critique de 15.0%",
  autoDetected: true
}
========================================
📬 Notification ajoutée au panneau
```

**Interface Chef de Projet:**
1. ✅ **Snackbar rouge** apparaît en haut à droite
2. ✅ **Badge sur la cloche** = 3 (nombre d'anomalies)
3. ✅ **Panneau de notifications** (cliquez sur 🔔):
   - 3 notifications avec icônes rouges/oranges
   - Timestamps "À l'instant"
   - Points bleus (non lues)

---

## 🧪 TEST 2: Vérification Automatique (Toutes les 5 minutes)

### Étape 1: Laisser le système tourner

Après avoir démarré le backend, le système vérifie automatiquement toutes les 5 minutes.

### Étape 2: Attendre 5 minutes

Vous verrez dans la console backend:
```
🔍 [AUTO KPI] Vérification automatique des KPI
⏰ Heure: 2025-10-23T06:03:00
```

### Étape 3: Vérifier les notifications

Si des anomalies persistent, de nouvelles notifications seront envoyées automatiquement.

---

## 🧪 TEST 3: Vérifier les Seuils Configurés

```http
GET http://localhost:8080/api/kpi-alerts/thresholds
```

**Réponse attendue:**
```json
{
  "status": "success",
  "thresholds": {
    "TAUX_RETARD": {
      "warningThreshold": 10.0,
      "criticalThreshold": 15.0,
      "displayName": "Taux de factures en retard",
      "unit": "%"
    },
    "MONTANT_IMPAYE": {
      "warningThreshold": 20000.0,
      "criticalThreshold": 30000.0,
      "displayName": "Montant total impayé",
      "unit": "TND"
    }
  },
  "checkInterval": "5 minutes"
}
```

---

## 📊 Scénarios de Test Détaillés

### Scénario 1: Taux de retard critique

**Valeur simulée:** 18%  
**Seuil critique:** 15%  
**Résultat:** ✅ Alerte HIGH envoyée

**Notification attendue:**
- 🔴 Icône rouge
- Titre: "Alerte KPI: Taux de factures en retard"
- Message: "Taux de factures en retard a atteint 18.0%, au-dessus du seuil critique de 15.0%"
- Recommandation: "Contacter immédiatement les clients avec factures en retard"

### Scénario 2: Montant impayé élevé

**Valeur simulée:** 35,000 TND  
**Seuil critique:** 30,000 TND  
**Résultat:** ✅ Alerte HIGH envoyée

**Notification attendue:**
- 🔴 Icône rouge
- Titre: "Alerte KPI: Montant total impayé"
- Message: "Montant total impayé a atteint 35000.0 TND, au-dessus du seuil critique de 30000.0 TND"
- Recommandation: "Prioriser le recouvrement des créances"

### Scénario 3: Taux de régularisation faible

**Valeur simulée:** 55%  
**Seuil critique:** 60%  
**Résultat:** ✅ Alerte HIGH envoyée

**Notification attendue:**
- 🔴 Icône rouge
- Titre: "Alerte KPI: Taux de régularisation"
- Message: "Taux de régularisation est tombé à 55.0%, en dessous du seuil critique de 60.0%"
- Recommandation: "Accélérer le processus de régularisation"

---

## ✅ Checklist de Vérification

### Backend
- [ ] Service `AutomaticKpiAlertService` créé
- [ ] Controller `KpiAlertController` créé
- [ ] `@EnableScheduling` activé dans `DemoApplication`
- [ ] Backend démarré sans erreurs
- [ ] Logs de vérification visibles toutes les 5 minutes

### Frontend
- [ ] Panneau de notifications créé
- [ ] WebSocket connecté
- [ ] Abonnement aux alertes KPI actif
- [ ] Méthode `addNotificationToPanel()` implémentée

### Test Manuel
- [ ] Endpoint `/api/kpi-alerts/check-now` accessible
- [ ] Notifications reçues dans la console frontend
- [ ] Badge de notification mis à jour
- [ ] Panneau affiche les notifications
- [ ] Timestamps corrects ("À l'instant")
- [ ] Icônes colorées selon la sévérité

### Test Automatique
- [ ] Attendre 5 minutes
- [ ] Vérifier les logs backend
- [ ] Nouvelles notifications reçues
- [ ] Badge incrémenté

---

## 🔧 Dépannage

### Problème: Aucune notification reçue

**Solutions:**
1. Vérifier que WebSocket est connecté (console frontend)
2. Vérifier les logs backend pour les erreurs
3. Vérifier que l'utilisateur est bien "projectmanager"
4. Redémarrer le backend et le frontend

### Problème: Badge ne s'incrémente pas

**Solutions:**
1. Vérifier que `updateNotificationCount()` est appelée
2. Vérifier que `realtimeNotifications` est bien un tableau
3. Ouvrir la console et chercher les erreurs JavaScript

### Problème: Panneau vide

**Solutions:**
1. Vérifier que `addNotificationToPanel()` est appelée
2. Vérifier que `showNotificationsPanel = true`
3. Vérifier le CSS du panneau

---

## 📈 Prochaines Étapes

### 1. Connecter aux vraies données

Modifier `getCurrentKpiValues()` dans `AutomaticKpiAlertService.java`:
```java
private Map<String, Double> getCurrentKpiValues() {
    // Remplacer par les vraies requêtes MongoDB
    Map<String, Double> kpis = new HashMap<>();
    
    // Exemple: Calculer le vrai taux de retard
    long totalInvoices = invoiceRepository.count();
    long overdueInvoices = invoiceRepository.countByStatus("OVERDUE");
    double tauxRetard = (overdueInvoices * 100.0) / totalInvoices;
    kpis.put("TAUX_RETARD", tauxRetard);
    
    return kpis;
}
```

### 2. Personnaliser les seuils

Créer une interface admin pour modifier les seuils dynamiquement.

### 3. Historique des alertes

Sauvegarder les alertes dans MongoDB pour analyse ultérieure.

---

## 🎯 Résumé

✅ **Système automatique** qui détecte les anomalies KPI  
✅ **Notifications en temps réel** via WebSocket  
✅ **Panneau style Facebook** avec timestamps  
✅ **Aucune intervention du Décideur** nécessaire  
✅ **Vérification toutes les 5 minutes** + déclenchement manuel  

**Le Chef de Projet est maintenant notifié automatiquement dès qu'un KPI devient anormal!** 🚀
