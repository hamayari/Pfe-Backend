# 🚀 Guide Pipeline CI/CD - Tests Unitaires + Docker + SonarQube

## 📋 Vue d'ensemble

Pipeline CI/CD optimisée pour:
- ✅ **Tests unitaires JUnit** (83 tests actifs)
- ✅ **Build image Docker** backend
- ✅ **Analyse SonarQube** (qualité du code)
- ✅ **Couverture de code JaCoCo**
- ✅ **Push Docker Hub** (optionnel)

---

## 📁 Fichiers de la Pipeline

### 1. Jenkinsfile Principal
**Fichier**: `Jenkinsfile.UNIT-TESTS`

```groovy
// Pipeline optimisée pour tests unitaires uniquement
// - Pas de tests d'intégration (désactivés)
// - Pas de MongoDB requis pour les tests
// - Build rapide et efficace
```

### 2. Configuration Maven
**Fichier**: `pom.xml`

```xml
<!-- SonarQube configuré -->
<sonar.projectKey>Commercial-PFE-Backend</sonar.projectKey>
<sonar.host.url>http://localhost:9000</sonar.host.url>

<!-- JaCoCo configuré -->
<jacoco-maven-plugin>0.8.11</jacoco-maven-plugin>
```

### 3. Dockerfile
**Fichier**: `Dockerfile`

```dockerfile
# Multi-stage build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
FROM eclipse-temurin:17-jre-alpine
# Image optimisée avec health check
```

---

## 🎯 Étapes de la Pipeline

### Stage 1: 🚀 Initialisation
- Affichage des informations du build
- Configuration de l'environnement
- Définition du nom du build

### Stage 2: 📥 Checkout
- Récupération du code source depuis Git
- Vérification des fichiers critiques

### Stage 3: 🏗️ Build & Tests Maven
#### 3.1 🔨 Compilation
```bash
mvn clean compile -B -DskipTests
```
- Compilation du code source
- Comptage des classes compilées

#### 3.2 🧪 Tests Unitaires
```bash
mvn test -Dspring.profiles.active=test -B
```
- **83 tests unitaires** exécutés
- **0 erreur** attendu
- Publication des résultats JUnit
- Génération du rapport JaCoCo

**Tests actifs**:
- ✅ UserServiceTest (17 tests)
- ✅ KpiCalculatorServiceTest (20 tests)
- ✅ ConventionServiceTest (27 tests)
- ✅ AuthServiceDetailedTest (18 tests)
- ✅ DemoApplicationTests (1 test)

**Tests désactivés** (78 tests):
- ⏸️ ConventionControllerTest (19 tests)
- ⏸️ AuthControllerTest (20 tests)
- ⏸️ ConventionRepositoryTest (27 tests)
- ⏸️ AuthenticationIntegrationTest (9 tests)
- ⏸️ AuthServiceTest (3 tests)

#### 3.3 📊 SonarQube Analysis
```bash
mvn sonar:sonar \
  -Dsonar.projectKey=Commercial-PFE-Backend \
  -Dsonar.host.url=http://localhost:9000
```
- Analyse de la qualité du code
- Détection des bugs et code smells
- Calcul de la couverture de code
- Génération du rapport

#### 3.4 🚦 Quality Gate
- Vérification des seuils de qualité SonarQube
- Timeout: 5 minutes
- Si échec: build UNSTABLE (pas FAILED)

#### 3.5 📦 Package JAR
```bash
mvn package -DskipTests -B
```
- Création du fichier JAR
- Archive de l'artefact
- Fingerprinting pour traçabilité

### Stage 4: 🐳 Docker Build
```bash
docker build \
  -t hamalak/commercial-pfe-backend:${BUILD_NUMBER} \
  -t hamalak/commercial-pfe-backend:latest \
  -t hamalak/commercial-pfe-backend:v1.0.0 \
  .
```
- Construction de l'image Docker
- Multi-tagging (build, latest, version)
- Labels pour métadonnées

### Stage 5: 🧪 Test Docker
```bash
docker run -d --name backend-test \
  -p 8082:8080 \
  hamalak/commercial-pfe-backend:${BUILD_NUMBER}
```
- Démarrage du conteneur
- Health check (3 tentatives)
- Vérification du endpoint `/actuator/health`

