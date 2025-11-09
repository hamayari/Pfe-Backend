# 🚀 CI/CD Pipeline - Commercial PFE

## 📋 Vue d'Ensemble

Ce projet utilise **Jenkins** pour l'intégration continue et le déploiement continu (CI/CD) avec **Docker** pour la containerisation.

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      JENKINS PIPELINE                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  1. 🔍 Checkout Code (GitHub/GitLab)                        │
│  2. 🏗️  Build & Test Backend (Maven + JUnit)                │
│  3. 🎨 Build & Test Frontend (npm + Karma)                  │
│  4. 🔒 Security Scan (OWASP + npm audit)                    │
│  5. 🐳 Build Docker Images (Backend + Frontend)             │
│  6. 🧪 Integration Tests (E2E)                              │
│  7. 📤 Push to Docker Registry                              │
│  8. 🚀 Deploy to Staging                                    │
│  9. ✅ Smoke Tests                                          │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Structure des Fichiers

```
commercial-pfe/demo/
├── Jenkinsfile                      # Pipeline principal
├── docker-compose.yml               # Production
├── docker-compose.test.yml          # Tests d'intégration
├── docker-compose.staging.yml       # Staging
├── Dockerfile                       # Backend
├── app-frontend-new/
│   ├── Dockerfile                   # Frontend
│   └── nginx.conf                   # Configuration Nginx
├── scripts/
│   ├── run-backend-tests.sh         # Tests backend (Linux/Mac)
│   ├── run-frontend-tests.sh        # Tests frontend (Linux/Mac)
│   ├── deploy.sh                    # Déploiement (Linux/Mac)
│   └── deploy.ps1                   # Déploiement (Windows)
├── env.template                     # Template variables d'environnement
├── JENKINS_SETUP.md                 # Guide configuration Jenkins
├── QUICK_START.md                   # Guide démarrage rapide
└── CI_CD_README.md                  # Ce fichier
```

---

## 🎯 Fonctionnalités du Pipeline

### ✅ Tests Automatisés

#### Backend (JUnit)
- ✅ Tests unitaires (UserService, AuthService, ConventionService)
- ✅ Tests d'intégration (AuthenticationIntegrationTest)
- ✅ Tests de contrôleurs (AuthControllerTest)
- ✅ Couverture de code (JaCoCo) - Objectif: 80%

#### Frontend (Karma/Jasmine)
- ✅ Tests unitaires (Components, Services)
- ✅ Tests d'intégration (LoginComponent)
- ✅ Couverture de code - Objectif: 80%
- ✅ Linting (ESLint)

### 🔒 Sécurité

- ✅ OWASP Dependency Check (Backend)
- ✅ npm audit (Frontend)
- ✅ Scan des vulnérabilités
- ✅ Health checks sur tous les services

### 🐳 Containerisation

- ✅ Multi-stage builds (optimisation taille images)
- ✅ Images légères (Alpine Linux)
- ✅ Non-root user (sécurité)
- ✅ Health checks intégrés

### 📊 Rapports

- ✅ Rapports de tests JUnit
- ✅ Rapports de couverture JaCoCo
- ✅ Rapports de couverture Karma
- ✅ Rapports de sécurité OWASP

---

## 🚀 Démarrage Rapide

### 1. Installation Jenkins

```bash
# Avec Docker (Recommandé)
docker run -d \
  --name jenkins \
  -p 8090:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17

# Récupérer le mot de passe initial
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 2. Configuration

1. **Accéder à Jenkins**: http://localhost:8090
2. **Installer les plugins requis**:
   - Docker Pipeline
   - Git
   - JUnit
   - JaCoCo
   - HTML Publisher
3. **Configurer les credentials**:
   - Docker Hub
   - GitHub/GitLab
   - Variables d'environnement

Voir [JENKINS_SETUP.md](JENKINS_SETUP.md) pour les détails complets.

### 3. Créer le Pipeline

1. **New Item** > **Pipeline**
2. **Configuration**:
   - Repository URL: `<your-repo-url>`
   - Script Path: `Jenkinsfile`
   - Branch: `*/main`
3. **Save** et **Build Now**

### 4. Déploiement Local

```bash
# Linux/Mac
./scripts/deploy.sh

# Windows
.\scripts\deploy.ps1

# Ou manuellement
docker-compose up -d
```

---

## 🧪 Exécuter les Tests

### Tests Backend

```bash
# Tous les tests
mvn clean test

# Tests unitaires uniquement
mvn test

# Tests d'intégration
mvn verify -Dtest=*Integration*

# Avec couverture
mvn clean test jacoco:report

# Rapport: target/site/jacoco/index.html
```

### Tests Frontend

```bash
cd app-frontend-new

# Tous les tests
npm test -- --watch=false --code-coverage

# Tests spécifiques
npm test -- --include='**/login.component.spec.ts'

# Linting
npm run lint

# Rapport: coverage/index.html
```

---

## 🐳 Docker Commands

### Build

```bash
# Build toutes les images
docker-compose build

# Build sans cache
docker-compose build --no-cache

# Build une image spécifique
docker-compose build backend
docker-compose build frontend
```

### Run

```bash
# Démarrer tous les services
docker-compose up -d

# Démarrer un service spécifique
docker-compose up -d backend

# Voir les logs
docker-compose logs -f

# Voir le status
docker-compose ps
```

### Stop

```bash
# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v

