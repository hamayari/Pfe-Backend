# 🔄 FLUX CORRECT DES ALERTES KPI

## 🚨 PROBLÈME IDENTIFIÉ

### Ce qui se passait (INCORRECT) :
```
Scheduler (toutes les 5 min)
    ↓
Détection anomalie KPI
    ↓
❌ Alerte envoyée DIRECTEMENT au Chef de Projet
    ↓
❌ Décideur ne voit RIEN (0 alertes)
    ↓
❌ 405 notifications accumulées pour le Chef de Projet
```

### Logs montrant le problème :
```
03:47:14 [AUTO KPI] Vérification automatique des KPI
03:47:14 📢 [AUTO NOTIFICATION] Envoi au Chef de Projet  ← ERREUR ICI
03:47:14 ✅ Notification personnelle envoyée à: projectmanager
03:47:14 📊 Compteur: count=403, 404, 405...
```

**Résultat :**
- Décideur : 0 alertes ❌
- Chef de Projet : 405 notifications ❌
- Pas de workflow de délégation ❌

---

## ✅ FLUX CORRECT (APRÈS CORRECTION)

### Étape 1 : Détection Automatique
```
⏰ Scheduler (toutes les 5 minutes)
    ↓
🔍 AutomaticKpiAlertService.checkKpiAnomalies()
    ↓
📊 Calcul des KPI actuels
    - Taux de retard: 58.3%
    - Taux de régularisation: 16.7%
    - Taux de conversion: 0.0%
    ↓
⚠️ Détection des anomalies (dépassement de seuils)
```

### Étape 2 : Création de l'Alerte pour le DÉCIDEUR
```
💾 Création KpiAlert dans MongoDB
    - kpiName: "TAUX_RETARD"
    - severity: "HIGH"
    - status: "🔴 ANORMAL"
    - alertStatus: "PENDING_DECISION"  ← Important !
    - recipients: [decideur_id]  ← UNIQUEMENT le Décideur
    ↓
📤 Envoi WebSocket
    - Topic général: /topic/kpi-alerts
    - Queue personnelle: /user/{decideur}/queue/kpi-alerts
    ↓
📱 Notification in-app créée
    - userId: decideur_id
    - type: "KPI_ALERT"
    - title: "🚨 Alerte KPI Automatique"
    - priority: "HIGH"
```

### Étape 3 : Décideur Voit l'Alerte
```
👤 Décideur se connecte
    ↓
📊 Dashboard Décideur
    ↓
🔔 Section "Alertes KPI en Attente de Décision"
    ↓
Affiche: "Taux de retard a atteint 58.3%"
    ↓
Options:
    [Envoyer au Chef de Projet]  ← Délégation
    [Marquer comme traité]
    [Ignorer]
```

### Étape 4 : Délégation (Si le Décideur choisit)
```
👤 Décideur clique "Envoyer au Chef de Projet"
    ↓
📤 POST /api/kpi-alerts/{alertId}/delegate-to-pm
    ↓
🔄 AutomaticKpiAlertService.delegateAlertToProjectManager()
    ↓
💾 Mise à jour de l'alerte
    - alertStatus: "DELEGATED"
    - recipients: [chef_projet_id]  ← Changement
    - message: "🔄 [Délégué par le Décideur] ..."
    ↓
📤 Notification au Chef de Projet
    - Type: "ALERT_DELEGATED"
    - WebSocket: /user/{chef_projet}/queue/kpi-alerts
    - Notification in-app créée
    ↓
✅ Alerte disparaît du panel du Décideur
✅ Alerte apparaît dans le panel du Chef de Projet
```

### Étape 5 : Chef de Projet Reçoit l'Alerte
```
👤 Chef de Projet se connecte
    ↓
🔔 Badge de notification: (1)
    ↓
📱 Panneau de notifications
    ↓
Affiche: "🔄 Alerte KPI Déléguée"
    "Le Décideur vous a délégué: Taux de retard..."
    ↓
Clic sur la notification
    ↓
🎯 Redirection vers les détails de l'alerte
```

---

## 📊 COMPARAISON AVANT/APRÈS

### AVANT (Incorrect)
| Acteur | Alertes Reçues | Statut |
|--------|----------------|--------|
| Décideur | 0 | ❌ Ne voit rien |
| Chef de Projet | 405 | ❌ Submergé |
| Workflow | Aucun | ❌ Pas de délégation |

### APRÈS (Correct)
| Acteur | Alertes Reçues | Statut |
|--------|----------------|--------|
| Décideur | 3 (nouvelles) | ✅ Peut décider |
| Chef de Projet | 0 (en attente) | ✅ Reçoit si délégué |
| Workflow | Complet | ✅ Délégation fonctionnelle |

---

## 🔧 MODIFICATIONS APPORTÉES

### 1. AutomaticKpiAlertService.java

