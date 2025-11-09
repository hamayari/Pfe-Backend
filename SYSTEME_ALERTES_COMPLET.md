# 🔔 Système d'Alertes et Notifications - Spécification Complète

## 📊 ARCHITECTURE ACTUELLE

### 1. Détection Automatique (✅ FONCTIONNE)
- **Scheduler:** `AlertScheduler.java` - Toutes les 5 minutes
- **Service:** `InvoiceAlertService.java` - Détecte les factures OVERDUE
- **Règle:** 1 facture OVERDUE = 1 alerte KPI
- **Stockage:** MongoDB collection `kpi_alerts`

### 2. Affichage pour le Décideur (✅ FONCTIONNE)
- **Composant:** `kpi-alerts.component.ts`
- **Route:** `/decideur` → Section "Gestion Alertes KPI"
- **Affichage:** Liste des alertes avec détails

### 3. Délégation au Chef de Projet (⚠️ PARTIELLEMENT FONCTIONNEL)
- **Bouton:** "Envoyer au Chef de Projet" ✅
- **Endpoint:** `POST /api/kpi-alerts/{alertId}/delegate-to-pm` ✅
- **Backend:** Change les destinataires ✅
- **Frontend:** Retire l'alerte du panel ✅
- **Notification:** ❌ PAS ENCORE IMPLÉMENTÉ

---

## ❌ PROBLÈME ACTUEL

Le Chef de Projet ne reçoit **AUCUNE notification** car :

1. L'alerte est déléguée dans MongoDB
2. Les destinataires sont mis à jour
3. **MAIS** le système de notifications in-app ne récupère pas les alertes déléguées

---

## ✅ SOLUTION REQUISE

### Étape 1: Créer une notification in-app pour le Chef de Projet

Quand le Décideur délègue une alerte, créer une notification dans la collection `notifications` :

```java
// Dans AutomaticKpiAlertService.delegateAlertToProjectManager()

// Créer une notification in-app
if (notificationService != null) {
    for (User pm : projectManagers) {
        notificationService.createNotification(
            pm.getId(),
            "ALERT_DELEGATED",
            "Nouvelle alerte déléguée",
            "🔄 " + alert.getMessage(),
            "/project-manager/alerts/" + alertId,
            "DECISION_MAKER"
        );
    }
}
```

### Étape 2: Le Chef de Projet récupère ses notifications

Le composant de notifications du Chef de Projet doit :
1. Récupérer les notifications de type `ALERT_DELEGATED`
2. Les afficher dans le panel de notifications
3. Permettre de cliquer pour voir les détails

### Étape 3: Historique pour le Décideur

L'alerte déléguée doit :
- ✅ Disparaître du panel actif
- ✅ Rester dans l'historique avec statut "Délégué"
- ✅ Afficher la date de délégation

---

## 🎯 IMPLÉMENTATION

### Backend (Java)

**Fichier:** `AutomaticKpiAlertService.java`

```java
public boolean delegateAlertToProjectManager(String alertId) {
    try {
        Optional<KpiAlert> alertOpt = kpiAlertRepository.findById(alertId);
        if (alertOpt.isEmpty()) {
            return false;
        }
        
        KpiAlert alert = alertOpt.get();
        
        // 1. Mettre à jour les destinataires
        List<User> projectManagers = userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER);
        List<String> pmIds = new ArrayList<>();
        for (User pm : projectManagers) {
            pmIds.add(pm.getId());
        }
        alert.setRecipients(pmIds);
        
        // 2. Marquer comme délégué
        alert.setMessage("🔄 [Délégué par le Décideur] " + alert.getMessage());
        
        // 3. Sauvegarder
        kpiAlertRepository.save(alert);
        
        // 4. Créer une notification pour chaque Chef de Projet
        if (notificationService != null) {
            for (User pm : projectManagers) {
                notificationService.createNotification(
                    pm.getId(),
                    "ALERT_DELEGATED",
                    "Nouvelle alerte déléguée",
                    alert.getMessage(),
                    "/project-manager/alerts",
                    "DECISION_MAKER"
                );
            }
        }
        
        // 5. Envoyer notification WebSocket
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ALERT_DELEGATED");
        notification.put("alertId", alertId);
        notification.put("message", "Nouvelle alerte déléguée par le Décideur");
        
        messagingTemplate.convertAndSend("/topic/kpi-alerts", notification);
        
        return true;
        
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
```

### Frontend (TypeScript)

**Fichier:** `notification.service.ts`

```typescript
getNotifications(): Observable<Notification[]> {
  return this.http.get<Notification[]>(
    `${this.apiUrl}/notifications/user/${this.currentUserId}`
  );
}

markAsRead(notificationId: string): Observable<any> {
  return this.http.put(
    `${this.apiUrl}/notifications/${notificationId}/read`,
    {}
  );
}
```

**Fichier:** `project-manager-dashboard.component.ts`

```typescript
loadNotifications() {
  this.notificationService.getNotifications().subscribe({
    next: (notifications) => {
      this.notifications = notifications;
      this.notificationCount = notifications.filter(n => !n.read).length;
    }
  });
}
```

---

## 📋 CHECKLIST D'IMPLÉMENTATION

### Backend
- [x] Endpoint de délégation créé
- [x] Changement des destinataires
- [ ] **Création de notification in-app**
- [ ] **Notification WebSocket**
- [ ] Historique de traçabilité

### Frontend
- [x] Bouton "Envoyer au Chef de Projet"
- [x] Appel API de délégation
- [x] Retrait de l'alerte du panel
- [ ] **Affichage dans notifications du Chef de Projet**
- [ ] **Historique pour le Décideur**

---

## 🧪 TEST COMPLET

### Scénario 1: Délégation d'alerte

1. **Décideur:**
   - Se connecte
   - Va sur "Gestion Alertes KPI"
   - Voit 7 alertes
   - Clique "Envoyer au Chef de Projet" sur une alerte
   - ✅ L'alerte disparaît (reste 6 alertes)
   - ✅ L'alerte reste dans l'historique

2. **Chef de Projet:**
   - Se connecte (autre navigateur)
   - ✅ Badge de notification (1)
   - Clique sur l'icône de notifications
   - ✅ Voit "Nouvelle alerte déléguée"
   - Clique sur la notification
   - ✅ Redirigé vers les détails de l'alerte

### Scénario 2: Traçabilité

1. **Décideur:**
   - Va dans "Historique"
   - ✅ Voit toutes les alertes déléguées
   - ✅ Voit la date de délégation
   - ✅ Voit à qui l'alerte a été envoyée

---

## 🚀 PROCHAINES ÉTAPES

1. **Implémenter la création de notification in-app** dans `delegateAlertToProjectManager()`
2. **Créer le composant d'historique** pour le Décideur
3. **Tester le flux complet** de bout en bout

---

**Le système est presque complet ! Il ne manque que la création de la notification in-app.** 🎯
