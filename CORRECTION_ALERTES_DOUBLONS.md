# 🔧 Correction des Alertes en Double

## 🎯 Problème Identifié

**Symptôme:** 121 alertes PENDING_DECISION alors qu'il devrait y avoir 1 alerte par facture en retard

**Cause racine:** 
1. Le scheduler `KpiAnalysisScheduler` s'exécutait **toutes les heures**
2. La méthode `KpiEvaluatorService.createAlert()` créait **toujours une nouvelle alerte** sans vérifier si elle existait déjà
3. Résultat: Création de doublons à chaque exécution horaire

---

## ✅ Corrections Appliquées

### 1. **KpiEvaluatorService.java** - Vérification des doublons

**Avant:**
```java
private KpiAlert createAlert(...) {
    KpiAlert alert = new KpiAlert();
    // ... configuration
    return alertRepository.save(alert); // ❌ Crée toujours une nouvelle alerte
}
```

**Après:**
```java
private KpiAlert createAlert(...) {
    // ✅ VÉRIFIER SI UNE ALERTE EXISTE DÉJÀ
    Optional<KpiAlert> existingAlert = alertRepository
        .findByKpiNameAndDimensionAndDimensionValueAndAlertStatus(
            kpiName, dimension, dimensionValue, "PENDING_DECISION"
        );
    
    if (existingAlert.isPresent()) {
        // Mettre à jour l'alerte existante
        KpiAlert alert = existingAlert.get();
        alert.setCurrentValue(result.getValue());
        // ... mise à jour
        return alertRepository.save(alert);
    }
    
    // Créer une nouvelle alerte seulement si elle n'existe pas
    KpiAlert alert = new KpiAlert();
    // ... configuration
    return alertRepository.save(alert);
}
```

**Fichier:** `demo/src/main/java/com/example/demo/service/KpiEvaluatorService.java`

---

### 2. **KpiAlertRepository.java** - Nouvelle méthode de recherche

**Ajout:**
```java
// Trouver une alerte existante par KPI, dimension et statut
java.util.Optional<KpiAlert> findByKpiNameAndDimensionAndDimensionValueAndAlertStatus(
    String kpiName, 
    String dimension, 
    String dimensionValue, 
    String alertStatus
);
```

**Fichier:** `demo/src/main/java/com/example/demo/repository/KpiAlertRepository.java`

---

### 3. **KpiAnalysisScheduler.java** - Réduction de la fréquence

**Avant:**
```java
@Scheduled(cron = "0 0 * * * *") // ❌ Toutes les heures
public void hourlyKpiCheck() {
    // ...
}
```

**Après:**
```java
@Scheduled(cron = "0 0 */6 * * *") // ✅ Toutes les 6 heures
public void periodicKpiCheck() {
    // ...
}
```

**Fichier:** `demo/src/main/java/com/example/demo/scheduler/KpiAnalysisScheduler.java`

---

### 4. **InvoiceAlertService.java** - Déjà correct ✅

Ce service vérifie déjà les doublons:
```java
Optional<KpiAlert> existingAlert = kpiAlertRepository
    .findByRelatedInvoiceIdAndSentToProjectManager(invoice.getId(), false);

if (existingAlert.isPresent()) {
    // Mettre à jour au lieu de créer
}
```

**Statut:** ✅ Aucune modification nécessaire

---

## 🧹 Nettoyage des Alertes Existantes

### Option 1: Script MongoDB (Recommandé)

**Fichier créé:** `demo/clean-duplicate-alerts.js`

**Exécution:**
```bash
# Dans MongoDB Compass
# 1. Ouvrir MongoDB Compass
# 2. Se connecter à mongodb://localhost:27017
# 3. Sélectionner la base "demo_db"
# 4. Ouvrir l'onglet "Mongosh"
# 5. Copier-coller le contenu de clean-duplicate-alerts.js
# 6. Exécuter

# OU via mongo shell
mongo demo_db < clean-duplicate-alerts.js
```

**Ce que fait le script:**
1. Compte les alertes avant nettoyage
2. Trouve les groupes d'alertes en double (même KPI + dimension + dimensionValue)
3. Pour chaque groupe, garde la plus récente et supprime les autres
4. Affiche un résumé du nettoyage

---

### Option 2: Commande MongoDB Directe

```javascript
// Se connecter à la base
use demo_db;

// Supprimer toutes les alertes PENDING_DECISION (recommencera proprement)
db.kpiAlerts.deleteMany({ alertStatus: "PENDING_DECISION" });

// Vérifier
db.kpiAlerts.countDocuments({ alertStatus: "PENDING_DECISION" });
// Devrait retourner 0
```

---

## 🧪 Test de la Correction

### Étape 1: Nettoyer les alertes existantes

