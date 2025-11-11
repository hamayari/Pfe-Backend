# 🚀 Guide Complet: Jenkins CI/CD pour Backend & Frontend

## 📋 PARTIE 1: COMMANDES RAPIDES

### 1️⃣ Démarrer Jenkins avec Docker

```cmd
docker run -d --name jenkins -p 8090:8080 -p 50000:50000 -v jenkins-data:/var/jenkins_home -v //var/run/docker.sock:/var/run/docker.sock --restart unless-stopped --privileged jenkins/jenkins:lts-jdk17
```

### 2️⃣ Attendre 30 secondes et installer Docker CLI

```cmd
timeout /t 30 /nobreak
docker exec -u root jenkins bash -c "apt-get update && apt-get install -y docker.io"
```

### 3️⃣ Récupérer le mot de passe initial

```cmd
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 4️⃣ Ouvrir Jenkins

```cmd
start http://localhost:8090
```

---

## 📖 PARTIE 2: CONFIGURATION ÉTAPE PAR ÉTAPE

### ✅ Étape 1: Configuration Initiale de Jenkins

1. **Accéder à Jenkins**: Ouvrez http://localhost:8090
2. **Coller le mot de passe** récupéré à l'étape 3
3. **Installer les plugins suggérés** (cliquez sur "Install suggested plugins")
4. **Créer un utilisateur admin**:
   - Username: `admin`
   - Password: `admin123`
   - Full name: `Administrator`
   - Email: `admin@example.com`
5. Cliquez sur **Save and Continue** puis **Start using Jenkins**

---

### ✅ Étape 2: Installer les Plugins Nécessaires

1. Allez dans **Manage Jenkins** > **Plugins**
2. Cliquez sur **Available plugins**
3. Recherchez et cochez:
   - ✅ **Docker Pipeline**
   - ✅ **Docker**
   - ✅ **Git**
   - ✅ **Pipeline**
   - ✅ **Maven Integration** (pour le backend)
4. Cliquez sur **Install** (en bas de la page)
5. Cochez **Restart Jenkins when installation is complete**

---

### ✅ Étape 3: Configurer les Credentials Docker Hub

1. Allez dans **Manage Jenkins** > **Credentials**
2. Cliquez sur **System** > **Global credentials (unrestricted)**
3. Cliquez sur **Add Credentials** (en haut à gauche)
4. Remplissez le formulaire:
   - **Kind**: `Username with password`
   - **Scope**: `Global`
   - **Username**: `hamayari` (votre Docker Hub username)
   - **Password**: Votre Docker Hub password ou access token
   - **ID**: `dockerhub-credentials` ⚠️ **IMPORTANT: Utilisez exactement cet ID**
   - **Description**: `Docker Hub Credentials`
5. Cliquez sur **Create**

---

### ✅ Étape 4: Créer le Pipeline Backend

1. Sur le dashboard Jenkins, cliquez sur **New Item**
2. Remplissez:
   - **Enter an item name**: `backend-pipeline`
   - Sélectionnez **Pipeline**
   - Cliquez sur **OK**

3. Dans la configuration:
   - **Description**: `Pipeline CI/CD pour le Backend Spring Boot`
   
4. Section **Pipeline**:
   - **Definition**: Sélectionnez `Pipeline script from SCM`
   - **SCM**: Sélectionnez `Git`
   - **Repository URL**: `https://github.com/hamayari/Pfe-Backend.git`
   - **Credentials**: Ajoutez vos credentials GitHub si le repo est privé
   - **Branch Specifier**: `*/develop`
   - **Script Path**: `Jenkinsfile`

5. Cliquez sur **Save**

---

### ✅ Étape 5: Créer le Pipeline Frontend

1. Sur le dashboard Jenkins, cliquez sur **New Item**
2. Remplissez:
   - **Enter an item name**: `frontend-pipeline`
   - Sélectionnez **Pipeline**
   - Cliquez sur **OK**

3. Dans la configuration:
   - **Description**: `Pipeline CI/CD pour le Frontend Angular`
   
4. Section **Pipeline**:
   - **Definition**: Sélectionnez `Pipeline script from SCM`
   - **SCM**: Sélectionnez `Git`
   - **Repository URL**: `https://github.com/hamayari/Pfe-Frontend.git`
   - **Credentials**: Ajoutez vos credentials GitHub si le repo est privé
   - **Branch Specifier**: `*/develop`
   - **Script Path**: `Jenkinsfile`

