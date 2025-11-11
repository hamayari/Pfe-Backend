# 🔓 Guide pour Activer les Tests MongoDB

## 📋 Tests Actuellement Désactivés (78 tests)

### Contrôleurs (39 tests)
- `ConventionControllerTest.java` (19 tests)
- `AuthControllerTest.java` (20 tests)

### Repositories (27 tests)
- `ConventionRepositoryTest.java` (27 tests)

### Intégration (10 tests)
- `AuthenticationIntegrationTest.java` (9 tests)
- `AuthServiceTest.java` - package racine (1 test)

### Services (2 tests)
- `AuthServiceTest.java` - package service (2 tests)

---

## 🚀 Étape 1: Démarrer MongoDB

### Option A: Docker (Recommandé)

```bash
# Démarrer MongoDB
docker run -d -p 27017:27017 --name mongodb-test mongo:latest

# Vérifier
docker ps

# Logs
docker logs mongodb-test
```

### Option B: MongoDB Local

```bash
# Windows
net start MongoDB

# OU
mongod --dbpath C:\data\db

# Linux/Mac
sudo systemctl start mongod
```

### Option C: MongoDB Atlas (Cloud)

1. Créer un compte sur https://www.mongodb.com/cloud/atlas
2. Créer un cluster gratuit
3. Obtenir la connection string
4. Modifier `application-test.yml`

---

## 🔧 Étape 2: Modifier les Fichiers de Test

### 1. ConventionControllerTest.java

**Fichier**: `src/test/java/com/example/demo/controller/ConventionControllerTest.java`

```java
// ❌ SUPPRIMER cette ligne:
@org.junit.jupiter.api.Disabled("Requires MongoDB - ApplicationContext fails to load without database")

// ✅ Résultat:
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class ConventionControllerTest {
    // ...
}
```

### 2. AuthControllerTest.java

**Fichier**: `src/test/java/com/example/demo/controller/AuthControllerTest.java`

```java
// ❌ SUPPRIMER cette ligne:
@org.junit.jupiter.api.Disabled("Requires MongoDB - ApplicationContext fails to load without database")

// ✅ Résultat:
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    // ...
}
```

### 3. ConventionRepositoryTest.java

**Fichier**: `src/test/java/com/example/demo/repository/ConventionRepositoryTest.java`

```java
// ❌ SUPPRIMER cette ligne:
@org.junit.jupiter.api.Disabled("MongoDB not available - enable when MongoDB is running")

// ✅ ET DÉCOMMENTER:
@DataMongoTest
@ActiveProfiles("test")
class ConventionRepositoryTest {
    // ...
}
```

### 4. AuthenticationIntegrationTest.java

**Fichier**: `src/test/java/com/example/demo/integration/AuthenticationIntegrationTest.java`

```java
// ❌ SUPPRIMER cette ligne:
@org.junit.jupiter.api.Disabled("Integration test - requires MongoDB")

// ✅ Résultat:
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {
    // ...
}
```

### 5. AuthServiceTest.java (package racine)

**Fichier**: `src/test/java/com/example/demo/AuthServiceTest.java`

```java
// ❌ SUPPRIMER cette ligne:
@org.junit.jupiter.api.Disabled("Integration test - requires clean database (user already exists)")

// ✅ Résultat:
@SpringBootTest
public class AuthServiceTest {
    // ...
}
```

### 6. AuthServiceTest.java (package service)

**Fichier**: `src/test/java/com/example/demo/service/AuthServiceTest.java`

```java
// ❌ SUPPRIMER cette ligne:
@org.junit.jupiter.api.Disabled("Integration tests - require database setup")

// ✅ Résultat:
@SpringBootTest
public class AuthServiceTest {
    // ...
}
```

---

## 🔄 Étape 3: Vérifier la Configuration

### Fichier: `application-test.yml`

**Emplacement**: `src/test/resources/application-test.yml`

