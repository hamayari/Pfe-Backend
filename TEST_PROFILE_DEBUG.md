# Test de Debug du Profil

## Étapes pour vérifier le problème

1. **Connectez-vous en tant que decideur**
2. **Ouvrez la console (F12)**
3. **Allez sur la page `/profile`**
4. **Cherchez dans la console les logs suivants :**
   - `👤 Chargement du profil utilisateur...`
   - `✅ Profil chargé depuis /api/user-profile/me:`
   
5. **Copiez la réponse complète de l'API**

## Test manuel du token

Dans la console du navigateur, tapez :
```javascript
localStorage.getItem('token')
```

Copiez le token et allez sur https://jwt.io pour le décoder.
Vérifiez que le `sub` (subject) contient bien le username du decideur.

## Test de l'endpoint backend

Dans Postman ou curl, testez :
```bash
GET http://localhost:8085/api/user-profile/me
Headers:
  Authorization: Bearer VOTRE_TOKEN_ICI
```

La réponse devrait contenir les infos du decideur, pas du commercial.
