# 🔔 GUIDE - Notifications Déléguées pour Chef de Projet

## ✅ FONCTIONNALITÉ COMPLÉTÉE

La fonctionnalité **CPR-002 : Notifications déléguées** est maintenant **TERMINÉE** !

---

## 📋 RÉSUMÉ DE L'IMPLÉMENTATION

### Backend (Java Spring Boot)

#### 1. Service AutomaticKpiAlertService.java
✅ **Méthode `delegateAlertToProjectManager()`**
- Récupère l'alerte KPI depuis MongoDB
- Met à jour les destinataires (Chef de Projet uniquement)
- Marque l'alerte comme déléguée dans le message
- **NOUVEAU** : Crée une notification in-app persistante
- Envoie une notification WebSocket temps réel
- Sauvegarde dans MongoDB

#### 2. Service InAppNotificationService.java
✅ **Nouvelle méthode `getDelegatedAlerts()`**
- Filtre les notifications de type `ALERT_DELEGATED`
- Retourne uniquement les alertes déléguées au Chef de Projet
- Exclut les notifications supprimées

#### 3. Controller NotificationController.java
✅ **Nouveau endpoint `GET /api/notifications/user/{userId}/delegated-alerts`**
- Accessible uniquement aux Chefs de Projet (`@PreAuthorize("hasRole('PROJECT_MANAGER')")`)
- Retourne la liste des alertes déléguées

#### 4. Repository NotificationRepository.java
✅ **Nouvelle méthode**
```java
List<Notification> findByUserIdAndTypeAndDeletedFalseOrderByTimestampDesc(String userId, String type);
```

---

## 🔄 FLUX COMPLET DE DÉLÉGATION

### Étape 1 : Décideur délègue une alerte
```
1. Décideur voit une alerte KPI dans son dashboard
2. Clique sur "Envoyer au Chef de Projet"
3. Frontend appelle: POST /api/kpi-alerts/{alertId}/delegate-to-pm
```

### Étape 2 : Backend traite la délégation
```java
// AutomaticKpiAlertService.delegateAlertToProjectManager()

1. Récupère l'alerte depuis MongoDB
2. Change les destinataires → PROJECT_MANAGER
3. Ajoute "[Délégué par le Décideur]" au message
4. Sauvegarde l'alerte mise à jour

5. NOUVEAU : Crée une notification in-app
   NotificationLog notif = new NotificationLog();
   notif.setType("ALERT_DELEGATED");
   notif.setTitle("🔄 Alerte KPI Déléguée");
   notif.setMessage("Le Décideur vous a délégué une alerte...");
   notif.setCategory("KPI_ALERT");
   notif.setLink("/project-manager/alerts/" + alertId);
   
6. Envoie notification WebSocket
   → /topic/kpi-alerts (broadcast)
   → /user/{username}/queue/kpi-alerts (personnel)
```

### Étape 3 : Chef de Projet reçoit la notification
```
1. WebSocket reçoit la notification en temps réel
2. Badge de notification s'incrémente automatiquement
3. Notification apparaît dans le panneau
4. Notification persistée dans MongoDB
```

### Étape 4 : Chef de Projet consulte les alertes
```
Frontend appelle:
GET /api/notifications/user/{userId}/delegated-alerts

Retourne:
[
  {
    "id": "abc123",
    "type": "ALERT_DELEGATED",
    "title": "🔄 Alerte KPI Déléguée",
    "message": "Le Décideur vous a délégué une alerte: Taux de retard...",
    "priority": "HIGH",
    "category": "KPI_ALERT",
    "link": "/project-manager/alerts/xyz789",
    "read": false,
    "timestamp": "2025-10-30T14:30:00"
  }
]
```

---

## 🧪 TESTS À EFFECTUER

### Test 1 : Délégation Simple
```
1. Se connecter en tant que DÉCIDEUR
2. Aller sur le dashboard décideur
3. Voir les alertes KPI
4. Cliquer "Envoyer au Chef de Projet" sur une alerte
5. ✅ Vérifier que l'alerte disparaît du panel du décideur
6. ✅ Vérifier qu'elle reste dans l'historique

7. Se connecter en tant que CHEF DE PROJET (autre navigateur)
8. ✅ Vérifier que le badge de notification s'incrémente
9. ✅ Cliquer sur l'icône de notifications
10. ✅ Voir la notification "Alerte KPI Déléguée"
11. ✅ Cliquer sur la notification
12. ✅ Être redirigé vers les détails de l'alerte
```

### Test 2 : Notifications Multiples
```
1. Décideur délègue 3 alertes différentes
2. ✅ Chef de Projet voit badge = 3
3. ✅ Panneau affiche les 3 notifications
4. ✅ Chaque notification a un lien vers l'alerte correspondante
```

### Test 3 : Marquer comme Lu
```
1. Chef de Projet clique sur une notification
2. ✅ Notification marquée comme lue
3. ✅ Badge décrémente automatiquement
4. ✅ Notification reste visible mais avec style "lu"
```

### Test 4 : Temps Réel
```
1. Chef de Projet connecté sur son dashboard
2. Décideur délègue une alerte (autre navigateur)
3. ✅ Chef de Projet reçoit la notification IMMÉDIATEMENT
4. ✅ Badge s'incrémente sans refresh
5. ✅ Son de notification (si activé)
```