**Ligne ~250 : Destinataires**
```java
// AVANT (INCORRECT)
// Ajouter tous les décideurs
for (User dm : decisionMakers) {
    recipients.add(dm.getId());
}
// Ajouter tous les chefs de projet  ← ERREUR
for (User pm : projectManagers) {
    recipients.add(pm.getId());
}

// APRÈS (CORRECT)
// ⚠️ IMPORTANT: Les alertes vont UNIQUEMENT au Décideur d'abord
List<User> decisionMakers = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
for (User dm : decisionMakers) {
    recipients.add(dm.getId());
}
// PAS de Chef de Projet ici !
```

**Ligne ~290 : Notifications**
```java
// AVANT (INCORRECT)
// Envoyer à chaque Chef de Projet
for (User pm : projectManagers) {
    messagingTemplate.convertAndSendToUser(pm.getUsername(), ...);
    notificationService.createNotification(pm.getId(), ...);
}

// APRÈS (CORRECT)
// ⚠️ IMPORTANT: Envoyer UNIQUEMENT aux Décideurs
List<User> decisionMakers = userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER);
for (User dm : decisionMakers) {
    messagingTemplate.convertAndSendToUser(dm.getUsername(), ...);
    notificationService.createNotification(dm.getId(), ...);
}
```

---

## 🧪 TEST DU FLUX CORRECT

### Scénario de Test

**1. Attendre la détection automatique (ou forcer)**
```bash
# Forcer la vérification manuelle
curl -X POST http://localhost:8080/api/kpi/check-manual
```

**2. Vérifier les logs**
```
✅ [AUTO NOTIFICATION] Envoi au Décideur (en attente de décision)
✅ Notification personnelle envoyée au Décideur: decideur
✅ Notification in-app créée pour le Décideur
📊 Alerte envoyée à 1 Décideur(s)
```

**3. Se connecter en tant que Décideur**
- Dashboard → Section "Alertes KPI"
- Doit voir : 3 alertes en attente
- Badge : (3)

**4. Déléguer une alerte**
- Cliquer "Envoyer au Chef de Projet"
- Alerte disparaît du panel
- Reste dans l'historique

**5. Se connecter en tant que Chef de Projet**
- Badge de notification : (1)
- Panneau : "🔄 Alerte KPI Déléguée"
- Clic → Redirection vers détails

---

## 📋 CHECKLIST DE VALIDATION

### Backend
- [ ] Alertes créées avec `alertStatus: "PENDING_DECISION"`
- [ ] Destinataires = UNIQUEMENT Décideurs
- [ ] Notifications envoyées UNIQUEMENT aux Décideurs
- [ ] Logs montrent "Envoi au Décideur"
- [ ] Pas de notification au Chef de Projet (sauf délégation)

### Frontend - Décideur
- [ ] Section "Alertes KPI" affiche les alertes
- [ ] Badge avec compteur correct
- [ ] Bouton "Envoyer au Chef de Projet" visible
- [ ] Après délégation, alerte disparaît
- [ ] Historique conserve l'alerte

### Frontend - Chef de Projet
- [ ] Aucune alerte avant délégation
- [ ] Après délégation, badge s'incrémente
- [ ] Notification "Alerte Déléguée" visible
- [ ] Clic redirige vers détails
- [ ] Type = "ALERT_DELEGATED"

---

## 🎯 RÉSULTAT ATTENDU

### Logs Corrects
```
03:47:14 [AUTO KPI] Vérification automatique des KPI
03:47:14 📨 [AUTO NOTIFICATION] Envoi au Décideur (en attente de décision)
03:47:14 📋 Destinataires: 1 Décideur(s) uniquement
03:47:14 💾 Alerte sauvegardée: 6902d1b2eca7c859ab3c6596
03:47:14 ✅ Notification personnelle envoyée au Décideur: decideur
03:47:14 ✅ Notification in-app créée pour le Décideur
03:47:14 📊 Alerte envoyée à 1 Décideur(s)
```

### Interface Décideur
```
🔔 Alertes KPI en Attente de Décision

┌─────────────────────────────────────────────────┐
│ 🔴 Taux de retard a atteint 58.3%              │
│ Recommandation: Contacter les clients...       │
│ [Envoyer au Chef de Projet] [Traiter] [Ignorer]│
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│ 🟡 Taux de régularisation tombé à 16.7%       │
│ Recommandation: Accélérer le processus...      │
│ [Envoyer au Chef de Projet] [Traiter] [Ignorer]│
└─────────────────────────────────────────────────┘
```

### Interface Chef de Projet (Après Délégation)
```
🔔 Notifications (1)

┌─────────────────────────────────────────────────┐
│ 🔄 Alerte KPI Déléguée                         │
│ Le Décideur vous a délégué une alerte:         │
│ Taux de retard a atteint 58.3%                 │
│ Il y a 2 min                                    │
└─────────────────────────────────────────────────┘
```

---

## ✅ STATUT

- ✅ Code corrigé
- ✅ Flux de délégation restauré
- ✅ Alertes vont au Décideur d'abord
- ✅ Chef de Projet reçoit uniquement si délégué
- ⏳ À tester après redémarrage

---

**Date:** 30 Octobre 2025  
**Version:** 2.0  
**Correction:** Flux de délégation restauré
