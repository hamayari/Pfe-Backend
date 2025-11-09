# Test Manuel du Scheduler de Notifications

## Méthode 1 : Via API REST (Recommandé)

Si vous avez créé un endpoint de test pour le scheduler :

```bash
# Déclencher manuellement le scheduler
POST http://localhost:8085/api/test/scheduler/trigger
Headers: Authorization: Bearer <votre_token>
```

## Méthode 2 : Modifier le Cron (Temporaire)

1. Ouvrez le fichier :
   `src/main/java/com/example/demo/service/NotificationSchedulerService.java`

2. Trouvez la ligne :
   ```java
   @Scheduled(cron = "0 0 9 * * *") // Tous les jours à 9h00
   ```

3. Remplacez par (exécution toutes les 2 minutes) :
   ```java
   @Scheduled(fixedRate = 120000) // Toutes les 2 minutes
   ```

4. Redémarrez le backend

5. Attendez 2 minutes et vérifiez les logs

6. **IMPORTANT** : Remettez le cron original après le test !

## Méthode 3 : Attendre 9h00 le Lendemain

1. Assurez-vous qu'une facture a une échéance dans 7, 3 ou 1 jour(s)

2. Le lendemain à 9h00, le scheduler s'exécutera automatiquement

3. Vérifiez les logs :
   ```
   🔔 [SCHEDULER] Début de la vérification des échéances - 2025-10-16
   📧 Envoi de rappel pour facture [ID]
   ✅ Rappel automatique envoyé
   ```

4. Vérifiez vos notifications (email, SMS, WebSocket)

## Résultat Attendu

Pour chaque facture éligible :
- ✅ Email envoyé
- ✅ SMS envoyé
- ✅ Notification WebSocket créée
- ✅ Log dans la console du serveur
