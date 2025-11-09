# 🚀 QUICK START - Démarrage Rapide

## ⚡ Démarrage en 5 Minutes

### Prérequis

```bash
✅ Docker et Docker Compose installés
✅ Ports 80, 8080, 27017 disponibles
✅ 4GB RAM minimum
```

### Option 1: Démarrage Automatique (Recommandé)

#### Sur Linux/Mac:
```bash
# 1. Cloner le projet
git clone <your-repo-url>
cd commercial-pfe/demo

# 2. Copier le fichier d'environnement
cp env.template .env

# 3. Éditer .env avec vos vraies valeurs
nano .env

# 4. Rendre le script exécutable
chmod +x scripts/deploy.sh

# 5. Lancer le déploiement
./scripts/deploy.sh
```

#### Sur Windows (PowerShell):
```powershell
# 1. Cloner le projet
git clone <your-repo-url>
cd commercial-pfe\demo

# 2. Copier le fichier d'environnement
Copy-Item env.template .env

# 3. Éditer .env avec vos vraies valeurs
notepad .env

# 4. Lancer le déploiement
.\scripts\deploy.ps1
```

### Option 2: Démarrage Manuel

```bash
# 1. Créer le fichier .env
cp env.template .env

# 2. Éditer les variables d'environnement
nano .env

# 3. Démarrer tous les services
docker-compose up -d

# 4. Vérifier les logs
docker-compose logs -f
```

---

## 🧪 Exécuter les Tests

### Tests Backend

```bash
# Linux/Mac
chmod +x scripts/run-backend-tests.sh
./scripts/run-backend-tests.sh

# Windows
mvn clean test
```

### Tests Frontend

```bash
# Linux/Mac
chmod +x scripts/run-frontend-tests.sh
./scripts/run-frontend-tests.sh

# Windows
cd app-frontend-new
npm test -- --watch=false --code-coverage
```

---

## 🔍 Vérifier que Tout Fonctionne

### 1. Vérifier les Conteneurs

```bash
docker-compose ps
```

Vous devriez voir:
```
NAME                        STATUS
commercial-pfe-backend      Up (healthy)
commercial-pfe-frontend     Up (healthy)
commercial-pfe-mongodb      Up (healthy)
```

### 2. Tester les URLs

- **Frontend**: http://localhost:80
- **Backend API**: http://localhost:8080/actuator/health
- **MongoDB**: `mongodb://admin:password@localhost:27017`

### 3. Vérifier les Logs

```bash
# Tous les logs
docker-compose logs

# Logs en temps réel
docker-compose logs -f

# Logs d'un service spécifique
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mongodb
```

---

## 🐛 Problèmes Courants

### Problème: Port déjà utilisé

```bash
# Trouver le processus qui utilise le port
# Linux/Mac
lsof -i :8080
lsof -i :80

# Windows
netstat -ano | findstr :8080
netstat -ano | findstr :80

# Arrêter le processus ou changer le port dans docker-compose.yml
```

### Problème: Conteneur ne démarre pas

```bash
# Voir les logs détaillés
docker-compose logs backend

# Redémarrer le conteneur
docker-compose restart backend

# Rebuild l'image
docker-compose build --no-cache backend
docker-compose up -d backend
```

### Problème: MongoDB connection refused

```bash
# Vérifier que MongoDB est démarré
docker-compose ps mongodb

# Redémarrer MongoDB
docker-compose restart mongodb

# Attendre 10 secondes puis tester
sleep 10
docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')"
```

---

## 📊 Commandes Utiles

```bash
# Démarrer l'application
docker-compose up -d

# Arrêter l'application
docker-compose down

# Arrêter et supprimer les volumes (⚠️  supprime les données)
docker-compose down -v

# Voir les logs en temps réel
docker-compose logs -f

# Rebuild les images
docker-compose build

# Rebuild sans cache
docker-compose build --no-cache

# Redémarrer un service
docker-compose restart backend

# Exécuter une commande dans un conteneur
docker-compose exec backend bash
docker-compose exec frontend sh
docker-compose exec mongodb mongosh

# Voir l'utilisation des ressources
docker stats
```

---

## 🔐 Configuration Initiale

### 1. Créer un Utilisateur Admin

```bash
# Se connecter à MongoDB
docker-compose exec mongodb mongosh -u admin -p password --authenticationDatabase admin

# Dans le shell MongoDB
use convention_tracker

# Créer un utilisateur admin
db.users.insertOne({
  username: "admin",
  email: "admin@example.com",
  password: "$2a$10$...",  // Hash BCrypt du mot de passe
  roles: ["ROLE_ADMIN"],
  isActive: true,
  emailVerified: true,
  locked: false,
  createdAt: new Date(),
  updatedAt: new Date()
})
```

### 2. Tester la Connexion

1. Ouvrir http://localhost:80
2. Se connecter avec:
   - Username: `admin`
   - Password: `admin123` (ou celui que vous avez configuré)

---

## 📝 Prochaines Étapes

1. ✅ **Configurer Jenkins** - Voir [JENKINS_SETUP.md](JENKINS_SETUP.md)
2. ✅ **Configurer les Webhooks GitHub/GitLab**
3. ✅ **Configurer les notifications (Email/SMS)**
4. ✅ **Configurer le monitoring**
5. ✅ **Déployer en production**

---

## 🆘 Besoin d'Aide ?

- 📖 Documentation complète: [JENKINS_SETUP.md](JENKINS_SETUP.md)
- 🐛 Logs: `docker-compose logs -f`
- 🔍 Status: `docker-compose ps`
- 💬 Issues: Créer une issue sur GitHub

---

**✅ Votre application est maintenant prête à l'emploi !**
