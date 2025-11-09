# Variables d'Environnement pour Production

## Configuration Gemini AI

Pour déployer en production, utilisez des variables d'environnement au lieu de stocker les clés en clair.

### Windows (PowerShell)
```powershell
# Définir la variable d'environnement
$env:GEMINI_API_KEY = "AIzaSyCt96z6HULFne7HOT2YmIRKgp7XDIl0kDY"

# Ou de manière permanente
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'AIzaSyCt96z6HULFne7HOT2YmIRKgp7XDIl0kDY', 'User')
```

### Linux/Mac
```bash
# Définir la variable d'environnement
export GEMINI_API_KEY="AIzaSyCt96z6HULFne7HOT2YmIRKgp7XDIl0kDY"

# Ou ajouter dans ~/.bashrc ou ~/.zshrc
echo 'export GEMINI_API_KEY="AIzaSyCt96z6HULFne7HOT2YmIRKgp7XDIl0kDY"' >> ~/.bashrc
```

### Docker
```dockerfile
ENV GEMINI_API_KEY=AIzaSyCt96z6HULFne7HOT2YmIRKgp7XDIl0kDY
```

### application-prod.properties
```properties
gemini.api.key=${GEMINI_API_KEY}
```

## Sécurité

⚠️ **Important :**
- Ne jamais commiter les clés API dans Git
- Restreindre la clé API aux services nécessaires dans Google Cloud Console
- Utiliser des secrets managers en production (AWS Secrets Manager, Azure Key Vault, etc.)
- Activer la rotation automatique des clés
