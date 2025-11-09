# 🧹 GUIDE - Nettoyage des Notifications

## 🚨 PROBLÈMES IDENTIFIÉS

### 1. Trop de notifications (402)
**Cause:** Accumulation de notifications au fil du temps, possibles doublons

**Impact:**
- Performance dégradée
- Temps de chargement lent
- Badge avec nombre élevé

### 2. Heure incorrecte ("Il y a 9h")
**Cause:** Problème de fuseau horaire entre backend et frontend

**Impact:**
- Affichage incorrect du temps relatif
- Confusion pour l'utilisateur

---

## ✅ SOLUTIONS IMPLÉMENTÉES

### 1. Correction du fuseau horaire (Frontend)

**Fichier:** `notification-panel.component.ts`

```typescript
getTimeAgo(timestamp: Date): string {
  const now = new Date();
  const notifDate = new Date(timestamp);
  
  // Calculer la différence
  const diff = now.getTime() - notifDate.getTime();
  
  // Détecter les problèmes de timezone
  if (diff < 0) {
    console.warn('⚠️ Notification dans le futur:', timestamp);
    return 'À l\'instant';
  }
  
  const seconds = Math.floor(Math.abs(diff) / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 60) return 'À l\'instant';
  if (minutes < 60) return `Il y a ${minutes} min`;
  if (hours < 24) return `Il y a ${hours}h`;
  if (days < 7) return `Il y a ${days}j`;
  return notifDate.toLocaleDateString('fr-FR');
}
```

### 2. Filtrage des doublons (Frontend)

**Fichier:** `notification-panel.component.ts`

```typescript
loadNotifications(): void {
  this.notificationService.getNotifications()
    .subscribe({
      next: (notifications) => {
        // Filtrer les doublons par ID
        const uniqueNotifications = notifications.filter((notif, index, self) =>
          index === self.findIndex((n) => n.id === notif.id)
        );
        
        // Trier par date et limiter à 10
        this.notifications = uniqueNotifications
          .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
          .slice(0, 10);
        
        // Avertir si trop de notifications
        if (notifications.length > 50) {
          console.warn('⚠️ Trop de notifications:', notifications.length);
        }
      }
    });
}
```

### 3. Limitation côté backend

**Fichier:** `NotificationController.java`

```java
@GetMapping("/user/{userId}")
public ResponseEntity<List<Notification>> getUserNotifications(
        @PathVariable String userId,
        @RequestParam(defaultValue = "50") int limit) {
    
    List<Notification> notifications = notificationService.getUserNotifications(userId);
    
    // Limiter à 100 maximum
    List<Notification> limitedNotifications = notifications.stream()
        .limit(Math.min(limit, 100))
        .collect(Collectors.toList());
    
    if (notifications.size() > 100) {
        System.out.println("⚠️ " + notifications.size() + " notifications - Nettoyage recommandé!");
    }
    
    return ResponseEntity.ok(limitedNotifications);
}
```

### 4. Endpoint de nettoyage (Admin)

**Nouveau endpoint:** `DELETE /api/notifications/cleanup`

```java
@DeleteMapping("/cleanup")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Map<String, Object>> cleanupOldNotifications(
        @RequestParam(defaultValue = "30") int daysOld) {
    
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
    
    long totalBefore = notificationService.countAllNotifications();
    int deleted = notificationService.deleteOldReadNotifications(cutoffDate);
    long totalAfter = notificationService.countAllNotifications();
    
    Map<String, Object> response = new HashMap<>();
    response.put("deletedCount", deleted);
    response.put("totalBefore", totalBefore);
    response.put("totalAfter", totalAfter);
    
    return ResponseEntity.ok(response);
}
```

---

## 🛠️ MÉTHODES DE NETTOYAGE

### Méthode 1: Script PowerShell (Recommandé)

```powershell
# Exécuter le script
cd demo
.\clean-duplicate-notifications.ps1
```

**Ce que fait le script:**
1. Compte le total de notifications
2. Identifie les doublons (même userId, type, titre, message)
3. Garde la plus récente de chaque groupe
4. Supprime les autres
5. Affiche le nouveau total

### Méthode 2: API REST (Pour Admin)

