# 🔧 Dépannage - Token de Réinitialisation

## 🐛 Problème : "Token invalide ou expiré" immédiatement après la demande

### Causes Possibles

1. **MongoDB non connecté** - Le token n'est pas sauvegardé
2. **Token mal encodé dans l'URL** - Caractères spéciaux non échappés
3. **Backend redémarré** - Données perdues si MongoDB n'est pas persistant
4. **Problème de sauvegarde** - Transaction non commitée

### 🔍 Étapes de Diagnostic

#### 1. Vérifier les Logs Backend

Après avoir demandé une réinitialisation, vous devriez voir dans les logs :

```
🔐 Token généré pour user@example.com: [TOKEN_VALUE]
🔐 Expiration du token: [DATE]
✅ Token sauvegardé dans la base de données pour: user@example.com
✅ Email de réinitialisation envoyé à: user@example.com
```

Si vous ne voyez pas ces logs, le problème est dans la sauvegarde.

#### 2. Vérifier MongoDB

Ouvrez MongoDB Compass ou utilisez la CLI :

```javascript
// Se connecter à MongoDB
use gestionpro

// Vérifier les utilisateurs avec token
db.users.find({ resetToken: { $exists: true, $ne: null } })

// Vérifier un utilisateur spécifique
db.users.findOne({ email: "eyayari123@gmail.com" })
```

Vous devriez voir :
```json
{
  "_id": "...",
  "email": "eyayari123@gmail.com",
  "resetToken": "...",
  "resetTokenExpiry": ISODate("...")
}
```

#### 3. Vérifier l'URL de l'Email

L'URL dans l'email devrait ressembler à :
```
http://localhost:4200/auth/reset-password?token=XXXXXXXX&role=decision-maker
```

**Important** : Le token ne doit pas contenir de caractères spéciaux qui cassent l'URL.

#### 4. Tester Manuellement

1. **Copier le token** depuis les logs backend
2. **Ouvrir l'URL** manuellement :
   ```
   http://localhost:4200/auth/reset-password?token=[TOKEN_COPIÉ]&role=decision-maker
   ```
3. **Vérifier** si ça fonctionne

### 🔧 Solutions

#### Solution 1 : Vérifier la Connexion MongoDB

Dans `application.properties` :

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/gestionpro
spring.data.mongodb.database=gestionpro

# Logs MongoDB
logging.level.org.springframework.data.mongodb=DEBUG
```

Redémarrer le backend et vérifier les logs de connexion.

#### Solution 2 : Encoder le Token Correctement

Le token est généré avec `Base64.getEncoder()` qui peut contenir des caractères `+`, `/`, `=`.

Modifions la génération pour utiliser un encodage URL-safe :

```java
private String generateSecurePassword() {
    byte[] randomBytes = new byte[24];
    secureRandom.nextBytes(randomBytes);
    // Utiliser l'encodeur URL-safe
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
}
```

#### Solution 3 : Augmenter la Durée de Validité (pour test)

Temporairement, augmenter à 24 heures pour tester :

```java
user.setResetTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS));
```

#### Solution 4 : Vérifier les Transactions

Ajouter `@Transactional` sur la méthode si ce n'est pas déjà fait :

```java
@Transactional
public void initiatePasswordReset(String email) {
    // ...
}
```

### 🧪 Test Complet

1. **Arrêter le backend**
2. **Vérifier MongoDB** est démarré :
   ```bash
   # Windows
   net start MongoDB
   
   # Ou vérifier le service
   services.msc
   ```

3. **Démarrer le backend** avec logs détaillés :
   ```bash
   mvn spring-boot:run -Dlogging.level.com.example.demo=DEBUG
   ```

4. **Demander une réinitialisation**
5. **Vérifier les logs** pour voir le token généré
6. **Vérifier MongoDB** pour voir si le token est sauvegardé
7. **Copier le token** depuis les logs
8. **Tester avec l'URL** manuelle

### 📝 Checklist de Vérification

- [ ] MongoDB est démarré et accessible
- [ ] Backend se connecte à MongoDB (voir logs)
- [ ] Email existe dans la base de données
- [ ] Token est généré (voir logs)
- [ ] Token est sauvegardé dans MongoDB (vérifier avec Compass)
- [ ] Email est envoyé avec le bon token
- [ ] URL dans l'email est correcte
- [ ] Token dans l'URL correspond au token dans MongoDB

### 🚨 Si Rien ne Fonctionne

Créer un endpoint de test pour vérifier :

```java
@GetMapping("/test-reset-token")
public ResponseEntity<?> testResetToken(@RequestParam String email) {
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
        return ResponseEntity.ok(Map.of(
            "email", user.get().getEmail(),
            "hasResetToken", user.get().getResetToken() != null,
            "resetToken", user.get().getResetToken(),
            "tokenExpiry", user.get().getResetTokenExpiry()
        ));
    }
    return ResponseEntity.notFound().build();
}
```

Appeler : `http://localhost:8085/api/auth/test-reset-token?email=eyayari123@gmail.com`

### 💡 Astuce

Pour déboguer rapidement, ajoutez un `System.out.println()` dans le code :

```java
String resetToken = generateSecurePassword();
System.out.println("========================================");
System.out.println("TOKEN GÉNÉRÉ: " + resetToken);
System.out.println("========================================");
```

Cela affichera le token en gros dans la console.
