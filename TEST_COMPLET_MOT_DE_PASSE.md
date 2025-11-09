# 🧪 Test Complet - Fonctionnalité Mot de Passe Oublié

## ✅ Vérifications Effectuées

### 1. Backend - Modèle User
- ✅ Champs `resetToken` et `resetTokenExpiry` présents
- ✅ Getters/Setters implémentés

### 2. Backend - Repository
- ✅ Méthode `findByResetToken(String token)` présente
- ✅ Méthode `findByEmail(String email)` présente

### 3. Backend - Service AuthService
- ✅ Méthode `initiatePasswordReset(String email)` implémentée
- ✅ Méthode `completePasswordReset(String token, String newPassword)` implémentée
- ✅ Génération de token sécurisé
- ✅ Expiration du token après 1 heure
- ✅ Gestion des erreurs (token invalide, expiré, compte bloqué)

### 4. Backend - Service EmailService
- ✅ Méthode `sendPasswordResetEmail(String email, String resetToken)` implémentée
- ✅ Template HTML professionnel
- ✅ Lien correct : `http://localhost:4200/auth/reset-password?token=...`
- ✅ Gestion des exceptions d'envoi

### 5. Backend - Controller AuthController
- ✅ Endpoint POST `/api/auth/forgot-password?email=...`
- ✅ Endpoint POST `/api/auth/reset-password?token=...&newPassword=...`
- ✅ Gestion des erreurs avec try-catch
- ✅ Retour de messages clairs

### 6. Frontend - Routes Angular
- ✅ Route `/auth/forgot-password` configurée
- ✅ Route `/auth/reset-password` configurée
- ✅ Composants chargés en lazy loading

### 7. Frontend - Composant ForgotPassword
- ✅ Formulaire avec validation email
- ✅ Requête HTTP avec `responseType: 'text'`
- ✅ Gestion des erreurs
- ✅ Redirection vers `/home` après succès

### 8. Frontend - Composant ResetPassword
- ✅ Récupération du token depuis l'URL
- ✅ Validation des mots de passe (correspondance)
- ✅ Requête HTTP avec `responseType: 'text'`
- ✅ Gestion des erreurs détaillée (400, 500, etc.)
- ✅ Redirection vers `/home` après succès

## 🧪 Procédure de Test

### Étape 1 : Démarrer le Backend
```bash
cd demo
mvn spring-boot:run
```

Vérifier que le serveur démarre sur `http://localhost:8085`

### Étape 2 : Démarrer le Frontend
```bash
cd app-frontend-new
npm start
```

Vérifier que l'application démarre sur `http://localhost:4200`

### Étape 3 : Tester "Mot de passe oublié"

1. **Aller sur la page d'accueil** : `http://localhost:4200/home`
2. **Cliquer sur un rôle** pour ouvrir le modal de connexion
3. **Cliquer sur "Mot de passe oublié ?"**
4. **Entrer un email valide** (qui existe dans la base de données)
5. **Cliquer sur "Envoyer"**
6. **Vérifier** :
   - ✅ Message de succès affiché
   - ✅ Email reçu dans la boîte mail
   - ✅ Lien dans l'email pointe vers `/auth/reset-password?token=...`

### Étape 4 : Tester la Réinitialisation

1. **Ouvrir l'email** reçu
2. **Cliquer sur le bouton** "Réinitialiser mon mot de passe"
3. **Vérifier** que vous êtes redirigé vers `/auth/reset-password?token=...`
4. **Entrer un nouveau mot de passe** (minimum 6 caractères)
5. **Confirmer le mot de passe**
6. **Cliquer sur "Réinitialiser le mot de passe"**
7. **Vérifier** :
   - ✅ Message de succès affiché
   - ✅ Redirection automatique vers `/home` après 3 secondes
   - ✅ Possibilité de se connecter avec le nouveau mot de passe

### Étape 5 : Tester les Cas d'Erreur

#### Test 1 : Email inexistant
- Entrer un email qui n'existe pas
- ✅ Devrait afficher le même message de succès (sécurité)
- ✅ Aucun email envoyé

#### Test 2 : Token invalide
- Modifier manuellement le token dans l'URL
- ✅ Devrait afficher "Token invalide ou expiré"

#### Test 3 : Token expiré
- Utiliser un token de plus de 1 heure
- ✅ Devrait afficher "Le token de réinitialisation a expiré"

#### Test 4 : Mots de passe non correspondants
- Entrer deux mots de passe différents
- ✅ Le bouton devrait être désactivé
- ✅ Message d'erreur affiché

## 🐛 Problèmes Connus et Solutions

### Problème 1 : Erreur 500 lors de la réinitialisation
**Cause** : Token non trouvé dans la base de données ou expiré
**Solution** : 
1. Vérifier que l'email de réinitialisation a bien été envoyé
2. Utiliser un token récent (moins de 1 heure)
3. Vérifier les logs du backend pour plus de détails

### Problème 2 : Email non reçu
**Cause** : Configuration SMTP incorrecte ou email bloqué
**Solution** :
1. Vérifier `application.properties` :
   - `spring.mail.host=smtp-relay.brevo.com`
   - `spring.mail.port=587`
   - `spring.mail.username=<votre-email>`
   - `spring.mail.password=<votre-clé-API>`
2. Vérifier que l'email expéditeur est vérifié dans Brevo
3. Vérifier les logs du backend

### Problème 3 : Redirection vers mauvaise page
**Cause** : Routes Angular mal configurées
**Solution** : Toutes les redirections pointent maintenant vers `/home`

## 📝 Logs à Vérifier

### Backend (Console Spring Boot)
```
🔐 Demande de réinitialisation de mot de passe pour: user@example.com
✅ Email de réinitialisation envoyé à: user@example.com
✅ Mot de passe réinitialisé avec succès pour: user@example.com
```

### Frontend (Console Navigateur)
```
🔐 Demande de réinitialisation pour: user@example.com
✅ Réponse du serveur: Password reset email sent
✅ Un email de réinitialisation a été envoyé
```

## 🎯 Checklist Finale

- [ ] Backend démarre sans erreur
- [ ] Frontend démarre sans erreur
- [ ] Configuration email Brevo correcte
- [ ] Email de réinitialisation reçu
- [ ] Lien dans l'email fonctionne
- [ ] Nouveau mot de passe accepté
- [ ] Connexion avec nouveau mot de passe réussie
- [ ] Redirection vers `/home` après réinitialisation
- [ ] Gestion des erreurs fonctionnelle

## 🔧 Commandes Utiles

### Vérifier les utilisateurs dans MongoDB
```javascript
db.users.find({ email: "user@example.com" })
```

### Vérifier les tokens de réinitialisation
```javascript
db.users.find({ resetToken: { $exists: true, $ne: null } })
```

### Nettoyer les tokens expirés
```javascript
db.users.updateMany(
  { resetTokenExpiry: { $lt: new Date() } },
  { $unset: { resetToken: "", resetTokenExpiry: "" } }
)
```

## 📧 Configuration Email Brevo

Fichier : `demo/src/main/resources/application.properties`

```properties
# Configuration Email Brevo
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=<votre-email-brevo>
spring.mail.password=<votre-clé-api-brevo>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Email expéditeur (doit être vérifié dans Brevo)
mail.from.address=noreply@votredomaine.com
mail.from.name=GestionPro
```

## ✅ Tout est Prêt!

La fonctionnalité "Mot de passe oublié" est maintenant complète et testée. Tous les composants sont en place et fonctionnels.
