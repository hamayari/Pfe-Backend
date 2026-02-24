<div align="center">

# 🚀 Backend - Gestion Pro API

### API REST Complète pour la Gestion Commerciale & Facturation

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📋 Vue d'Ensemble

API backend robuste et scalable construite avec Spring Boot pour gérer les opérations commerciales, la facturation intelligente, et les communications en temps réel. Architecture microservices-ready avec plus de 70 endpoints REST.

---

## ✨ Fonctionnalités Clés

### 🔐 Authentification & Sécurité
- 🔑 **JWT Authentication** - Tokens sécurisés avec expiration
- 🛡️ **Spring Security** - Protection des endpoints par rôle
- 📱 **2FA (TOTP)** - Authentification à deux facteurs avec QR codes
- 🔒 **Bcrypt Hashing** - Chiffrement sécurisé des mots de passe
- 👥 **RBAC** - Contrôle d'accès basé sur les rôles (6 rôles)
- 📝 **Audit Logging** - Traçabilité complète des actions

### 📊 Gestion des Conventions
- ✅ **CRUD Complet** - Création, lecture, mise à jour, suppression
- 🔄 **Cycle de Vie** - Gestion des statuts (DRAFT, ACTIVE, COMPLETED, CANCELLED)
- 📅 **Échéances** - Suivi automatique des dates limites
- 🗺️ **Géolocalisation** - Organisation par zones géographiques et gouvernorats
- 💰 **Termes de Paiement** - Configuration flexible des conditions
- 📜 **Historique** - Audit trail complet avec versioning

### 💳 Facturation Intelligente
- 🤖 **Génération Automatique** - Numérotation et création de factures
- 🔍 **OCR Tesseract** - Extraction automatique des données de paiement
- ✅ **Validation Automatique** - Matching intelligent des preuves de paiement
- 💵 **Paiements Partiels** - Support des paiements échelonnés
- 📧 **Rappels Automatiques** - Notifications programmées pour les retards
- 📊 **Suivi en Temps Réel** - Dashboard des statuts de paiement
- 📄 **Export PDF** - Génération de factures professionnelles

### 🔔 Système de Notifications Multi-Canal
- 📧 **Email (Brevo SMTP)** - Templates personnalisables
- 📱 **SMS (Twilio)** - Notifications instantanées
- 🔔 **Push Web** - Notifications navigateur en temps réel
- 💬 **In-App** - Notifications intégrées à l'application
- ⚙️ **Préférences Utilisateur** - Configuration par canal et type
- 📊 **Analytics** - Statistiques d'envoi et de lecture
- 🕐 **Planification** - Envoi différé et récurrent

### 💬 Messagerie Temps Réel
- 🔌 **WebSocket + STOMP** - Communication bidirectionnelle
- 💬 **Chat Type Slack** - Conversations, threads, mentions
- 📎 **Pièces Jointes** - Upload et partage de fichiers
- 👍 **Réactions** - Emojis et interactions
- 📌 **Épinglage** - Messages importants
- ⌨️ **Typing Indicators** - Indicateurs de frappe
- 📜 **Historique** - Recherche et archivage

### 📈 KPI & Alertes Intelligentes
- 📊 **Monitoring Temps Réel** - Suivi des métriques business
- 🚨 **Alertes Automatiques** - Déclenchement sur seuils
- 🔄 **Délégation** - Escalade hiérarchique des alertes
- 📉 **Détection d'Anomalies** - Analyse prédictive
- 💻 **Monitoring Système** - CPU, RAM, Disque
- 📧 **Notifications Multi-Canal** - Email + SMS pour alertes critiques

### 🎨 Dashboards Analytiques
- 👨‍💼 **Admin Dashboard** - Vue système complète, gestion utilisateurs
- 💼 **Commercial Dashboard** - Métriques ventes, pipeline, revenus
- 🎯 **Décideur Dashboard** - KPIs stratégiques, heatmaps régionales
- 📋 **Chef de Projet Dashboard** - Suivi projets, équipes, tâches

### 🤖 Intelligence Artificielle
- 🧠 **Chatbot NLP** - Intégration Gemini AI
- 💬 **Compréhension Naturelle** - Traitement du langage
- 📊 **Génération de Rapports** - Insights automatiques
- 🔮 **Analyses Prédictives** - Forecasting et tendances