### Stage 6: 📤 Push Docker Hub (Optionnel)
```bash
docker push hamalak/commercial-pfe-backend:${BUILD_NUMBER}
docker push hamalak/commercial-pfe-backend:latest
docker push hamalak/commercial-pfe-backend:v1.0.0
```
- Push vers Docker Hub
- Authentification sécurisée via credentials Jenkins

### Stage 7: 📊 Rapport Final
- Résumé du build
- Liens vers les rapports
- Statistiques de tests

---

## ⚙️ Configuration Jenkins

### 1. Prérequis Jenkins

#### Plugins requis:
```
✓ Pipeline
✓ Docker Pipeline
✓ JUnit
✓ JaCoCo
✓ SonarQube Scanner
✓ HTML Publisher
✓ Git
```

#### Installation:
```bash
# Dans Jenkins > Manage Jenkins > Manage Plugins
# Installer tous les plugins ci-dessus
```

### 2. Configuration SonarQube

#### Dans Jenkins:
1. **Manage Jenkins** > **Configure System**
2. Section **SonarQube servers**
3. Ajouter:
   - Name: `SonarQube`
   - Server URL: `http://localhost:9000`
   - Server authentication token: (créer dans SonarQube)

#### Dans SonarQube:
1. Aller sur `http://localhost:9000`
2. **My Account** > **Security** > **Generate Token**
3. Copier le token dans Jenkins credentials

### 3. Configuration Docker Hub

#### Créer credentials Jenkins:
1. **Manage Jenkins** > **Manage Credentials**
2. **Add Credentials**
3. Type: `Username with password`
4. ID: `dockerhub-credentials`
5. Username: `hamalak`
6. Password: (votre token Docker Hub)

### 4. Créer le Job Jenkins

#### Option A: Pipeline depuis SCM
```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop',
                    url: 'https://github.com/hamayari/Pfe-Backend.git'
            }
        }
        stage('Run Pipeline') {
            steps {
                script {
                    load 'Jenkinsfile.UNIT-TESTS'
                }
            }
        }
    }
}
```

#### Option B: Pipeline Script
1. **New Item** > **Pipeline**
2. Nom: `Commercial-PFE-Backend-UnitTests`
3. **Pipeline** section
4. **Definition**: Pipeline script from SCM
5. **SCM**: Git
6. **Repository URL**: votre repo
7. **Script Path**: `Jenkinsfile.UNIT-TESTS`

---

## 🚀 Utilisation

### 1. Lancer le Build

#### Via Jenkins UI:
1. Aller sur le job `Commercial-PFE-Backend-UnitTests`
2. Cliquer sur **Build with Parameters**
3. Configurer:
   - `PUSH_TO_DOCKERHUB`: ☐ (false par défaut)
   - `RUN_SONARQUBE`: ☑ (true par défaut)
4. Cliquer sur **Build**

#### Via Jenkins CLI:
```bash
java -jar jenkins-cli.jar -s http://localhost:8080/ \
  build Commercial-PFE-Backend-UnitTests \
  -p PUSH_TO_DOCKERHUB=false \
  -p RUN_SONARQUBE=true
```

### 2. Suivre le Build

#### Console Output:
```
http://localhost:8080/job/Commercial-PFE-Backend-UnitTests/lastBuild/console
```

#### Rapports:
- **Tests JUnit**: `http://localhost:8080/job/.../lastBuild/testReport/`
- **Couverture JaCoCo**: `http://localhost:8080/job/.../lastBuild/jacoco/`
- **SonarQube**: `http://localhost:9000/dashboard?id=Commercial-PFE-Backend`

---

## 📊 Résultats Attendus

### ✅ Build Réussi

```
════════════════════════════════════════════════════════
           RAPPORT FINAL
════════════════════════════════════════════════════════
✅ Status: SUCCESS
📦 Build: #42
🏷️  Version: 1.0.0
📝 Commit: abc1234
⏱️  Durée: 8 min 32 sec

📦 ARTEFACTS:
   ✓ JAR: demo-1.0.0-SNAPSHOT.jar
   ✓ Docker: hamalak/commercial-pfe-backend:42
   ✓ Docker: hamalak/commercial-pfe-backend:latest

📊 RAPPORTS:
   • Tests: 83 passed, 0 failed
   • Couverture: 75.2% (seuil: 70%)
   • SonarQube: Quality Gate PASSED

🔗 LIENS:
   • Docker Hub: https://hub.docker.com/r/hamalak/commercial-pfe-backend
   • Jenkins: http://localhost:8080/job/.../42/
════════════════════════════════════════════════════════
```