Vérifiez que la configuration MongoDB est correcte:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/commercial-pfe-test
      # OU pour MongoDB Atlas:
      # uri: mongodb+srv://username:password@cluster.mongodb.net/commercial-pfe-test
```

---

## ✅ Étape 4: Exécuter les Tests

```bash
# Tous les tests (avec MongoDB)
mvn clean test

# Avec rapport de couverture
mvn clean test jacoco:report

# Voir les résultats
start target\site\jacoco\index.html
```

---

## 📊 Résultat Attendu

### Avant (MongoDB désactivé)
```
Tests run: 161
Failures: 0
Errors: 0
Skipped: 78
BUILD SUCCESS ✅
```

### Après (MongoDB activé)
```
Tests run: 161
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS ✅
```

**+78 tests actifs !** 🎉

---

## 🛠️ Script Automatique (Optionnel)

Créez un fichier `activer-tests-mongodb.bat`:

```batch
@echo off
echo ========================================
echo Activation des tests MongoDB
echo ========================================
echo.

echo 1. Démarrage de MongoDB avec Docker...
docker run -d -p 27017:27017 --name mongodb-test mongo:latest

echo.
echo 2. Attente du démarrage de MongoDB (5 secondes)...
timeout /t 5 /nobreak

echo.
echo 3. Vérification de MongoDB...
docker ps | findstr mongodb-test

echo.
echo 4. MongoDB est prêt !
echo.
echo Pour exécuter les tests:
echo   mvn clean test
echo.
echo Pour arrêter MongoDB:
echo   docker stop mongodb-test
echo   docker rm mongodb-test
echo.
pause
```

---

## 🔍 Vérification MongoDB

### Tester la Connexion

```bash
# Avec Docker
docker exec -it mongodb-test mongosh

# Commandes MongoDB
show dbs
use commercial-pfe-test
show collections
exit
```

### Vérifier les Logs

```bash
# Logs MongoDB
docker logs mongodb-test

# Logs des tests
type target\surefire-reports\*.txt
```

---

## ⚠️ Problèmes Courants

### Problème 1: Port 27017 déjà utilisé

```bash
# Trouver le processus
netstat -ano | findstr :27017

# Arrêter le processus
taskkill /PID <PID> /F

# OU utiliser un autre port
docker run -d -p 27018:27017 --name mongodb-test mongo:latest
```

Puis modifier `application-test.yml`:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27018/commercial-pfe-test
```

### Problème 2: Docker non installé

**Solution**: Installer Docker Desktop
- Windows: https://www.docker.com/products/docker-desktop
- Ou utiliser MongoDB local

### Problème 3: Tests échouent avec MongoDB

```bash
# Nettoyer la base de test
docker exec -it mongodb-test mongosh --eval "db.getSiblingDB('commercial-pfe-test').dropDatabase()"

# Relancer les tests
mvn clean test
```

---

## 📈 Couverture de Code Attendue

### Avec MongoDB Activé

- **Contrôleurs**: ~70% de couverture
- **Services**: ~65% de couverture
- **Repositories**: ~80% de couverture
- **Global**: ~70% de couverture

---

## 🎯 Commandes Rapides

```bash
# Démarrer MongoDB
docker run -d -p 27017:27017 --name mongodb-test mongo:latest

# Exécuter les tests
mvn clean test

# Voir la couverture
mvn clean test jacoco:report
start target\site\jacoco\index.html

# Arrêter MongoDB
docker stop mongodb-test
docker rm mongodb-test
```

---

## 📞 Support

Si vous rencontrez des problèmes:

1. Vérifiez que MongoDB tourne: `docker ps`
2. Vérifiez les logs: `docker logs mongodb-test`
3. Vérifiez la connexion: `docker exec -it mongodb-test mongosh`
4. Nettoyez et relancez: `mvn clean test`

---

**Bonne chance avec l'activation des tests !** 🚀