### 📅 Gestion Avancée
- 📆 **Calendrier** - Événements et rappels
- ✅ **Tâches** - Gestion de projets intégrée
- 📄 **Génération de Rapports** - PDF, Excel, CSV
- 💳 **Intégration Stripe** - Webhooks de paiement
- 🔍 **Recherche Avancée** - Full-text search MongoDB

---

## 🛠️ Stack Technique

### Core Framework
```
☕ Java 17
🍃 Spring Boot 3.2.0
🗄️ MongoDB 7.0
🔧 Maven 3.8+
```

### Sécurité
```
🔐 Spring Security
🎫 JWT (JJWT 0.11.5)
🔑 TOTP (2FA)
📱 Google ZXing (QR Codes)
```

### Communication
```
🔌 Spring WebSocket
📡 STOMP Protocol
📧 Spring Mail + Brevo
📱 Twilio SDK 8.31.1
```

### Intégrations
```
💳 Stripe Java 24.6.0
🔍 Tesseract OCR 5.4.0
📄 iText PDF 7.2.5
📊 Apache POI 5.2.3
🤖 Gemini AI API
```

### DevOps & Qualité
```
🐳 Docker + Docker Compose
🔄 Jenkins CI/CD
📊 SonarQube
✅ JUnit + Mockito
📈 JaCoCo (Code Coverage)
📚 Swagger/OpenAPI 3.0
```

---

## 📁 Architecture

```
src/main/java/com/example/demo/
├── 📂 config/              # 20+ configurations (Security, WebSocket, Mail, etc.)
├── 📂 controller/          # 70+ REST endpoints
├── 📂 service/             # 95+ services métier
├── 📂 model/               # 44 entités MongoDB
├── 📂 repository/          # 30+ repositories Spring Data
├── 📂 security/            # JWT, 2FA, WebSocket auth
├── 📂 scheduler/           # Tâches planifiées (Cron)
├── 📂 notification/        # Système multi-canal
├── 📂 dto/                 # Data Transfer Objects
├── 📂 exception/           # Gestion centralisée des erreurs
└── 📂 util/                # Utilitaires et helpers
```

---

## 🚀 Démarrage Rapide

### Prérequis
- Java 17+
- Maven 3.8+
- MongoDB 7.0+
- Docker (optionnel)

### Installation

```bash
# Cloner le repository
git clone https://github.com/hamayari/Pfe-Backend.git
cd Pfe-Backend

# Installer les dépendances
mvn clean install

# Lancer l'application
mvn spring-boot:run
```

L'API sera accessible sur `http://localhost:8080`

### Docker

```bash
# Build l'image
docker build -t gestion-pro-backend .

# Lancer avec Docker Compose
docker-compose up -d
```

---

## ⚙️ Configuration

### application.properties

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/gestion_pro

# JWT
jwt.secret=YOUR_SECRET_KEY_HERE
jwt.expiration=86400000

# Email (Brevo)
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL
spring.mail.password=YOUR_BREVO_API_KEY

# SMS (Twilio)
twilio.account.sid=YOUR_ACCOUNT_SID
twilio.auth.token=YOUR_AUTH_TOKEN
twilio.phone.number=YOUR_TWILIO_NUMBER

