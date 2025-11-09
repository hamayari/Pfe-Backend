# 🚀 GUIDE COMPLET - CONFIGURATION JENKINS CI/CD

## 📋 TABLE DES MATIÈRES

1. [Prérequis](#prérequis)
2. [Installation Jenkins](#installation-jenkins)
3. [Configuration Jenkins](#configuration-jenkins)
4. [Configuration du Pipeline](#configuration-du-pipeline)
5. [Intégration GitHub/GitLab](#intégration-githubgitlab)
6. [Tests et Déploiement](#tests-et-déploiement)
7. [Troubleshooting](#troubleshooting)

---

## 🔧 PRÉREQUIS

### Logiciels Requis

```bash
# 1. Java 17+ (pour Jenkins et le backend)
java -version

# 2. Docker et Docker Compose
docker --version
docker-compose --version

# 3. Node.js 18+ (pour le frontend)
node --version
npm --version

# 4. Maven 3.9+ (pour le backend)
mvn --version

# 5. Git
git --version
```

### Ports Requis

- **Jenkins**: 8090 (ou 8080 si disponible)
- **Backend**: 8080
- **Frontend**: 80, 443
- **MongoDB**: 27017

---

## 📦 INSTALLATION JENKINS

### Option 1: Installation avec Docker (Recommandé)

```bash
# 1. Créer un réseau Docker pour Jenkins
docker network create jenkins

# 2. Lancer Jenkins avec Docker
docker run -d \
  --name jenkins \
  --restart=unless-stopped \
  --network jenkins \
  -p 8090:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17

# 3. Récupérer le mot de passe initial
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Option 2: Installation sur Windows

1. Télécharger Jenkins depuis: https://www.jenkins.io/download/
2. Installer Jenkins comme service Windows
3. Accéder à http://localhost:8090

### Option 3: Installation sur Linux

```bash
# Ubuntu/Debian
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -
sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
sudo apt-get update
sudo apt-get install jenkins

# Démarrer Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins
```

---

## ⚙️ CONFIGURATION JENKINS

### 1. Configuration Initiale

1. **Accéder à Jenkins**: http://localhost:8090
2. **Entrer le mot de passe initial**
3. **Installer les plugins suggérés**

### 2. Plugins Requis

Aller dans **Manage Jenkins** > **Manage Plugins** > **Available**

Installer les plugins suivants:

```
✅ Docker Pipeline
✅ Docker Commons
✅ Git
✅ GitHub Integration
✅ Pipeline
✅ Pipeline: Stage View
✅ JUnit
✅ JaCoCo
✅ HTML Publisher
✅ Slack Notification (optionnel)
✅ Email Extension (optionnel)
✅ OWASP Dependency-Check
```

### 3. Configuration des Credentials

#### a) Docker Hub Credentials

1. **Manage Jenkins** > **Manage Credentials**
2. **Add Credentials**
   - Kind: `Username with password`
   - ID: `dockerhub-credentials`
   - Username: Votre username Docker Hub
   - Password: Votre token Docker Hub
   - Description: `Docker Hub Access`

#### b) GitHub/GitLab Credentials

1. **Add Credentials**
   - Kind: `SSH Username with private key` (pour SSH)
   - OU `Username with password` (pour HTTPS)
   - ID: `github-credentials`
   - Username: Votre username GitHub
   - Password/Private Key: Votre token ou clé SSH

#### c) Variables d'Environnement Sensibles

1. **Manage Jenkins** > **Configure System**
2. **Global properties** > **Environment variables**

Ajouter:
```
JWT_SECRET=your-secret-key
BREVO_API_KEY=your-brevo-key
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
MONGO_PASSWORD=your-mongo-password
```

### 4. Configuration des Outils

#### Maven

1. **Manage Jenkins** > **Global Tool Configuration**
2. **Maven** > **Add Maven**
   - Name: `Maven 3.9`
   - Install automatically: ✅
   - Version: `3.9.5`

#### Node.js

1. **NodeJS** > **Add NodeJS**
   - Name: `Node 18`
   - Install automatically: ✅
   - Version: `18.x`

#### Docker

1. **Docker** > **Add Docker**
   - Name: `Docker`
   - Install automatically: ✅

---

## 🔗 CONFIGURATION DU PIPELINE

### 1. Créer un Nouveau Job

1. **New Item**
2. Nom: `commercial-pfe-pipeline`
3. Type: **Pipeline**
4. **OK**

### 2. Configuration du Job

#### General

- ✅ **GitHub project**: URL de votre repo
- ✅ **Discard old builds**: Keep 10 builds

#### Build Triggers

- ✅ **GitHub hook trigger for GITScm polling**
- ✅ **Poll SCM**: `H/5 * * * *` (toutes les 5 minutes)

#### Pipeline

- **Definition**: `Pipeline script from SCM`
- **SCM**: `Git`
- **Repository URL**: URL de votre repo
- **Credentials**: Sélectionner vos credentials GitHub
- **Branch**: `*/main`
- **Script Path**: `Jenkinsfile`

### 3. Sauvegarder et Tester

```bash
# Cliquer sur "Build Now"
# Vérifier les logs dans "Console Output"
```

---

## 🔄 INTÉGRATION GITHUB/GITLAB

### Configuration GitHub Webhook

1. **Aller sur votre repo GitHub**
2. **Settings** > **Webhooks** > **Add webhook**
3. **Payload URL**: `http://your-jenkins-url:8090/github-webhook/`
4. **Content type**: `application/json`
5. **Events**: `Just the push event`
6. **Active**: ✅

### Configuration GitLab Webhook

1. **Aller sur votre projet GitLab**
2. **Settings** > **Webhooks**
3. **URL**: `http://your-jenkins-url:8090/project/commercial-pfe-pipeline`
4. **Trigger**: `Push events`, `Merge request events`
5. **Add webhook**

---

## 🧪 TESTS ET DÉPLOIEMENT

### Structure des Tests

```
Pipeline Stages:
├── 🔍 Checkout                    # Clone du code
├── 🏗️ Build & Test Backend       # Tests JUnit + Package
├── 🎨 Build & Test Frontend      # Tests Karma + Build
├── 🔒 Security Scan              # OWASP + npm audit
├── 🐳 Build Docker Images        # Backend + Frontend
├── 🧪 Integration Tests          # Tests E2E
├── 📤 Push Docker Images         # Push to registry
├── 🚀 Deploy to Staging          # Déploiement staging
└── ✅ Smoke Tests                # Tests de base
```

### Commandes Manuelles

```bash
# 1. Tester localement le pipeline
docker-compose up -d

# 2. Exécuter les tests backend
mvn clean test

# 3. Exécuter les tests frontend
cd app-frontend-new
npm test -- --watch=false --code-coverage

# 4. Build les images Docker
docker-compose build

# 5. Déployer en staging
docker-compose -f docker-compose.staging.yml up -d

# 6. Vérifier les logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

---

## 📊 RAPPORTS ET MÉTRIQUES

### Rapports Disponibles

1. **Tests JUnit** (Backend)
   - Accessible dans le build Jenkins
   - Graphiques de tendance des tests

2. **Couverture JaCoCo** (Backend)
   - Rapport HTML de couverture de code
   - Objectif: 80%

3. **Couverture Karma** (Frontend)
   - Rapport HTML dans `coverage/index.html`
   - Objectif: 80%

4. **Security Scan**
   - OWASP Dependency Check
   - npm audit

### Accéder aux Rapports

```
Jenkins Build > Test Results
Jenkins Build > Coverage Report
Jenkins Build > HTML Reports
```

---

## 🐛 TROUBLESHOOTING

### Problème 1: Jenkins ne peut pas se connecter à Docker

**Solution**:
```bash
# Donner les permissions Docker à Jenkins
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Ou dans Docker
docker exec -u root jenkins chmod 666 /var/run/docker.sock
```

### Problème 2: Tests Frontend échouent (ChromeHeadless)

**Solution**:
```bash
# Installer Chrome dans le conteneur Jenkins
docker exec -u root jenkins apt-get update
docker exec -u root jenkins apt-get install -y chromium chromium-driver
```

### Problème 3: Maven ne trouve pas les dépendances

**Solution**:
```bash
# Nettoyer le cache Maven
mvn dependency:purge-local-repository
mvn clean install
```

### Problème 4: Docker build échoue (Out of memory)

**Solution**:
```bash
# Augmenter la mémoire Docker
# Dans Docker Desktop: Settings > Resources > Memory: 4GB+
```

### Problème 5: MongoDB connection refused

**Solution**:
```bash
# Vérifier que MongoDB est démarré
docker-compose ps mongodb

# Vérifier les logs
docker-compose logs mongodb

# Redémarrer MongoDB
docker-compose restart mongodb
```

---

## 📝 CHECKLIST DE DÉPLOIEMENT

### Avant le Premier Build

- [ ] Jenkins installé et accessible
- [ ] Tous les plugins installés
- [ ] Credentials configurés (Docker Hub, GitHub)
- [ ] Variables d'environnement configurées
- [ ] Webhook GitHub/GitLab configuré
- [ ] Fichier `.env` créé avec les vraies valeurs
- [ ] Docker et Docker Compose installés
- [ ] Ports 8080, 80, 27017 disponibles

### Après le Premier Build Réussi

- [ ] Tests backend passent (JUnit)
- [ ] Tests frontend passent (Karma)
- [ ] Images Docker créées
- [ ] Images Docker pushées sur Docker Hub
- [ ] Application accessible sur http://localhost:80
- [ ] Backend API accessible sur http://localhost:8080
- [ ] MongoDB accessible et fonctionnel

---

## 🚀 COMMANDES UTILES

```bash
# Démarrer l'application complète
docker-compose up -d

# Voir les logs en temps réel
docker-compose logs -f

# Arrêter l'application
docker-compose down

# Nettoyer complètement (avec volumes)
docker-compose down -v

# Rebuild les images
docker-compose build --no-cache

# Vérifier le statut des services
docker-compose ps

# Exécuter une commande dans un conteneur
docker-compose exec backend bash
docker-compose exec frontend sh

# Voir les logs d'un service spécifique
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mongodb
```

---

## 📞 SUPPORT

En cas de problème:

1. Vérifier les logs Jenkins: `Console Output`
2. Vérifier les logs Docker: `docker-compose logs`
3. Vérifier les health checks: `docker-compose ps`
4. Consulter la documentation Jenkins: https://www.jenkins.io/doc/

---

**✅ Votre pipeline Jenkins est maintenant configuré et prêt à l'emploi !**