### Test 5 : Endpoint API
```bash
# Récupérer les alertes déléguées
curl -X GET "http://localhost:8080/api/notifications/user/{userId}/delegated-alerts" \
  -H "Authorization: Bearer {token}"

# Réponse attendue
[
  {
    "id": "...",
    "type": "ALERT_DELEGATED",
    "title": "🔄 Alerte KPI Déléguée",
    "message": "...",
    "priority": "HIGH",
    "read": false
  }
]
```

---

## 📊 ENDPOINTS DISPONIBLES

### Pour le Chef de Projet

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/notifications/user/{userId}` | Toutes les notifications |
| GET | `/api/notifications/user/{userId}/unread` | Notifications non lues |
| GET | `/api/notifications/user/{userId}/unread/count` | Compteur non lues |
| GET | `/api/notifications/user/{userId}/delegated-alerts` | **NOUVEAU** Alertes déléguées |
| PUT | `/api/notifications/{notificationId}/read` | Marquer comme lue |
| PUT | `/api/notifications/user/{userId}/read-all` | Tout marquer comme lu |
| DELETE | `/api/notifications/{notificationId}` | Supprimer notification |

### Pour le Décideur

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/kpi-alerts/{alertId}/delegate-to-pm` | Déléguer une alerte |
| GET | `/api/kpi-alerts/history` | Historique des délégations |

---

## 🎨 INTERFACE UTILISATEUR

### Dashboard Chef de Projet

```
┌─────────────────────────────────────────────────────────┐
│  🏠 Dashboard Chef de Projet                            │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  🔔 Notifications (3)                            │  │
│  │                                                   │  │
│  │  🔴 Alerte KPI Déléguée                         │  │
│  │  Le Décideur vous a délégué: Taux de retard...  │  │
│  │  Il y a 5 minutes                                │  │
│  │                                                   │  │
│  │  🟡 Alerte KPI Déléguée                         │  │
│  │  Le Décideur vous a délégué: Montant impayé...  │  │
│  │  Il y a 15 minutes                               │  │
│  │                                                   │  │
│  │  🟢 Tâche assignée                              │  │
│  │  Nouvelle tâche: Relancer client X              │  │
│  │  Il y a 1 heure                                  │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 CONFIGURATION WEBSOCKET

### Canaux utilisés

```javascript
// Topic général (broadcast)
/topic/kpi-alerts

// Queue personnelle (utilisateur spécifique)
/user/{username}/queue/kpi-alerts

// Compteur de notifications
/topic/notifications/{userId}/count

// Notifications individuelles
/topic/notifications/{userId}
```

### Frontend (Angular)

```typescript
// S'abonner aux alertes déléguées
this.websocket.subscribe('/user/queue/kpi-alerts', (message: any) => {
  console.log('🚨 Alerte déléguée reçue:', message);
  this.showNotification(message);
  this.loadDelegatedAlerts();
});

// Charger les alertes déléguées
loadDelegatedAlerts() {
  const userId = this.authService.currentUserValue.id;
  this.http.get(`/api/notifications/user/${userId}/delegated-alerts`)
    .subscribe(alerts => {
      this.delegatedAlerts = alerts;
      this.notificationCount = alerts.filter(a => !a.read).length;
    });
}
```

---

## ✅ CHECKLIST DE VALIDATION

### Backend
- [x] Méthode `delegateAlertToProjectManager()` mise à jour
- [x] Création de notification in-app persistante
- [x] Notification WebSocket envoyée
- [x] Endpoint `/delegated-alerts` créé
- [x] Repository method ajoutée
- [x] Service method `getDelegatedAlerts()` ajoutée

### Frontend
- [ ] Abonnement WebSocket aux alertes déléguées
- [ ] Affichage dans le panneau de notifications
- [ ] Badge de compteur mis à jour
- [ ] Redirection vers détails de l'alerte
- [ ] Marquer comme lu fonctionnel

### Tests
- [ ] Test délégation simple
- [ ] Test notifications multiples
- [ ] Test temps réel
- [ ] Test marquer comme lu
- [ ] Test API endpoints

---

## 🚀 PROCHAINES ÉTAPES

### Sprint 3 (Actuel)
1. ✅ Backend notifications déléguées - **TERMINÉ**
2. 🟡 Frontend intégration WebSocket - **EN COURS**
3. 🟡 Tests end-to-end - **EN COURS**

### Sprint 4 (Prochain)
- Historique des délégations pour le Décideur
- Statistiques de traitement des alertes
- Notifications par email (optionnel)
- Notifications SMS pour alertes critiques

---

## 📝 NOTES TECHNIQUES

### Modèle de Notification

```java
NotificationLog {
  String id;
  String recipientId;
  String type;              // "ALERT_DELEGATED"
  String title;             // "🔄 Alerte KPI Déléguée"
  String message;           // Message détaillé
  String status;            // "UNREAD" / "READ"
  String priority;          // "HIGH" / "MEDIUM" / "LOW"
  String category;          // "KPI_ALERT"
  String link;              // "/project-manager/alerts/{id}"
  String source;            // "DECISION_MAKER"
  LocalDateTime createdAt;
  LocalDateTime readAt;
  boolean deleted;
}
```

### Sécurité

- Endpoint protégé par `@PreAuthorize("hasRole('PROJECT_MANAGER')")`
- Vérification userId dans les requêtes
- Soft delete des notifications
- Validation des permissions côté backend

---

**Statut:** ✅ **IMPLÉMENTATION TERMINÉE**  
**Date:** 30 Octobre 2025  
**Version:** 1.0  
**Prochaine étape:** Tests et intégration frontend
