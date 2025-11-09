# 📧 Configuration Brevo (Sendinblue) - Email Professionnel

## ✅ Configuration Appliquée

### Fichiers Modifiés

#### 1. `application.properties`
```properties
# Configuration Email (SMTP) - Brevo (Professionnel)
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=eyayari123@gmail.com
spring.mail.password=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
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp-relay.brevo.com
mail.from.address=noreply@gestionpro.com
mail.from.name=GestionPro
```

#### 2. `EmailService.java`
- ✅ Utilise l'email vérifié dans Brevo
- ✅ Nom d'expéditeur personnalisé: "GestionPro"
- ✅ Gestion des erreurs améliorée

---

## 🔧 Configuration Brevo

### Informations de Connexion

| Paramètre | Valeur |
|-----------|--------|
| **SMTP Server** | smtp-relay.brevo.com |
| **Port** | 587 |
| **Username** | eyayari123@gmail.com |
| **API Key** | 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 |
| **Encryption** | STARTTLS |

---

## 📋 Étapes de Configuration Brevo

### Étape 1: Vérifier l'Expéditeur ✅

1. Connectez-vous à https://app.brevo.com
2. Allez dans **Paramètres** → **Expéditeurs et domaines**
3. Vérifiez que `eyayari123@gmail.com` est validé
4. Si pas validé, cliquez sur le lien de vérification dans l'email reçu

### Étape 2: (Optionnel) Ajouter un Domaine Personnalisé

Pour utiliser `noreply@votredomaine.com` au lieu de `eyayari123@gmail.com`:

1. Dans Brevo, allez dans **Expéditeurs et domaines**
2. Cliquez sur **Ajouter un domaine**
3. Entrez votre domaine (ex: `gestionpro.com`)
4. Brevo vous donnera des enregistrements DNS à ajouter:
   - **SPF** (TXT)
   - **DKIM** (TXT)
   - **DMARC** (TXT)

5. Ajoutez ces enregistrements chez votre hébergeur DNS
6. Attendez la validation (quelques heures max)
7. Une fois validé, créez l'expéditeur `noreply@gestionpro.com`

### Étape 3: Limites du Plan Gratuit

**Plan Gratuit Brevo:**
- ✅ 300 emails/jour
- ✅ Emails transactionnels illimités
- ✅ SMTP relay inclus
- ✅ Statistiques de base

**Si vous dépassez 300 emails/jour:**
- Passez au plan Lite (19€/mois pour 20,000 emails)
- OU Business (49€/mois pour 40,000 emails)

---

## 🧪 Test de Configuration

### Test 1: Depuis l'Application

1. Redémarrez le backend:
```bash
cd demo
mvn spring-boot:run
```

2. Allez sur http://localhost:4200/auth/forgot-password
3. Entrez votre email: `eyayari123@gmail.com`
4. Cliquez sur "Envoyer le lien"
5. **Vérifier:** Email reçu dans votre boîte

### Test 2: Vérifier les Logs Backend

```
✅ Email envoyé avec succès à: eyayari123@gmail.com depuis eyayari123@gmail.com
```

### Test 3: Tableau de Bord Brevo

1. Allez sur https://app.brevo.com
2. Menu **Statistiques** → **Emails transactionnels**
3. Vous devriez voir votre email envoyé

---

## 📧 Templates d'Email

### Email de Réinitialisation de Mot de Passe

**Expéditeur:** GestionPro <eyayari123@gmail.com>  
**Sujet:** 🔐 Réinitialisation de votre mot de passe

**Contenu:**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                  color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
        .button { display: inline-block; padding: 15px 30px; background: #667eea; 
                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
        .footer { text-align: center; margin-top: 30px; color: #999; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔐 Réinitialisation de Mot de Passe</h1>
        </div>
        <div class="content">
            <p>Bonjour,</p>
            <p>Vous avez demandé la réinitialisation de votre mot de passe pour votre compte GestionPro.</p>
            <p>Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :</p>
            <center>
                <a href="http://localhost:4200/auth/reset-password?token={{TOKEN}}" class="button">
                    Réinitialiser mon mot de passe
                </a>
            </center>
            <p><strong>Ce lien est valide pendant 1 heure.</strong></p>
            <p>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>
            <p>Cordialement,<br>L'équipe GestionPro</p>
        </div>
        <div class="footer">
            <p>© 2025 GestionPro - Tous droits réservés</p>
            <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
        </div>
    </div>
</body>
</html>
```

---

## 🔒 Sécurité

### Bonnes Pratiques

1. ✅ **Ne jamais exposer la clé API** dans le code frontend
2. ✅ **Utiliser des variables d'environnement** en production
3. ✅ **Limiter les tentatives** de réinitialisation (max 3/heure)
4. ✅ **Token à usage unique** (supprimé après utilisation)
5. ✅ **Expiration courte** (1 heure)

### Variables d'Environnement (Production)

```bash
# .env
BREVO_SMTP_HOST=smtp-relay.brevo.com
BREVO_SMTP_PORT=587
BREVO_USERNAME=eyayari123@gmail.com
BREVO_API_KEY=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
MAIL_FROM_ADDRESS=noreply@gestionpro.com
MAIL_FROM_NAME=GestionPro
```

---

## 📊 Monitoring

### Tableau de Bord Brevo

**Métriques disponibles:**
- Emails envoyés
- Emails délivrés
- Emails ouverts
- Clics sur les liens
- Bounces (erreurs)
- Spam reports

**Accès:** https://app.brevo.com/statistics/email

---

## 🐛 Dépannage

### Problème 1: "Authentication failed"

**Cause:** Clé API incorrecte

**Solution:**
1. Vérifiez la clé API dans Brevo
2. Copiez-la exactement (sans espaces)
3. Mettez à jour `application.properties`
4. Redémarrez le backend

### Problème 2: "Sender not verified"

**Cause:** Email expéditeur non vérifié dans Brevo

**Solution:**
1. Allez dans Brevo → Expéditeurs
2. Vérifiez l'email via le lien reçu
3. Attendez la validation

### Problème 3: "Daily limit exceeded"

**Cause:** Limite de 300 emails/jour dépassée

**Solution:**
1. Attendez le lendemain
2. OU passez à un plan payant
3. OU utilisez un autre compte Brevo

### Problème 4: Email en spam

**Cause:** Domaine non authentifié

**Solution:**
1. Ajoutez votre domaine dans Brevo
2. Configurez SPF, DKIM, DMARC
3. Attendez la validation

---

## ✅ Checklist de Configuration

### Backend
- [x] `application.properties` mis à jour avec Brevo
- [x] `EmailService.java` configuré
- [x] Clé API Brevo ajoutée
- [ ] Backend redémarré

### Brevo
- [x] Compte créé
- [x] Email expéditeur vérifié
- [ ] (Optionnel) Domaine personnalisé ajouté
- [ ] (Optionnel) DNS configuré

### Tests
- [ ] Email de test envoyé
- [ ] Email reçu dans la boîte
- [ ] Lien de réinitialisation fonctionne
- [ ] Statistiques visibles dans Brevo

---

## 🎯 Résultat Attendu

Après configuration:
- ✅ Emails envoyés depuis `eyayari123@gmail.com` (ou votre domaine)
- ✅ Nom d'expéditeur: "GestionPro"
- ✅ Emails professionnels avec template HTML
- ✅ Statistiques dans le tableau de bord Brevo
- ✅ Pas de limite Gmail (300 emails/jour avec Brevo gratuit)
- ✅ Meilleure délivrabilité (moins de spam)

**Configuration professionnelle prête pour la production!** 🚀