5. Cliquez sur **Save**

---

### ✅ Étape 6: Tester les Pipelines

#### Test Backend:
1. Allez sur le pipeline **backend-pipeline**
2. Cliquez sur **Build Now**
3. Cliquez sur le numéro du build (ex: #1) pour voir les logs
4. Vérifiez que toutes les étapes passent au vert ✅

#### Test Frontend:
1. Allez sur le pipeline **frontend-pipeline**
2. Cliquez sur **Build Now**
3. Cliquez sur le numéro du build pour voir les logs
4. Vérifiez que toutes les étapes passent au vert ✅

---

## 🐳 PARTIE 3: VÉRIFIER LES IMAGES DOCKER

Après un build réussi, vérifiez que vos images sont sur Docker Hub:

```cmd
docker pull hamayari/commercial-pfe-backend:latest
docker pull hamayari/commercial-pfe-frontend:latest
```

Ou visitez:
- https://hub.docker.com/r/hamayari/commercial-pfe-backend
- https://hub.docker.com/r/hamayari/commercial-pfe-frontend

---

## 🔧 PARTIE 4: COMMANDES UTILES

### Voir les logs Jenkins:
```cmd
docker logs jenkins
```

### Redémarrer Jenkins:
```cmd
docker restart jenkins
```

### Arrêter Jenkins:
```cmd
docker stop jenkins
```

### Supprimer Jenkins (⚠️ Attention: supprime toutes les données):
```cmd
docker stop jenkins
docker rm jenkins
docker volume rm jenkins-data
```

### Accéder au shell Jenkins:
```cmd
docker exec -it jenkins bash
```

---

## 🎯 PARTIE 5: STRUCTURE DES PIPELINES

### Pipeline Backend (Jenkinsfile):
```
📥 Checkout → 🔨 Build (Maven) → 🐳 Build Docker → 📤 Push Docker Hub
```

### Pipeline Frontend (Jenkinsfile):
```
📥 Checkout → 🔨 Build (Node) → 🐳 Build Docker → 📤 Push Docker Hub
```

---

## ❓ DÉPANNAGE

### Problème: "docker: not found"
**Solution**: Vérifiez que Docker CLI est installé dans Jenkins:
```cmd
docker exec -u root jenkins bash -c "apt-get update && apt-get install -y docker.io"
```

### Problème: "Permission denied" sur Docker socket
**Solution**: Redémarrez Jenkins avec les bonnes permissions:
```cmd
docker stop jenkins
docker rm jenkins
# Puis relancez la commande de l'étape 1
```

### Problème: Build échoue avec "mvn: not found"
**Solution**: Le pipeline utilise un conteneur Maven, assurez-vous que Docker fonctionne correctement.

### Problème: "Credentials not found"
**Solution**: Vérifiez que l'ID des credentials est exactement `dockerhub-credentials`

---

## 📊 RÉSUMÉ DES ÉTAPES

1. ✅ Démarrer Jenkins avec Docker
2. ✅ Installer Docker CLI dans Jenkins
3. ✅ Configurer Jenkins (utilisateur admin)
4. ✅ Installer les plugins nécessaires
5. ✅ Ajouter les credentials Docker Hub
6. ✅ Créer le pipeline Backend
7. ✅ Créer le pipeline Frontend
8. ✅ Tester les deux pipelines
9. ✅ Vérifier les images sur Docker Hub

---

## 🎉 FÉLICITATIONS!

Vous avez maintenant un système CI/CD complet avec Jenkins pour votre application!

**URLs importantes**:
- Jenkins: http://localhost:8090
- Backend API: http://localhost:8080
- Frontend: http://localhost:4200
- Docker Hub: https://hub.docker.com/u/hamayari

---

## 📝 NOTES IMPORTANTES

- Les images Docker sont poussées uniquement sur la branche `develop`
- Chaque build crée une image avec un numéro de version unique
- L'image `latest` est toujours mise à jour avec le dernier build
- Les pipelines utilisent des conteneurs Docker pour isoler les builds
- Les artifacts (JAR, dist/) sont archivés automatiquement

---

**Besoin d'aide?** Consultez les logs Jenkins ou Docker pour plus de détails!
