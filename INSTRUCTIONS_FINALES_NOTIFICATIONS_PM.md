# 🔧 Instructions Finales - Notifications Chef de Projet

## ❌ Problème Actuel

Le fichier `project-manager-dashboard.component.ts` est corrompu avec du code dupliqué en dehors de la classe.

## ✅ Solution Manuelle

### Étape 1: Nettoyer le fichier

1. Ouvrir `demo/app-frontend-new/src/app/dashboard/project-manager-dashboard/project-manager-dashboard.component.ts`
2. Chercher la ligne qui contient la **première** fermeture de classe : `}` (vers la ligne 3127)
3. **Supprimer tout le code après cette ligne** jusqu'à la fin du fichier

### Étape 2: Ajouter les méthodes manquantes

**AVANT la dernière accolade `}` de la classe**, ajouter ces méthodes :

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
        type: alert.severity === 'HIGH' ? 'critical' : 'warning',
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
   * Afficher une alerte facture
   */
  private showInvoiceAlert(alert: any): void {
    this.addNotificationToPanel({
      id: alert.id || Date.now().toString(),
      type: 'invoice_alert',
      title: `Alerte Facture: ${alert.invoiceNumber || 'N/A'}`,
      message: alert.message,
      timestamp: new Date(),
      read: false,
      priority: 'medium',
      data: alert
    });
    
    this.snackBar.open(
      `💰 ALERTE FACTURE: ${alert.message}`,
      'Voir',
      {
        duration: 8000,
        panelClass: 'alert-warning',
        horizontalPosition: 'end',
        verticalPosition: 'top'
      }
    ).onAction().subscribe(() => {
      this.navigateToSection('invoices');
    });
  }
```

### Étape 3: Modifier ngOnInit()

Dans la méthode `ngOnInit()`, **après** `this.loadTeamMembers()`, ajouter :

```typescript
    // Charger les notifications déléguées
    console.log('📥 [PROJECT MANAGER] Chargement des notifications...');
    this.loadDelegatedNotifications();
    this.loadNotificationCount();
    this.startNotificationRefresh();
```

### Étape 4: Vérifier

1. Sauvegarder le fichier
2. Vérifier qu'il n'y a plus d'erreurs TypeScript
3. Le fichier doit se terminer par une seule accolade `}`

## 🎯 Résultat Attendu

Une fois corrigé :
- ✅ Aucune erreur de compilation
- ✅ Le Chef de Projet charge ses notifications au démarrage
- ✅ Rafraîchissement automatique toutes les 30 secondes
- ✅ Badge de notification fonctionnel

## 📝 Note

Si le problème persiste, il est recommandé de restaurer une version propre du fichier depuis le contrôle de version (git) et de réappliquer uniquement les modifications nécessaires.