### Métriques de Qualité

| Métrique | Seuil | Attendu |
|----------|-------|---------|
| Tests unitaires | 100% pass | ✅ 83/83 |
| Couverture ligne | 70% | ✅ ~75% |
| Couverture branche | 65% | ✅ ~68% |
| Quality Gate | PASSED | ✅ OK |
| Build Docker | SUCCESS | ✅ OK |

---

## 🔧 Personnalisation

### Modifier les Seuils de Couverture

Dans `Jenkinsfile.UNIT-TESTS`:
```groovy
environment {
    COVERAGE_THRESHOLD = '70'        // Ligne: 70%
    BRANCH_COVERAGE_THRESHOLD = '65' // Branche: 65%
}
```

### Modifier les Tags Docker

Dans `Jenkinsfile.UNIT-TESTS`:
```groovy
stage('🐳 Docker Build') {
    sh """
        docker build \
            -t ${BACKEND_IMAGE}:${IMAGE_TAG} \
            -t ${BACKEND_IMAGE}:latest \
            -t ${BACKEND_IMAGE}:v${APP_VERSION} \
            -t ${BACKEND_IMAGE}:custom-tag \
            .
    """
}
```

### Ajouter des Notifications

Dans `Jenkinsfile.UNIT-TESTS` section `post`:
```groovy
post {
    success {
        emailext(
            subject: "✅ Build ${env.BUILD_NUMBER} - SUCCESS",
            body: "Le build a réussi!",
            to: "team@example.com"
        )
    }
    failure {
        emailext(
            subject: "❌ Build ${env.BUILD_NUMBER} - FAILED",
            body: "Le build a échoué!",
            to: "team@example.com"
        )
    }
}
```

---

## 🐛 Dépannage

### Problème: Tests échouent

**Solution**:
```bash
# Exécuter localement
mvn clean test -Dspring.profiles.active=test

# Vérifier les logs
cat target/surefire-reports/*.txt
```

### Problème: SonarQube non accessible

**Solution**:
```bash
# Vérifier que SonarQube tourne
docker ps | grep sonarqube

# Démarrer SonarQube
docker start sonarqube

# Vérifier l'URL
curl http://localhost:9000/api/system/status
```

### Problème: Docker build échoue

**Solution**:
```bash
# Vérifier le Dockerfile
docker build -t test .

# Vérifier les logs
docker logs <container-id>

# Nettoyer le cache
docker system prune -a
```

### Problème: Push Docker Hub échoue

**Solution**:
```bash
# Vérifier les credentials
docker login -u hamalak

# Vérifier le nom de l'image
docker images | grep commercial-pfe-backend

# Push manuel
docker push hamalak/commercial-pfe-backend:latest
```

---

## 📚 Ressources

### Documentation
- **Jenkins Pipeline**: https://www.jenkins.io/doc/book/pipeline/
- **SonarQube**: https://docs.sonarqube.org/
- **JaCoCo**: https://www.jacoco.org/jacoco/trunk/doc/
- **Docker**: https://docs.docker.com/

### Liens Utiles
- **Jenkins**: http://localhost:8080/
- **SonarQube**: http://localhost:9000/
- **Docker Hub**: https://hub.docker.com/r/hamalak/commercial-pfe-backend

---

## ✅ Checklist de Déploiement

Avant de lancer la pipeline en production:

- [ ] Jenkins installé et configuré
- [ ] Plugins Jenkins installés
- [ ] SonarQube démarré et accessible
- [ ] Token SonarQube créé et configuré dans Jenkins
- [ ] Credentials Docker Hub configurés dans Jenkins
- [ ] Repository Git accessible
- [ ] Dockerfile présent et valide
- [ ] pom.xml configuré avec JaCoCo et SonarQube
- [ ] Tests unitaires passent localement (`mvn test`)
- [ ] Build Docker fonctionne localement (`docker build .`)

---

## 🎯 Prochaines Étapes

1. ✅ **Tests unitaires** - Implémenté
2. ✅ **Build Docker** - Implémenté
3. ✅ **SonarQube** - Implémenté
4. ⏳ **Tests d'intégration** - À activer quand MongoDB configuré
5. ⏳ **Déploiement automatique** - À implémenter
6. ⏳ **Notifications Slack/Email** - À configurer

---

**Créé le**: 2025-11-11  
**Version**: 1.0.0  
**Auteur**: Pipeline CI/CD Team