# Redémarrer un service
docker-compose restart backend
```

---

## 📊 Monitoring et Health Checks

### Endpoints de Santé

```bash
# Backend Health
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:80

# MongoDB
docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')"
```

### Logs

```bash
# Tous les logs
docker-compose logs

# Logs en temps réel
docker-compose logs -f

# Logs d'un service
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mongodb

# Dernières 100 lignes
docker-compose logs --tail=100 backend
```

### Métriques

```bash
# Utilisation des ressources
docker stats

# Espace disque
docker system df

# Nettoyer les ressources inutilisées
docker system prune -a
```

---

## 🔐 Variables d'Environnement

### Fichier .env

```bash
# Copier le template
cp env.template .env

# Éditer avec vos valeurs
nano .env
```

### Variables Requises

```env
# Docker Images
BACKEND_IMAGE=your-username/commercial-pfe-backend:latest
FRONTEND_IMAGE=your-username/commercial-pfe-frontend:latest

# MongoDB
MONGO_USERNAME=admin
MONGO_PASSWORD=your-secure-password

# JWT
JWT_SECRET=your-super-secret-key-min-256-bits
JWT_EXPIRATION=86400000

# Email (Brevo)
BREVO_EMAIL=your-email@example.com
BREVO_API_KEY=your-api-key

# SMS (Twilio)
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_PHONE_NUMBER=+1234567890
```

---

## 🌍 Environnements

### Development (Local)

```bash
docker-compose up -d
```

- Frontend: http://localhost:80
- Backend: http://localhost:8080
- MongoDB: mongodb://localhost:27017

### Testing

```bash
docker-compose -f docker-compose.test.yml up -d
```

- Backend: http://localhost:8081
- MongoDB: mongodb://localhost:27018

### Staging

```bash
docker-compose -f docker-compose.staging.yml up -d
```

- Frontend: http://staging.your-domain.com
- Backend: http://api-staging.your-domain.com

### Production

```bash
# À configurer selon votre infrastructure
# Kubernetes, AWS ECS, Azure Container Instances, etc.
```

---

## 🔄 Workflow Git

### Branches

```
main          → Production
develop       → Développement
feature/*     → Nouvelles fonctionnalités
hotfix/*      → Corrections urgentes
```

### Pipeline Triggers

- **Push sur main** → Build + Tests + Deploy Staging
- **Pull Request** → Build + Tests uniquement
- **Tag v*.\*.\*** → Build + Tests + Deploy Production

---

## 📈 Métriques de Qualité

### Objectifs

- ✅ **Couverture de code**: ≥ 80%
- ✅ **Tests réussis**: 100%
- ✅ **Build time**: < 10 minutes
- ✅ **Vulnérabilités**: 0 critiques
- ✅ **Disponibilité**: ≥ 99.9%

### Rapports

1. **Tests JUnit**: Jenkins > Build > Test Results
2. **Couverture JaCoCo**: Jenkins > Build > Coverage Report
3. **Couverture Frontend**: Jenkins > Build > HTML Reports
4. **Sécurité**: Jenkins > Build > OWASP Report

---

## 🐛 Troubleshooting

### Problème: Build échoue

```bash
# Vérifier les logs Jenkins
# Console Output dans Jenkins

# Nettoyer et rebuild
mvn clean install
docker-compose build --no-cache
```

### Problème: Tests échouent

```bash
# Backend
mvn clean test -X  # Mode debug

# Frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

### Problème: Docker out of memory

```bash
# Augmenter la mémoire Docker
# Docker Desktop > Settings > Resources > Memory: 4GB+

# Nettoyer les images inutilisées
docker system prune -a
```

### Problème: Port déjà utilisé

```bash
# Trouver le processus
lsof -i :8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows

# Changer le port dans docker-compose.yml
ports:
  - "8081:8080"  # Au lieu de 8080:8080
```

---

## 📚 Documentation

- [JENKINS_SETUP.md](JENKINS_SETUP.md) - Configuration complète Jenkins
- [QUICK_START.md](QUICK_START.md) - Démarrage rapide
- [env.template](env.template) - Variables d'environnement

---

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

---

## 📞 Support

- 📖 Documentation: Voir les fichiers MD
- 🐛 Issues: GitHub Issues
- 💬 Questions: Créer une discussion

---

## ✅ Checklist de Déploiement

### Avant le Premier Déploiement

- [ ] Jenkins installé et configuré
- [ ] Plugins Jenkins installés
- [ ] Credentials configurés (Docker Hub, GitHub)
- [ ] Variables d'environnement configurées
- [ ] Fichier .env créé avec les vraies valeurs
- [ ] Docker et Docker Compose installés
- [ ] Ports disponibles (80, 8080, 27017)
- [ ] Webhook GitHub/GitLab configuré

### Après le Déploiement

- [ ] Pipeline Jenkins exécuté avec succès
- [ ] Tests backend passent (100%)
- [ ] Tests frontend passent (100%)
- [ ] Couverture de code ≥ 80%
- [ ] Images Docker créées et pushées
- [ ] Application accessible (Frontend + Backend)
- [ ] Health checks OK
- [ ] Logs sans erreurs critiques

---

**✅ Votre pipeline CI/CD est maintenant opérationnel !**

Pour démarrer: Voir [QUICK_START.md](QUICK_START.md)
