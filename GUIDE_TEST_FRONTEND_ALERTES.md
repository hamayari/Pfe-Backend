# 🎨 GUIDE DE TEST - Interface Frontend Gestion des Alertes KPI

## ✅ Ce Qui a Été Créé

### **Fichiers Frontend:**
1. ✅ `kpi-alert.service.ts` - Service API
2. ✅ `kpi-alert-management.component.ts` - Composant principal
3. ✅ `kpi-alert-management.component.html` - Template
4. ✅ `kpi-alert-management.component.scss` - Styles
5. ✅ Route ajoutée dans `app.routes.ts`
6. ✅ Intégration dans Dashboard Chef de Projet
7. ✅ Intégration dans Dashboard Décideur

---

## 🚀 DÉMARRAGE

### **Étape 1: Démarrer le Backend**
```bash
cd c:/Users/eyaya/OneDrive/Desktop/commercial-pfe/demo
mvn spring-boot:run
```

**Attendez de voir:**
```
Started DemoApplication in X seconds
```

### **Étape 2: Démarrer le Frontend**
```bash
cd c:/Users/eyaya/OneDrive/Desktop/commercial-pfe/demo/app-frontend-new
ng serve
```

**Attendez de voir:**
```
✔ Compiled successfully
```

### **Étape 3: Se Connecter**

**Option A: Chef de Projet**
```
URL: http://localhost:4200/auth/login-project-manager
Username: projectmanager
Password: pm123456
```

**Option B: Décideur**
```
URL: http://localhost:4200/auth/login-decision-maker
Username: decisionmaker
Password: dm123456
```

---

## 📱 ACCÈS À L'INTERFACE

### **Méthode 1: Via le Menu Latéral**

**Chef de Projet:**
1. Connectez-vous comme Chef de Projet
2. Dans le menu latéral gauche, cliquez sur:
   ```
   🔔 Gestion Alertes KPI
   ```

**Décideur:**
1. Connectez-vous comme Décideur
2. Dans le menu latéral gauche, cliquez sur:
   ```
   🔔 Gestion Alertes KPI
   ```

### **Méthode 2: URL Directe**
```
http://localhost:4200/kpi-alerts
```

---

## 🧪 SCÉNARIO DE TEST COMPLET

### **TEST 1: Créer des Alertes Automatiquement**

**Étape 1: Déclencher la vérification**
```bash
# Dans un navigateur (nouvel onglet):
http://localhost:8080/api/kpi-alerts/check-now
```

**Résultat attendu:**
```json
{
  "status": "success",
  "message": "Vérification des KPI effectuée avec succès"
}
```

**Console Backend:**
```
========================================
🔍 [AUTO KPI] Vérification automatique des KPI
📊 Taux de retard calculé: 58.3% (7/12)
💰 Montant impayé calculé: 45000.00 TND
🚨 Anomalie détectée: TAUX_RETARD = 58.3
💾 Alerte sauvegardée dans MongoDB: 67890abc...
✅ Notification envoyée au topic général
========================================
```

**Étape 2: Vérifier dans l'interface**

Retournez sur `http://localhost:4200/kpi-alerts`

**Vous devriez voir:**
- 📊 **Statistiques en haut:**
  - Nouvelles: 3
  - En cours: 0
  - Résolues: 0
  - Archivées: 0

- 📋 **Onglet "Alertes Actives":**
  - 3 cartes d'alertes avec:
    - Icône rouge 🔴
    - Message: "Taux de retard à 58.3%..."
    - Badge: HIGH
    - Statut: Nouvelle
    - Recommandation
    - Boutons: Prendre en charge, Commenter, Résoudre, Historique

---

### **TEST 2: Prendre en Charge une Alerte**

**Étape 1: Cliquer sur "Prendre en charge"**

**Étape 2: Ajouter un commentaire**
```
Je m'occupe de cette alerte immédiatement
```

**Résultat attendu:**
- ✅ Message de succès: "Alerte prise en charge avec succès"
- 📊 Statistiques mises à jour:
  - Nouvelles: 2
  - En cours: 1
- 🏷️ Badge de l'alerte change: "En cours"
- 📝 Bouton "Prendre en charge" disparaît

---

### **TEST 3: Ajouter un Commentaire**

**Étape 1: Cliquer sur "Commenter"**

**Étape 2: Ajouter un commentaire**
```
Contacté 5 clients, 3 ont confirmé le paiement
```

**Résultat attendu:**
- ✅ Message: "💬 Commentaire ajouté avec succès"
- 📜 Commentaire visible dans l'historique

---

### **TEST 4: Voir l'Historique**

**Étape 1: Cliquer sur "Historique"**

**Résultat attendu:**
- 📱 Modal s'ouvre avec timeline
- 📋 Actions visibles:
  ```
  1. CREATED
     Par: System
     Le: 23/10/2025 à 06:00
     Alerte créée automatiquement
     
  2. IN_PROGRESS
     Par: Jean Dupont
     Le: 23/10/2025 à 06:15
     Je m'occupe de cette alerte
     NEW → IN_PROGRESS
     
  3. COMMENTED
     Par: Jean Dupont
     Le: 23/10/2025 à 06:30
     Contacté 5 clients...
  ```

---

### **TEST 5: Résoudre une Alerte**

**Étape 1: Cliquer sur "Résoudre"**

**Étape 2: Remplir le formulaire**

**Commentaire de résolution (obligatoire):**
```
Problème résolu après contact avec tous les clients en retard
```

**Actions prises (optionnel):**
```
1. Contacté 7 clients en retard
2. Négocié nouveaux délais de paiement
3. Reçu 5 paiements immédiats
4. Mis en place rappels automatiques
```

