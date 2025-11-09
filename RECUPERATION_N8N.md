# Guide de Récupération du Mot de Passe n8n

## Problème
Vous avez oublié le mot de passe de votre compte n8n et ne pouvez plus accéder aux workflows du chatbot.

## Solutions

### Solution 1 : Réinitialisation via Docker (Recommandé si n8n est dans Docker)

```bash
# Vérifier si n8n tourne dans Docker
docker ps | grep n8n

# Réinitialiser le mot de passe
docker exec -it n8n n8n user-management:reset --email=votre-email@example.com --password=NouveauMotDePasse123
```

### Solution 2 : Réinitialisation via npm (Si n8n est installé localement)

```bash
# Réinitialiser le mot de passe
n8n user-management:reset --email=votre-email@example.com --password=NouveauMotDePasse123
```

### Solution 3 : Supprimer et Recréer (⚠️ Perte des workflows)

**ATTENTION : Cette méthode supprime tous vos workflows !**

```powershell
# Localiser la base de données n8n
$n8nPath = "$env:USERPROFILE\.n8n\database.sqlite"

# Sauvegarder d'abord (optionnel)
Copy-Item $n8nPath "$n8nPath.backup"

# Supprimer la base de données
Remove-Item $n8nPath

# Redémarrer n8n - il créera un nouveau compte
```

### Solution 4 : Modification Directe de la Base de Données

1. **Télécharger DB Browser for SQLite**
   - https://sqlitebrowser.org/

2. **Localiser la base de données**
   - Windows : `C:\Users\VotreNom\.n8n\database.sqlite`
   - Linux/Mac : `~/.n8n/database.sqlite`

3. **Ouvrir avec DB Browser**
   - Ouvrir le fichier `database.sqlite`
   - Aller dans l'onglet "Browse Data"
   - Sélectionner la table `user`

4. **Générer un nouveau hash de mot de passe**
   ```javascript
   // Utiliser Node.js pour générer un hash bcrypt
   const bcrypt = require('bcrypt');
   const password = 'VotreNouveauMotDePasse';
   const hash = bcrypt.hashSync(password, 10);
   console.log(hash);
   ```

5. **Mettre à jour le champ `password`**
   - Remplacer le hash existant par le nouveau
   - Sauvegarder

## Script PowerShell Automatique

Exécutez le script fourni :

```powershell
.\reset-n8n-password.ps1
```

## Après la Réinitialisation

1. **Redémarrer n8n**
   ```bash
   # Docker
   docker restart n8n
   
   # npm
   # Arrêter avec Ctrl+C puis relancer
   n8n start
   ```

2. **Se connecter avec le nouveau mot de passe**
   - URL : http://localhost:5678
   - Email : votre-email@example.com
   - Mot de passe : NouveauMotDePasse123

3. **Vérifier les workflows du chatbot**
   - Aller dans "Workflows"
   - Vérifier que le workflow du chatbot existe
   - Activer le workflow si nécessaire

## Configuration du Chatbot dans application.properties

Assurez-vous que votre `application.properties` contient :

```properties
# Configuration n8n
n8n.webhook.url=http://localhost:5678/webhook/chatbot
n8n.api.url=http://localhost:5678/api/v1
n8n.api.key=votre-api-key-n8n
```

## Récupération de l'API Key n8n

1. Connectez-vous à n8n
2. Allez dans **Settings** → **API**
3. Créez une nouvelle API Key
4. Copiez-la dans `application.properties`

## Workflow du Chatbot

Si vous avez perdu le workflow, voici la structure de base :

### Webhook Trigger
- Method : POST
- Path : chatbot
- Response Mode : Last Node

### HTTP Request Node
- Method : POST
- URL : Votre API de chatbot (OpenAI, etc.)
- Headers : Authorization avec votre clé API

### Response Node
- Retourner la réponse du chatbot

## Prévention Future

1. **Sauvegarder régulièrement**
   ```bash
   # Sauvegarder la base de données
   cp ~/.n8n/database.sqlite ~/.n8n/database.sqlite.backup
   ```

2. **Exporter les workflows**
   - Dans n8n, aller dans chaque workflow
   - Cliquer sur "..." → "Download"
   - Sauvegarder le fichier JSON

3. **Utiliser un gestionnaire de mots de passe**
   - LastPass, 1Password, Bitwarden, etc.

4. **Documenter les credentials**
   - Créer un fichier `.env` sécurisé
   - Ne jamais le commiter dans Git

## Support

Si aucune solution ne fonctionne :
- Documentation n8n : https://docs.n8n.io/
- Forum n8n : https://community.n8n.io/
- GitHub Issues : https://github.com/n8n-io/n8n/issues