```bash
# Nettoyer les notifications de plus de 30 jours
curl -X DELETE "http://localhost:8080/api/notifications/cleanup?daysOld=30" \
  -H "Authorization: Bearer {admin_token}"

# Réponse
{
  "success": true,
  "deletedCount": 350,
  "totalBefore": 402,
  "totalAfter": 52,
  "cutoffDate": "2024-09-30T14:30:00"
}
```

### Méthode 3: MongoDB Direct

```javascript
// Se connecter à MongoDB
mongosh mongodb://localhost:27017/commercial_pfe

// Compter les notifications
db.notifications.countDocuments()

// Supprimer les notifications lues de plus de 30 jours
db.notifications.deleteMany({
  read: true,
  timestamp: { $lt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) }
})

// Supprimer les doublons (garder la plus récente)
db.notifications.aggregate([
  {
    $group: {
      _id: {
        userId: "$userId",
        type: "$type",
        title: "$title"
      },
      ids: { $push: "$_id" },
      count: { $sum: 1 }
    }
  },
  {
    $match: { count: { $gt: 1 } }
  }
]).forEach(function(doc) {
  var notifs = db.notifications.find({
    _id: { $in: doc.ids }
  }).sort({ timestamp: -1 }).toArray();
  
  for (var i = 1; i < notifs.length; i++) {
    db.notifications.deleteOne({ _id: notifs[i]._id });
  }
});
```

---

## 📊 STRATÉGIE DE MAINTENANCE

### Nettoyage Automatique (À implémenter)

**Créer un scheduler Spring:**

```java
@Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
public void cleanupOldNotifications() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
    int deleted = notificationService.deleteOldReadNotifications(cutoffDate);
    logger.info("🧹 Nettoyage automatique: {} notifications supprimées", deleted);
}
```

### Limites Recommandées

| Type | Limite | Raison |
|------|--------|--------|
| Affichage panneau | 10 | UX optimale |
| API par défaut | 50 | Performance |
| API maximum | 100 | Sécurité |
| Conservation | 30 jours | Conformité RGPD |

### Bonnes Pratiques

1. **Éviter les doublons**
   - Vérifier avant de créer une notification
   - Utiliser un identifiant unique (hash du contenu)

2. **Nettoyer régulièrement**
   - Notifications lues > 30 jours
   - Notifications supprimées (soft delete) > 90 jours

3. **Monitorer**
   - Alerter si > 100 notifications par utilisateur
   - Logger les créations massives

4. **Optimiser**
   - Index sur `userId`, `timestamp`, `read`
   - Pagination côté backend

---

## 🔍 DIAGNOSTIC

### Vérifier le nombre de notifications

```bash
# Backend logs
grep "Retour de" logs/application.log | tail -20

# MongoDB
mongosh mongodb://localhost:27017/commercial_pfe --eval "db.notifications.countDocuments()"

# Par utilisateur
mongosh mongodb://localhost:27017/commercial_pfe --eval "
  db.notifications.aggregate([
    { \$group: { _id: '\$userId', count: { \$sum: 1 } } },
    { \$sort: { count: -1 } }
  ])
"
```

### Identifier les doublons

```javascript
// Trouver les doublons
db.notifications.aggregate([
  {
    $group: {
      _id: {
        userId: "$userId",
        type: "$type",
        title: "$title",
        message: "$message"
      },
      count: { $sum: 1 },
      ids: { $push: "$_id" }
    }
  },
  {
    $match: { count: { $gt: 1 } }
  },
  {
    $sort: { count: -1 }
  }
])
```

---

## ✅ CHECKLIST DE NETTOYAGE

- [ ] Sauvegarder la base de données
- [ ] Compter le total de notifications
- [ ] Identifier les doublons
- [ ] Exécuter le script de nettoyage
- [ ] Vérifier le nouveau total
- [ ] Tester l'affichage dans l'UI
- [ ] Vérifier les timestamps
- [ ] Redémarrer l'application si nécessaire

---

## 🚀 RÉSULTAT ATTENDU

**Avant:**
- 402 notifications
- "Il y a 9h" (incorrect)
- Chargement lent

**Après:**
- ~50 notifications (récentes uniquement)
- "À l'instant" / "Il y a 2 min" (correct)
- Chargement rapide

---

**Date:** 30 Octobre 2025  
**Version:** 1.0  
**Statut:** ✅ Solutions implémentées
