# 📤 Guide de Délégation d'Alertes

## ✅ BACKEND COMPLÉTÉ

### Endpoint créé
```
POST /api/kpi-alerts/{alertId}/delegate-to-pm
```

**Fonctionnalité:**
- Change le statut de l'alerte à `DELEGATED_TO_PM`
- Met à jour les destinataires (seulement Chefs de Projet)
- Crée une notification pour le Chef de Projet
- Envoie une notification WebSocket

**Code:**
- `KpiAlertController.java` - Endpoint de délégation
- `AutomaticKpiAlertService.java` - Méthode `delegateAlertToProjectManager()`

---

## ❌ FRONTEND À COMPLÉTER

### 1. Trouver le composant qui affiche les alertes

Le bouton "Envoyer au Chef de Projet" doit être ajouté dans le panel d'alertes du Décideur.

**Fichiers à vérifier:**
- `decision-maker-dashboard.component.html`
- `decision-maker-dashboard.component.ts`
- Ou un composant d'alertes séparé

### 2. Ajouter le bouton dans le template HTML

```html
<button 
  mat-raised-button 
  color="warn"
  (click)="delegateToProjectManager(alert.id)"
  *ngIf="currentUserRole === 'DECISION_MAKER'">
  📤 Envoyer au Chef de Projet
</button>
```

### 3. Ajouter la méthode dans le TypeScript

```typescript
delegateToProjectManager(alertId: string) {
  this.http.post(`${this.apiUrl}/kpi-alerts/${alertId}/delegate-to-pm`, {})
    .subscribe({
      next: (response: any) => {
        console.log('✅ Alerte déléguée:', response);
        
        // Retirer l'alerte du panel du Décideur
        this.alerts = this.alerts.filter(a => a.id !== alertId);
        
        // Afficher un message de succès
        this.snackBar.open('Alerte envoyée au Chef de Projet', 'OK', {
          duration: 3000
        });
        
        // Rafraîchir les alertes
        this.loadAlerts();
      },
      error: (err) => {
        console.error('❌ Erreur délégation:', err);
        this.snackBar.open('Erreur lors de l\'envoi', 'OK', {
          duration: 3000
        });
      }
    });
}
```

### 4. Vérifier que le Chef de Projet reçoit la notification

Le Chef de Projet doit voir l'alerte dans son panel de notifications.

**Vérifications:**
1. Le Chef de Projet est connecté
2. Le WebSocket fonctionne
3. Le service de notifications est actif

---

## 🧪 TEST

### Étape 1: Décideur envoie l'alerte
1. Connecte-toi en tant que Décideur
2. Va sur le dashboard
3. Clique sur "Envoyer au Chef de Projet"
4. **Résultat:** L'alerte disparaît du panel

### Étape 2: Chef de Projet reçoit l'alerte
1. Connecte-toi en tant que Chef de Projet (autre navigateur)
2. Va sur le dashboard
3. Clique sur l'icône de notifications (🔔)
4. **Résultat:** L'alerte apparaît dans les notifications

---

## 🔍 DÉBOGAGE

### Si l'alerte ne disparaît pas du panel du Décideur:
- Vérifier que la méthode `delegateToProjectManager()` est appelée
- Vérifier les logs de la console (F12)
- Vérifier que l'endpoint retourne un succès (200)

### Si le Chef de Projet ne reçoit pas la notification:
- Vérifier que le WebSocket est connecté
- Vérifier les logs backend pour voir si la notification est envoyée
- Vérifier que le Chef de Projet est dans la liste des destinataires

---

## 📋 PROCHAINES ÉTAPES

1. **Localiser** le composant qui affiche les alertes du Décideur
2. **Ajouter** le bouton "Envoyer au Chef de Projet"
3. **Implémenter** la méthode `delegateToProjectManager()`
4. **Tester** le flux complet

---

**Le backend est prêt ! Il ne reste plus qu'à ajouter le bouton dans le frontend.** 🚀
