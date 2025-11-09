# Diagrammes de Cas d'Utilisation - Système de Gestion des Conventions et Facturations

## 📋 Description

Ce dossier contient les diagrammes de cas d'utilisation raffinés pour le projet de gestion des conventions et des échéances de facturations. Les diagrammes sont créés en PlantUML et couvrent l'ensemble des fonctionnalités du système pour les 4 types d'acteurs.

## 🎯 Acteurs du Système

1. **Administrateur** - Gestion des utilisateurs et des nomenclatures
2. **Commercial Métier** - Gestion des conventions et des factures
3. **Chef de Projet** - Supervision et interaction avec les commerciaux
4. **Décideur** - Analyse des données via tableau de bord

## 📁 Structure des Diagrammes

### 1. Diagramme Global (`diagramme-global.puml`)
Vue d'ensemble complète du système montrant tous les acteurs et leurs cas d'utilisation principaux.

**Packages inclus:**
- Authentification
- Gestion des Utilisateurs
- Gestion des Nomenclatures
- Gestion des Conventions
- Gestion des Factures
- Gestion des Notifications
- Tableau de Bord
- Supervision

### 2. Sprint 3 - Administrateur (`sprint3-administrateur.puml`)
Diagramme raffiné pour le Sprint 3 (Semaines 5-6)

**Fonctionnalités:**
- ✅ Authentification et gestion du profil
- ✅ CRUD des comptes utilisateurs
- ✅ Attribution des rôles
- ✅ Gestion des nomenclatures:
  - Applications
  - Zones géographiques
  - Structures

### 3. Sprint 4 - Commercial Métier (`sprint4-commercial.puml`)
Diagramme raffiné pour le Sprint 4 (Semaines 7-8)

**Fonctionnalités:**
- ✅ CRUD des conventions avec tous les détails:
  - Référence, libellé, dates
  - Structure, gouvernorat
  - Modalités de paiement
- ✅ Génération automatique des factures
- ✅ Mise à jour du statut des factures
- ✅ Visualisation des échéances avec coloration
- ✅ Gestion des preuves de paiement

### 4. Sprint 5 - Notifications (`sprint5-notifications.puml`)
Diagramme raffiné pour le Sprint 5 (Semaines 9-10)

**Fonctionnalités:**
- ✅ Paramétrage des notifications (Administrateur)
- ✅ Préférences utilisateur (E-mail/SMS)
- ✅ Génération automatique des notifications
- ✅ Envoi personnalisé par canal
- ✅ Journalisation et rapports

**Acteurs impliqués:**
- Administrateur (paramétrage)
- Commercial Métier (réception)
- Chef de Projet (réception)
- Système (génération automatique)

### 5. Sprint 6 - Décideur (`sprint6-decideur.puml`)
Diagramme raffiné pour le Sprint 6 (Semaines 11-12)

**Fonctionnalités:**
- ✅ Tableau de bord interactif
- ✅ Filtres multiples:
  - Par gouvernorat
  - Par structure
  - Par application
  - Par période
  - Par statut
- ✅ Visualisations:
  - KPI (indicateurs clés)
  - Graphiques temporels
  - Répartitions géographiques
- ✅ Analyse croisée et tendances
- ✅ Export (PDF, Excel)
- ✅ Personnalisation du tableau de bord

### 6. Chef de Projet - Supervision (`chef-projet-supervision.puml`)
Diagramme dédié aux fonctionnalités de supervision

**Fonctionnalités:**
- ✅ Supervision des conventions
- ✅ Supervision des facturations
- ✅ Interaction avec les commerciaux:
  - Envoi de messages
  - Signalement de problèmes
  - Suivi des résolutions
- ✅ Notifications et alertes
- ✅ Rapports de suivi
- ✅ Tableau de bord de supervision

## 🔗 Relations entre Cas d'Utilisation

Les diagrammes utilisent les relations UML standard:

- **`<<include>>`** : Relation obligatoire (le cas inclus est toujours exécuté)
- **`<<extend>>`** : Relation optionnelle (le cas étendu peut être exécuté)
- **`<<communicate>>`** : Communication entre acteurs

## 🛠️ Utilisation des Diagrammes

### Visualisation avec PlantUML

1. **En ligne:**
   - Visitez [PlantUML Online Editor](http://www.plantuml.com/plantuml/uml/)
   - Copiez le contenu d'un fichier `.puml`
   - Visualisez le diagramme généré

2. **VS Code:**
   - Installez l'extension "PlantUML"
   - Ouvrez un fichier `.puml`
   - Utilisez `Alt+D` pour prévisualiser

3. **IntelliJ IDEA:**
   - Installez le plugin "PlantUML integration"
   - Ouvrez un fichier `.puml`
   - Le diagramme s'affiche automatiquement

### Export des Diagrammes

Les diagrammes peuvent être exportés en:
- PNG (images)
- SVG (vectoriel)
- PDF (documentation)

## 📊 Correspondance avec les Sprints

| Sprint | Semaines | Diagramme | Acteur Principal |
|--------|----------|-----------|------------------|
| Sprint 3 | 5-6 | `sprint3-administrateur.puml` | Administrateur |
| Sprint 4 | 7-8 | `sprint4-commercial.puml` | Commercial Métier |
| Sprint 5 | 9-10 | `sprint5-notifications.puml` | Tous + Système |
| Sprint 6 | 11-12 | `sprint6-decideur.puml` | Décideur |

## 🎨 Conventions de Nommage

- **UC_** : Préfixe pour Use Case (cas d'utilisation)
- **Verbes à l'infinitif** : Pour les actions (Créer, Modifier, Consulter...)
- **Packages thématiques** : Regroupement logique des fonctionnalités

## 📝 Notes Importantes

1. **Authentification** : Tous les acteurs doivent s'authentifier avant d'accéder aux fonctionnalités
2. **Gestion du profil** : Disponible pour tous les acteurs
3. **Notifications** : Système automatique avec préférences personnalisables
4. **Coloration des échéances** : Visuelle pour identifier rapidement les retards
5. **Interactions** : Le Chef de Projet peut interagir avec les Commerciaux

## 🔄 Évolution des Diagrammes

Ces diagrammes sont des documents vivants qui peuvent évoluer pendant le développement:
- Ajout de nouveaux cas d'utilisation
- Raffinement des relations
- Ajout de détails suite aux retours utilisateurs

## 📚 Documentation Complémentaire

Pour plus de détails, consultez:
- Cahier des charges du projet
- Spécifications fonctionnelles détaillées
- Documentation technique de l'architecture

---

**Date de création:** Novembre 2024  
**Version:** 1.0  
**Méthodologie:** Scrum (Sprints de 2 semaines)
