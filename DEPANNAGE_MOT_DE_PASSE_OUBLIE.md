# 🔧 Dépannage - Mot de Passe Oublié

## ❌ Erreur: "Erreur lors de l'envoi de l'email"

### Causes Possibles

1. **L'email n'existe pas dans la base de données**
2. **Le backend n'est pas démarré**
3. **Problème de connexion à MongoDB**
4. **Configuration email incorrecte**

---

## 🔍 Diagnostic

### Étape 1: Vérifier que le Backend est Démarré

```bash
# Vérifier si le backend tourne sur le port 8085
curl http://localhost:8085/api/auth/forgot-password?email=test@test.com
```

**Résultat attendu:** Une réponse (même une erreur 404 est OK)

**Si erreur de connexion:** Le backend n'est pas démarré
```bash
cd demo
mvn spring-boot:run
```

---

### Étape 2: Vérifier l'Email dans MongoDB

**Option A: Via MongoDB Compass**

1. Ouvrir MongoDB Compass
2. Se connecter à `mongodb://localhost:27017`
3. Sélectionner la base `demo_db`
4. Collection `users`
5. Chercher votre email

**Option B: Via Script**

```bash
mongo demo_db < check-user-email.js
```

**Option C: Via Commande Directe**

```javascript
use demo_db;
db.users.find({ email: "eyayari123@gmail.com" });
```

**Si aucun résultat:** L'email n'existe pas dans la base

---

### Étape 3: Lister Tous les Emails Disponibles

```javascript
use demo_db;
db.users.find({}, { email: 1, username: 1 }).pretty();
```

**Résultat attendu:**
```javascript
{ "_id": "...", "username": "admin", "email": "admin@gestionpro.com" }
{ "_id": "...", "username": "commercial", "email": "commercial@gestionpro.com" }
{ "_id": "...", "username": "projectmanager", "email": "pm@gestionpro.com" }
{ "_id": "...", "username": "decisionmaker", "email": "dm@gestionpro.com" }
```

---

## ✅ Solutions

### Solution 1: Utiliser un Email Existant

Utilisez un des emails par défaut:
- `admin@gestionpro.com`
- `commercial@gestionpro.com`
- `pm@gestionpro.com`
- `dm@gestionpro.com`

---

### Solution 2: Ajouter l'Email à un Utilisateur Existant

```javascript
use demo_db;

// Mettre à jour l'email d'un utilisateur
db.users.updateOne(
  { username: "admin" },
  { $set: { email: "eyayari123@gmail.com" } }
);

// Vérifier
db.users.findOne({ email: "eyayari123@gmail.com" });
```

---

### Solution 3: Créer un Nouvel Utilisateur avec cet Email

```javascript
use demo_db;

// Récupérer un rôle existant
const roleUser = db.roles.findOne({ name: "ROLE_USER" });

// Créer l'utilisateur
db.users.insertOne({
  username: "eyayari",
  email: "eyayari123@gmail.com",
  password: "$2a$10$YourHashedPasswordHere", // Hash bcrypt
  roles: [roleUser._id],
  active: true,
  forcePasswordChange: false,
  createdAt: new Date(),
  updatedAt: new Date()
});
```

**Note:** Pour un vrai mot de passe hashé, utilisez l'API de création d'utilisateur.

---

### Solution 4: Utiliser l'API pour Créer un Utilisateur

**Via Postman ou curl:**

```bash
curl -X POST http://localhost:8085/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "eyayari",
    "email": "eyayari123@gmail.com",
    "password": "password123",
    "roles": ["ROLE_USER"]
  }'
```

---

## 🧪 Test Après Correction

### 1. Vérifier l'Email

```javascript
use demo_db;
db.users.findOne({ email: "eyayari123@gmail.com" });
```

**Résultat attendu:**
```javascript
{
  "_id": "...",
  "username": "eyayari",
  "email": "eyayari123@gmail.com",
  "password": "$2a$10$...",
  "active": true
}
```

### 2. Tester la Réinitialisation

1. Aller sur http://localhost:4200/auth/forgot-password
2. Entrer `eyayari123@gmail.com`
3. Cliquer sur "Envoyer le lien"
4. **Vérifier:** Message de succès

### 3. Vérifier le Token

```javascript
use demo_db;
db.users.findOne({ email: "eyayari123@gmail.com" });
```

**Résultat attendu:**
```javascript
{
  "_id": "...",
  "email": "eyayari123@gmail.com",
  "resetToken": "ABC123XYZ...", // ← Token généré
  "resetTokenExpiry": ISODate("2025-10-29T17:00:00Z") // ← Expiration
}
```

### 4. Vérifier les Logs Backend

```
🔐 Demande de réinitialisation de mot de passe pour: eyayari123@gmail.com
✅ Email de réinitialisation envoyé à: eyayari123@gmail.com
```

---

## 📋 Checklist de Dépannage

### Backend
- [ ] Backend démarré sur le port 8085
- [ ] MongoDB connecté
- [ ] Logs backend visibles
- [ ] Aucune erreur dans les logs

### Base de Données
- [ ] MongoDB démarré
- [ ] Base `demo_db` existe
- [ ] Collection `users` existe
- [ ] Utilisateur avec l'email existe
- [ ] Utilisateur est actif (`active: true`)

### Frontend
- [ ] Frontend démarré sur le port 4200
- [ ] Console browser sans erreurs
- [ ] Requête HTTP visible dans Network tab
- [ ] URL correcte: `http://localhost:8085/api/auth/forgot-password`

---

## 🔍 Logs à Vérifier

### Console Browser (F12)

**Avant l'envoi:**
```
🔐 Demande de réinitialisation pour: eyayari123@gmail.com
```

**Après succès:**
```
✅ Réponse du serveur: Password reset email sent
```

**Après erreur:**
```
❌ Erreur: {status: 404, error: {message: "User not found"}}
```

### Logs Backend

**Succès:**
```
🔐 Demande de réinitialisation de mot de passe pour: eyayari123@gmail.com
✅ Email de réinitialisation envoyé à: eyayari123@gmail.com
```

**Erreur:**
```
❌ Aucun utilisateur trouvé avec cet email: eyayari123@gmail.com
```

---

## 💡 Conseils

### 1. Toujours Vérifier l'Email d'Abord

Avant de tester, vérifiez que l'email existe:
```javascript
db.users.findOne({ email: "VOTRE_EMAIL" });
```

### 2. Utiliser les Emails par Défaut

Pour les tests, utilisez les emails par défaut qui existent déjà:
- `admin@gestionpro.com`
- `commercial@gestionpro.com`
- `pm@gestionpro.com`
- `dm@gestionpro.com`

### 3. Vérifier les Logs en Temps Réel

Gardez les logs backend visibles pendant le test pour voir les erreurs immédiatement.

### 4. Tester avec Postman d'Abord

Avant de tester via l'interface, testez l'API directement:
```bash
curl -X POST "http://localhost:8085/api/auth/forgot-password?email=admin@gestionpro.com"
```

---

## ✅ Solution Rapide

**Si vous voulez tester immédiatement:**

1. Utilisez un email existant:
   ```
   admin@gestionpro.com
   ```

2. Ou mettez à jour votre utilisateur:
   ```javascript
   use demo_db;
   db.users.updateOne(
     { username: "admin" },
     { $set: { email: "eyayari123@gmail.com" } }
   );
   ```

3. Testez à nouveau la réinitialisation

**Ça devrait fonctionner!** ✅