**Résultat attendu:**
- ✅ Message: "✅ Alerte résolue avec succès"
- 📊 Statistiques mises à jour:
  - Nouvelles: 2
  - En cours: 0
  - Résolues: 1
- 🗂️ Alerte disparaît de "Alertes Actives"
- ✅ Alerte apparaît dans "Alertes Résolues"

---

### **TEST 6: Consulter les Alertes Résolues**

**Étape 1: Cliquer sur l'onglet "Alertes Résolues"**

**Résultat attendu:**
- 📋 Liste des alertes résolues (7 derniers jours)
- ✅ Icône verte
- 📝 Informations:
  - Résolu par: Jean Dupont
  - Résolu le: 23/10/2025 à 10:00
  - Commentaire de résolution
  - Actions prises
- 🔘 Boutons: Voir l'historique, Archiver

---

### **TEST 7: Archiver une Alerte**

**Étape 1: Dans "Alertes Résolues", cliquer sur "Archiver"**

**Étape 2: Confirmer**
```
Êtes-vous sûr de vouloir archiver cette alerte ?
[Oui] [Non]
```

**Résultat attendu:**
- ✅ Message: "📦 Alerte archivée avec succès"
- 📊 Statistiques mises à jour:
  - Résolues: 0
  - Archivées: 1
- 🗂️ Alerte disparaît de "Alertes Résolues"
- 📦 Alerte apparaît dans "Historique"

---

### **TEST 8: Consulter l'Historique Complet**

**Étape 1: Cliquer sur l'onglet "Historique"**

**Résultat attendu:**
- 📦 Liste de toutes les alertes archivées
- 📅 Triées par date d'archivage (plus récentes en premier)
- 📜 Bouton "Voir l'historique complet" pour chaque alerte

---

### **TEST 9: Rafraîchissement Automatique**

**Attendez 30 secondes sans rien faire**

**Résultat attendu:**
- 🔄 Les données se rafraîchissent automatiquement
- 📊 Statistiques mises à jour
- 📋 Nouvelles alertes apparaissent si détectées

---

### **TEST 10: Vérification Manuelle**

**Étape 1: Cliquer sur "Vérifier maintenant" (bouton en haut)**

**Résultat attendu:**
- ✅ Message: "🔍 Vérification des KPI lancée"
- ⏳ Attente 2 secondes
- 🔄 Données rafraîchies
- 📋 Nouvelles alertes apparaissent si anomalies détectées

---

## 🎨 INTERFACE UTILISATEUR

### **Vue d'ensemble**

```
┌─────────────────────────────────────────────────────────────┐
│  🔔 Gestion des Alertes KPI          [🔄 Vérifier maintenant] │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐                   │
│  │  5   │  │  3   │  │  12  │  │  45  │                   │
│  │Nouv. │  │En c. │  │Résol.│  │Arch. │                   │
│  └──────┘  └──────┘  └──────┘  └──────┘                   │
│                                                              │
│  [Alertes Actives (5)] [Alertes Résolues] [Historique]     │
│  ┌────────────────────────────────────────────────────┐    │
│  │ 🔴 Taux de retard: 58.3%                  [HIGH]   │    │
│  │ Détecté: Il y a 2h                       [Nouvelle]│    │
│  │ Recommandation: Contacter immédiatement...         │    │
│  │ [Prendre en charge] [Commenter] [Résoudre]         │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ CHECKLIST DE VÉRIFICATION

### **Backend**
- [ ] Backend démarré
- [ ] Endpoint `/api/kpi-alerts/check-now` accessible
- [ ] Alertes créées dans MongoDB
- [ ] WebSocket connecté

### **Frontend**
- [ ] Frontend démarré
- [ ] Route `/kpi-alerts` accessible
- [ ] Menu "Gestion Alertes KPI" visible dans Chef de Projet
- [ ] Menu "Gestion Alertes KPI" visible dans Décideur
- [ ] Service `KpiAlertService` fonctionne

### **Fonctionnalités**
- [ ] Affichage des alertes actives
- [ ] Statistiques correctes
- [ ] Prendre en charge fonctionne
- [ ] Ajouter commentaire fonctionne
- [ ] Résoudre alerte fonctionne
- [ ] Voir historique fonctionne
- [ ] Archiver alerte fonctionne
- [ ] Rafraîchissement automatique (30s)
- [ ] Vérification manuelle fonctionne

### **Interface**
- [ ] Design professionnel
- [ ] Animations fluides
- [ ] Responsive (mobile/desktop)
- [ ] Messages de succès/erreur
- [ ] Timeline historique claire

---

## 🐛 DÉPANNAGE

### **Problème: Page blanche**
**Solution:**
```bash
# Vérifier la console navigateur (F12)
# Vérifier que le composant est bien créé
# Redémarrer le frontend: Ctrl+C puis ng serve
```

### **Problème: Aucune alerte**
**Solution:**
```bash
# Déclencher manuellement:
http://localhost:8080/api/kpi-alerts/check-now

# Vérifier les logs backend
# Vérifier MongoDB
```

### **Problème: Erreur 401 (Non autorisé)**
**Solution:**
```bash
# Se reconnecter
# Vérifier le token dans localStorage
# Vérifier les rôles dans app.routes.ts
```

---

## 🎯 RÉSUMÉ

✅ **Interface complète** pour gérer les alertes KPI  
✅ **3 onglets**: Actives, Résolues, Historique  
✅ **Cycle de vie complet**: NEW → IN_PROGRESS → RESOLVED → ARCHIVED  
✅ **Traçabilité totale**: Historique de toutes les actions  
✅ **Intégration**: Chef de Projet + Décideur  
✅ **Rafraîchissement automatique**: Toutes les 30 secondes  
✅ **Design professionnel**: Style moderne et responsive  

**Votre système de gestion des alertes KPI est maintenant opérationnel!** 🚀