# Stripe
stripe.api.key=YOUR_STRIPE_SECRET_KEY
stripe.webhook.secret=YOUR_WEBHOOK_SECRET
```

---

## 📚 Documentation API

### Swagger UI
Une fois l'application lancée, accédez à la documentation interactive:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principaux

#### 🔐 Authentification
```
POST   /api/auth/signup          # Inscription
POST   /api/auth/login           # Connexion
POST   /api/auth/2fa/enable      # Activer 2FA
POST   /api/auth/2fa/verify      # Vérifier code 2FA
POST   /api/auth/refresh         # Rafraîchir token
```

#### 📊 Conventions
```
GET    /api/conventions          # Liste des conventions
POST   /api/conventions          # Créer une convention
GET    /api/conventions/{id}     # Détails d'une convention
PUT    /api/conventions/{id}     # Modifier une convention
DELETE /api/conventions/{id}     # Supprimer une convention
GET    /api/conventions/{id}/history  # Historique
```

#### 💳 Factures
```
GET    /api/invoices             # Liste des factures
POST   /api/invoices             # Créer une facture
PUT    /api/invoices/{id}/pay    # Enregistrer un paiement
POST   /api/invoices/{id}/proof  # Upload preuve de paiement
GET    /api/invoices/overdue     # Factures en retard
```

#### 🔔 Notifications
```
GET    /api/notifications        # Liste des notifications
POST   /api/notifications/send   # Envoyer une notification
PUT    /api/notifications/{id}/read  # Marquer comme lu
GET    /api/notifications/preferences  # Préférences utilisateur
```

#### 💬 Messagerie
```
WS     /ws/chat                  # WebSocket endpoint
GET    /api/messages             # Historique des messages
POST   /api/messages             # Envoyer un message
POST   /api/messages/{id}/react  # Ajouter une réaction
```

---

## 👥 Rôles & Permissions

| Rôle | Description | Permissions |
|------|-------------|-------------|
| 🔴 **SUPER_ADMIN** | Administrateur système | Accès complet |
| 🟠 **ADMIN** | Administrateur | Gestion utilisateurs, monitoring |
| 🟢 **COMMERCIAL** | Commercial | Conventions, factures, clients |
| 🔵 **DECISION_MAKER** | Décideur | Vue stratégique, KPIs, analytics |
| 🟣 **PROJECT_MANAGER** | Chef de projet | Projets, tâches, équipes |
| ⚪ **USER** | Utilisateur | Accès basique lecture seule |

---

## 🧪 Tests

### Lancer les tests
```bash
# Tests unitaires
mvn test

# Tests d'intégration
mvn verify

# Rapport de couverture
mvn jacoco:report
```

### Couverture de Code
- Tests unitaires: 95+ services testés
- Tests d'intégration: Embedded MongoDB
- JaCoCo: Rapport HTML dans `target/site/jacoco/`

---

## 📊 Monitoring & Santé

### Actuator Endpoints
```
GET /actuator/health          # État de santé
GET /actuator/metrics         # Métriques système
GET /actuator/info            # Informations application
```

### Prometheus & Grafana
```bash
# Lancer le monitoring
docker-compose -f docker-compose.monitoring.yml up -d
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

---

## 🔒 Sécurité

### Bonnes Pratiques Implémentées
- ✅ JWT avec expiration et refresh tokens
- ✅ Authentification 2FA (TOTP)
- ✅ Hachage bcrypt des mots de passe
- ✅ Protection CSRF
- ✅ CORS configuré
- ✅ Validation des entrées (JSoup, Apache Commons Text)
- ✅ Rate limiting sur les endpoints sensibles
- ✅ Audit logging complet
- ✅ Chiffrement des données sensibles

---

## 📈 Performance

### Optimisations
- 🚀 Indexation MongoDB optimisée
- 💾 Cache Redis pour les données fréquentes
- 📊 Pagination sur tous les endpoints de liste
- 🔄 Lazy loading des relations
- ⚡ Requêtes asynchrones pour les notifications
- 📦 Compression GZIP des réponses

---

## 🐳 Déploiement

### Docker Production
```bash
# Build pour production
mvn clean package -Pprod

# Lancer avec Docker
docker-compose -f docker-compose.prod.yml up -d
```

### Variables d'Environnement
```bash
SPRING_PROFILES_ACTIVE=prod
MONGODB_URI=mongodb://mongo:27017/gestion_pro
JWT_SECRET=your_production_secret
BREVO_API_KEY=your_brevo_key
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
STRIPE_API_KEY=your_stripe_key
```

---

## 📝 Logs

### Configuration Logback
```xml
<!-- Logs dans target/logs/ -->
- application.log      # Logs généraux
- error.log           # Erreurs uniquement
- audit.log           # Audit trail
```

### Niveaux de Log
```properties
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## 🤝 Contribution

Les contributions sont les bienvenues! Veuillez suivre ces étapes:

1. Fork le projet
2. Créez votre branche (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

---


### ⭐ Si ce projet vous aide, n'hésitez pas à lui donner une étoile!

**Développé avec ❤️ par l'équipe Gestion Pro**

</div>
