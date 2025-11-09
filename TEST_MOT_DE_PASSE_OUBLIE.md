# 🧪 Guide de Test - Mot de Passe Oublié

## ✅ Modification Appliquée

**Fichier modifié:** `demo/app-frontend-new/src/app/auth/login/login.component.ts`

**Changement:**
```typescript
// Avant
forgotPassword(event: Event) {
  event.preventDefault();
  alert('Fonctionnalité de récupération de mot de passe à implémenter');
}

// Après
forgotPassword(event: Event) {
  event.preventDefault();
  this.router.navigate(['/auth/forgot-password']);
}
```

---

## 🧪 Test Complet

### Étape 1: Démarrer l'Application

```bash
# Terminal 1 - Backend
cd demo
mvn spring-boot:run

# Terminal 2 - Frontend
cd demo/app-frontend-new
ng serve
```

### Étape 2: Tester le Flux Complet

#### 2.1 Accéder à la Page de Login

Ouvrez votre navigateur et allez sur:
- http://localhost:4200/auth/login
- OU http://localhost:4200/auth/login-admin
- OU http://localhost:4200/auth/login-commercial
- OU n'importe quelle page de login

#### 2.2 Cliquer sur "Mot de passe oublié ?"

1. Sur la page de login, cherchez le lien "Mot de passe oublié ?"
2. Cliquez dessus
3. **Vérifier:** Vous êtes redirigé vers `/auth/forgot-password`

#### 2.3 Demander la Réinitialisation

1. Entrez un email valide (ex: `admin@gestionpro.com`)
2. Cliquez sur "Envoyer le lien"
3. **Vérifier:** Message de confirmation affiché
4. **Vérifier logs backend:**
   ```
   🔐 Demande de réinitialisation de mot de passe pour: admin@gestionpro.com
   ✅ Email de réinitialisation envoyé à: admin@gestionpro.com
   ```

#### 2.4 Récupérer le Token

**Dans les logs backend**, cherchez une ligne comme:
```
Email de réinitialisation pour admin@gestionpro.com avec token: ABC123XYZ...
```

Copiez le token.

#### 2.5 Réinitialiser le Mot de Passe

1. Allez sur: `http://localhost:4200/auth/reset-password?token=VOTRE_TOKEN`
2. Entrez un nouveau mot de passe (min 6 caractères)
3. Confirmez le mot de passe
4. **Vérifier:** Indicateur de force du mot de passe s'affiche
5. Cliquez sur "Réinitialiser le mot de passe"
6. **Vérifier:** Message de succès
7. **Vérifier:** Redirection automatique vers `/auth/login` après 3 secondes

#### 2.6 Se Connecter avec le Nouveau Mot de Passe

1. Sur la page de login
2. Entrez votre nom d'utilisateur
3. Entrez le NOUVEAU mot de passe
4. Cliquez sur "Se connecter"
5. **Vérifier:** Connexion réussie!

---

## 📸 Captures d'Écran Attendues

### 1. Page de Login avec Lien
```
┌─────────────────────────────────────┐
│         🔒 Connexion                │
│                                     │
│  Nom d'utilisateur: [_________]    │
│  Mot de passe:      [_________]    │
│                                     │
│  [  Se connecter  ]                │
│                                     │
│  ❓ Mot de passe oublié ?  ← CLIC  │
└─────────────────────────────────────┘
```

### 2. Page Mot de Passe Oublié
```
┌─────────────────────────────────────┐
│    🔒 Mot de passe oublié           │
│                                     │
│  Entrez votre adresse email         │
│                                     │
│  Email: [admin@gestionpro.com]     │
│                                     │
│  [  Envoyer le lien  ]             │
│                                     │
│  ← Retour à la connexion           │
└─────────────────────────────────────┘
```

### 3. Confirmation d'Envoi
```
┌─────────────────────────────────────┐
│         ✅ Email envoyé !           │
│                                     │
│  Un email a été envoyé à            │
│  admin@gestionpro.com               │
│                                     │
│  Vérifiez votre boîte de réception  │
│  Le lien est valide pendant 1 heure │
│                                     │
│  ← Retour à la connexion           │
└─────────────────────────────────────┘
```

### 4. Page Réinitialisation
```
┌─────────────────────────────────────┐
│  🔑 Réinitialiser le mot de passe   │
│                                     │
│  Nouveau mot de passe:              │
│  [______________] 👁                │
│                                     │
│  Confirmer:                         │
│  [______________] 👁                │
│                                     │
│  Force: ████████░░ Moyen           │
│                                     │
│  [  Réinitialiser  ]               │
│                                     │
│  ← Retour à la connexion           │
└─────────────────────────────────────┘
```

