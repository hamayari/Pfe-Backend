# Patch pour Activer les Notifications du Chef de Projet

## Problème
Le Chef de Projet ne voit pas les alertes déléguées par le Décideur car le code de chargement des notifications n'est pas implémenté.

## Solution

Ajouter les méthodes suivantes dans `project-manager-dashboard.component.ts` :

### 1. Dans ngOnInit(), ajouter après `this.loadTeamMembers()` :

```typescript
// Charger les notifications déléguées
this.loadDelegatedNotifications();
this.loadNotificationCount();
this.startNotificationRefresh();
```

### 2. Ajouter ces méthodes privées dans la classe :

```typescript
/**
 * Charger les notifications déléguées depuis le backend
 */
private loadDelegatedNotifications(): void {
  console.log('📥 [NOTIFICATIONS] Chargement des alertes déléguées...');
  
  this.notificationService.getDelegatedAlerts()
    .pipe(
      takeUntil(this.destroy$),
      catchError(error => {
        console.error('❌ Erreur chargement alertes déléguées:', error);
        return of([]);
      })
    )
    .subscribe((notifications: Notification[]) => {
      console.log(`✅ ${notifications.length} alerte(s) déléguée(s) chargée(s)`);
      
      // Ajouter chaque notification au panneau
      notifications.forEach(notif => {
        this.addNotificationToPanel({
          id: notif.id || Date.now().toString(),
          type: 'kpi_alert',
          title: notif.title,
          message: notif.message,
          timestamp: new Date(notif.timestamp),
          read: notif.read,
          priority: notif.priority === 'HIGH' ? 'high' : 'medium',
          data: notif
        });
      });
      
      this.updateNotificationCount();
    });
}

/**
 * Charger le compteur de notifications non lues
 */
private loadNotificationCount(): void {
  this.notificationService.getUnreadCount()
    .pipe(
      takeUntil(this.destroy$),
      catchError(error => {
        console.error('❌ Erreur chargement compteur notifications:', error);
        return of(0);
      })
    )
    .subscribe((count: number) => {
      this.notificationCount = count;
      this.hasNotifications = count > 0;
      console.log(`📊 Compteur notifications: ${count}`);
    });
}

/**
 * Ajouter une notification au panneau
 */
private addNotificationToPanel(notification: any): void {
  const exists = this.realtimeNotifications.some(n => n.id === notification.id);
  if (!exists) {
    this.realtimeNotifications.unshift(notification);
    this.updateNotificationCount();
    console.log('➕ Notification ajoutée au panneau:', notification.title);
  }
}

/**
 * Ajouter une alerte KPI à la liste
 */
private addKpiAlertToList(alert: any): void {
  const exists = this.alerts.some(a => a.id === alert.id);
  if (!exists) {
    const monitoringAlert: MonitoringAlert = {
      id: alert.id || alert.alertId || Date.now().toString(),
      type: alert.severity === 'HIGH' ? 'error' : 'warning',
      message: alert.message,
      timestamp: new Date(alert.timestamp || Date.now()),
      acknowledged: false,
      source: 'kpi-system'
    };
    
    this.alerts.unshift(monitoringAlert);
    this.stats.pendingAlerts = this.alerts.filter(a => !a.acknowledged).length;
    this.filterAlerts();
    
    console.log('➕ Alerte KPI ajoutée à la liste');
  }
}

/**
 * Rafraîchir les notifications périodiquement
 */
private startNotificationRefresh(): void {
  timer(30000, 30000)
    .pipe(takeUntil(this.destroy$))
    .subscribe(() => {
      console.log('🔄 Rafraîchissement automatique des notifications...');
      this.loadDelegatedNotifications();
      this.loadNotificationCount();
    });
}

/**
 * Gérer le clic sur une notification
 */
onNotificationClicked(notification: any): void {
  console.log('🔔 Clic sur notification:', notification);
  
  if (notification.id && !notification.read) {
    this.notificationService.markAsRead(notification.id)
      .pipe(
        takeUntil(this.destroy$),
        catchError(error => {
          console.error('❌ Erreur marquage notification:', error);
          return of(null);
        })
      )
      .subscribe(() => {
        notification.read = true;
        this.updateNotificationCount();
      });
  }
  
  if (notification.type === 'kpi_alert' || notification.type === 'ALERT_DELEGATED') {
    this.navigateToSection('kpi-alerts');
  } else if (notification.type === 'invoice_alert') {
    this.navigateToSection('invoices');
  }
}
```

## Test

1. Décideur délègue une alerte
2. Chef de Projet se connecte
3. Badge de notification apparaît avec le compteur
4. Clic sur l'icône de notification affiche les alertes déléguées
5. Clic sur une alerte navigue vers la section KPI

## Résultat Attendu

✅ Chef de Projet voit les notifications déléguées
✅ Compteur mis à jour automatiquement
✅ Rafraîchissement toutes les 30 secondes
✅ WebSocket temps réel fonctionnel