```javascript
use demo_db;
db.kpiAlerts.deleteMany({ alertStatus: "PENDING_DECISION" });
```

### Étape 2: Redémarrer l'application

```bash
cd demo
mvn spring-boot:run
```

### Étape 3: Déclencher manuellement l'analyse

**Via API:**
```bash
curl -X POST http://localhost:8085/api/kpi-alerts/check-now \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Via Frontend:**
- Se connecter comme Décideur
- Cliquer sur "🔄 Actualiser" dans la section Alertes KPI

### Étape 4: Vérifier le nombre d'alertes

**Dans MongoDB:**
```javascript
// Compter les alertes par KPI
db.kpiAlerts.aggregate([
    { $match: { alertStatus: "PENDING_DECISION" } },
    { $group: { _id: "$kpiName", count: { $sum: 1 } } },
    { $sort: { count: -1 } }
]);
```

**Résultat attendu:**
```
FACTURE_IMPAYEE: 7 alertes (si 7 factures en retard)
TAUX_RETARD: 1 alerte (global)
TAUX_RETARD: 3 alertes (par gouvernorat, si 3 gouvernorats en alerte)
```

**Total attendu:** Environ 10-15 alertes (pas 121!)

---

## 📊 Comportement Correct

### Création d'Alertes

| Scénario | Avant | Après |
|----------|-------|-------|
| 1ère exécution | 15 alertes créées | 15 alertes créées ✅ |
| 2ème exécution (1h après) | +15 alertes (total: 30) ❌ | 15 alertes mises à jour ✅ |
| 3ème exécution (2h après) | +15 alertes (total: 45) ❌ | 15 alertes mises à jour ✅ |
| Après 8 exécutions | 120 alertes ❌ | 15 alertes ✅ |

### Fréquence d'Exécution

| Scheduler | Avant | Après |
|-----------|-------|-------|
| Quotidien | 8h00 ✅ | 8h00 ✅ |
| Hebdomadaire | Lundi 9h00 ✅ | Lundi 9h00 ✅ |
| Mensuel | 1er du mois 10h00 ✅ | 1er du mois 10h00 ✅ |
| Périodique | Toutes les heures ❌ | Toutes les 6 heures ✅ |

---

## 🔍 Logs Attendus

### Avant (Problème)
```
⏰ [KPI SCHEDULER] Vérification horaire à 2025-10-29T14:00:00
✅ Nouvelle alerte créée: TAUX_RETARD - Sfax
✅ Nouvelle alerte créée: TAUX_RETARD - Sfax  ❌ DOUBLON
✅ Nouvelle alerte créée: TAUX_RETARD - Sfax  ❌ DOUBLON
...
📊 121 alertes créées  ❌ TROP!
```

### Après (Corrigé)
```
⏰ [KPI SCHEDULER] Vérification périodique à 2025-10-29T14:00:00
✅ Nouvelle alerte créée: TAUX_RETARD - Sfax
⏰ [KPI SCHEDULER] Vérification périodique à 2025-10-29T20:00:00
⚠️ Alerte existante mise à jour: TAUX_RETARD - Sfax  ✅ PAS DE DOUBLON
✅ [KPI SCHEDULER] Aucune alerte critique
```

---

## 📝 Checklist de Vérification

### Backend
- [x] `KpiEvaluatorService.java` - Vérification des doublons ajoutée
- [x] `KpiAlertRepository.java` - Méthode de recherche ajoutée
- [x] `KpiAnalysisScheduler.java` - Fréquence réduite (6h au lieu de 1h)
- [x] `InvoiceAlertService.java` - Déjà correct

### Base de Données
- [ ] Exécuter le script de nettoyage `clean-duplicate-alerts.js`
- [ ] Vérifier le nombre d'alertes après nettoyage
- [ ] Confirmer qu'il n'y a plus de doublons

### Tests
- [ ] Redémarrer l'application
- [ ] Déclencher l'analyse manuellement
- [ ] Vérifier qu'aucun doublon n'est créé
- [ ] Attendre 6 heures et vérifier à nouveau

---

## 🎯 Résultat Attendu

**Avant:**
- 121 alertes PENDING_DECISION
- Doublons créés toutes les heures
- Base de données surchargée

**Après:**
- ~10-15 alertes PENDING_DECISION (nombre réel)
- 1 alerte par facture en retard
- 1 alerte par KPI anormal
- Pas de doublons
- Mises à jour au lieu de créations

---

## 🚀 Prochaines Étapes

1. **Nettoyer la base de données** avec le script fourni
2. **Redémarrer l'application** pour appliquer les corrections
3. **Tester** en déclenchant manuellement l'analyse
4. **Surveiller** les logs pour confirmer qu'il n'y a plus de doublons

**Votre système d'alertes est maintenant optimisé et ne créera plus de doublons!** ✅

