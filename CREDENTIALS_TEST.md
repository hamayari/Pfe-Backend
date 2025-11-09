# 🔐 CREDENTIALS DE TEST - TOUS LES RÔLES

## ✅ CREDENTIALS VALIDES

Tous ces mots de passe **contiennent des chiffres** et sont **100% fonctionnels**.

### 1️⃣ ADMINISTRATEUR
```
Username: admin
Password: admin123
Email: admin@gestionpro.com
Rôle: ROLE_ADMIN
Dashboard: /admin-dashboard
```

### 2️⃣ COMMERCIAL
```
Username: commercial
Password: commercial123
Email: commercial@gestionpro.com
Rôle: ROLE_COMMERCIAL
Dashboard: /commercial-dashboard
```

### 3️⃣ CHEF DE PROJET (Project Manager)
```
Username: projectmanager
Password: pm123456
Email: pm@gestionpro.com
Rôle: ROLE_PROJECT_MANAGER
Dashboard: /project-manager-dashboard
```

### 4️⃣ DÉCIDEUR (Decision Maker)
```
Username: decisionmaker
Password: dm123456
Email: dm@gestionpro.com
Rôle: ROLE_DECISION_MAKER
Dashboard: /decision-maker-dashboard
```

---

## 🔍 ANALYSE DU PROBLÈME "CHIFFRES NON ACCEPTÉS"

### ✅ VALIDATION ACTUELLE DU MOT DE PASSE

Le champ mot de passe dans `login.component.ts` a seulement:
```typescript
password: ['', [Validators.required, Validators.minLength(6)]]
```

**AUCUNE restriction sur les chiffres!** ✅

### ❌ PAS DE PATTERN RESTRICTIF

Le code ne contient **AUCUN** `Validators.pattern` qui bloquerait les chiffres dans le champ mot de passe du login.

**Note:** Il existe des patterns restrictifs dans d'autres formulaires (création d'utilisateur), mais **PAS dans le login**.

---

## 🛠️ SOLUTIONS SI LE PROBLÈME PERSISTE

### 1. Vider le cache du navigateur
```
- Chrome/Edge: Ctrl + Shift + Delete
- Firefox: Ctrl + Shift + Delete
- Ou: Ctrl + F5 pour rafraîchir la page
```

### 2. Désactiver l'autocomplete
Le navigateur peut interférer avec la saisie. Essayez:
- Mode navigation privée
- Désactiver les extensions
- Tester dans un autre navigateur

### 3. Vérifier le clavier
- Pavé numérique activé (touche Num Lock)
- Tester la saisie dans un éditeur de texte
- Vérifier la langue du clavier

### 4. Tester avec DevTools
Ouvrez la console (F12) et vérifiez:
```javascript
// Vérifier la valeur du champ
document.querySelector('input[formControlName="password"]').value
```

---

## 🧪 PROCÉDURE DE TEST

### Test 1: Login Admin
1. Aller sur: `http://localhost:4200/auth/login-admin`
2. Username: `admin`
3. Password: `admin123` ← **Contient "123"**
4. Cliquer sur "Se connecter"
5. ✅ Devrait rediriger vers `/admin-dashboard`

### Test 2: Login Commercial
1. Aller sur: `http://localhost:4200/auth/login-commercial`
2. Username: `commercial`
3. Password: `commercial123` ← **Contient "123"**
4. Cliquer sur "Se connecter"
5. ✅ Devrait rediriger vers `/commercial-dashboard`

### Test 3: Login Project Manager
1. Aller sur: `http://localhost:4200/auth/login-project-manager`
2. Username: `projectmanager`
3. Password: `pm123456` ← **Contient "123456"**
4. Cliquer sur "Se connecter"
5. ✅ Devrait rediriger vers `/project-manager-dashboard`

### Test 4: Login Decision Maker
1. Aller sur: `http://localhost:4200/auth/login-decision-maker`
2. Username: `decisionmaker`
3. Password: `dm123456` ← **Contient "123456"**
4. Cliquer sur "Se connecter"
5. ✅ Devrait rediriger vers `/decision-maker-dashboard`

---

## 📊 VÉRIFICATION BACKEND

Les credentials sont créés automatiquement au démarrage de l'application dans:
```
src/main/java/com/example/demo/config/DataInitializer.java
```

Vérifiez les logs du backend au démarrage:
```
✅ Utilisateur Admin créé: admin / admin123
✅ Utilisateur Commercial créé: commercial / commercial123
✅ Utilisateur Project Manager créé: projectmanager / pm123456
✅ Utilisateur Decision Maker créé: decisionmaker / dm123456
```

---

## 🎯 CONCLUSION

**Le code ne bloque PAS les chiffres dans le mot de passe!**

Tous les mots de passe de test contiennent des chiffres et fonctionnent correctement.

Si le problème persiste:
1. ✅ Vérifiez le cache du navigateur
2. ✅ Testez en mode navigation privée
3. ✅ Vérifiez que le backend est bien démarré
4. ✅ Consultez les logs du backend pour confirmer la création des utilisateurs
5. ✅ Testez avec un autre navigateur

---

**Dernière mise à jour:** 5 novembre 2025