### 5. Succès Réinitialisation
```
┌─────────────────────────────────────┐
│    ✅ Mot de passe réinitialisé !   │
│                                     │
│  Votre mot de passe a été modifié   │
│  avec succès.                       │
│                                     │
│  Redirection vers la connexion...   │
│                                     │
│  ← Retour à la connexion           │
└─────────────────────────────────────┘
```

---

## 🔍 Vérifications dans MongoDB

### Avant Réinitialisation
```javascript
use demo_db;
db.users.findOne({ email: "admin@gestionpro.com" });

// Résultat attendu:
{
  "_id": "...",
  "username": "admin",
  "email": "admin@gestionpro.com",
  "password": "$2a$10$OLD_HASH...",
  "resetToken": "ABC123XYZ...",
  "resetTokenExpiry": ISODate("2025-10-29T17:00:00Z")
}
```

### Après Réinitialisation
```javascript
db.users.findOne({ email: "admin@gestionpro.com" });

// Résultat attendu:
{
  "_id": "...",
  "username": "admin",
  "email": "admin@gestionpro.com",
  "password": "$2a$10$NEW_HASH...", // ← Changé
  "resetToken": null, // ← Supprimé
  "resetTokenExpiry": null // ← Supprimé
}
```

---

## 🐛 Dépannage

### Problème 1: "Token invalide ou expiré"

**Cause:** Le token a expiré (> 1 heure) ou n'existe pas

**Solution:**
1. Refaire une demande de réinitialisation
2. Utiliser le nouveau token immédiatement

### Problème 2: Email non reçu

**Cause:** Configuration email incorrecte

**Solution:**
1. Vérifier les logs backend pour voir si l'email a été envoyé
2. Récupérer le token directement depuis les logs
3. Vérifier la configuration dans `application.properties`

### Problème 3: "Les mots de passe ne correspondent pas"

**Cause:** Erreur de saisie

**Solution:**
1. Retaper les deux mots de passe identiques
2. Utiliser le bouton 👁 pour voir ce que vous tapez

### Problème 4: Redirection ne fonctionne pas

**Cause:** Route non configurée

**Solution:**
1. Vérifier que les routes sont bien dans `app.routes.ts`
2. Redémarrer `ng serve`

---

## ✅ Checklist de Test

### Fonctionnalités de Base
- [ ] Lien "Mot de passe oublié ?" visible sur la page de login
- [ ] Clic sur le lien redirige vers `/auth/forgot-password`
- [ ] Formulaire de demande fonctionne
- [ ] Email de confirmation affiché
- [ ] Token généré dans la base de données

### Réinitialisation
- [ ] Page de réinitialisation accessible avec token
- [ ] Validation du mot de passe fonctionne
- [ ] Indicateur de force s'affiche
- [ ] Confirmation du mot de passe fonctionne
- [ ] Message d'erreur si mots de passe différents
- [ ] Réinitialisation réussie
- [ ] Token supprimé de la base
- [ ] Redirection automatique vers login

### Connexion
- [ ] Connexion avec nouveau mot de passe fonctionne
- [ ] Ancien mot de passe ne fonctionne plus

### Sécurité
- [ ] Token expire après 1 heure
- [ ] Token invalide rejeté
- [ ] Token usage unique (supprimé après utilisation)

---

## 📝 Utilisateurs de Test

### Admin
- **Email:** admin@gestionpro.com
- **Username:** admin
- **Mot de passe actuel:** admin123

### Commercial
- **Email:** commercial@gestionpro.com
- **Username:** commercial
- **Mot de passe actuel:** commercial123

### Chef de Projet
- **Email:** pm@gestionpro.com
- **Username:** projectmanager
- **Mot de passe actuel:** pm123456

### Décideur
- **Email:** dm@gestionpro.com
- **Username:** decisionmaker
- **Mot de passe actuel:** dm123456

---

## 🎯 Résultat Attendu

Après avoir suivi tous les tests:
- ✅ Le lien "Mot de passe oublié ?" redirige correctement
- ✅ L'email de réinitialisation est envoyé (ou token dans les logs)
- ✅ La réinitialisation fonctionne
- ✅ La connexion avec le nouveau mot de passe fonctionne
- ✅ L'ancien mot de passe ne fonctionne plus

**Fonctionnalité complète et opérationnelle!** 🎉

