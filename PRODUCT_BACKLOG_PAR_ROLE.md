# 📋 PRODUCT BACKLOG PAR RÔLE

## 📊 ANALYSE DES RÔLES DU SYSTÈME

### Rôles Identifiés
1. **ADMIN** (Administrateur Système)
2. **COMMERCIAL** (Agent Commercial)
3. **DECIDEUR** (Décideur / Decision Maker)
4. **CHEF_PROJET** (Chef de Projet / Project Manager)

---

## 🔴 RÔLE: ADMINISTRATEUR (ADMIN)

### Description
L'administrateur gère l'ensemble du système, les utilisateurs, les configurations et supervise toutes les opérations.

### Fonctionnalités Actuelles
- ✅ Gestion des utilisateurs (CRUD)
- ✅ Gestion des rôles et permissions
- ✅ Gestion des nomenclatures
- ✅ Gestion des structures
- ✅ Monitoring système
- ✅ Logs d'audit
- ✅ Configuration des alertes
- ✅ Dashboard administratif
- ✅ Gestion des gouvernorats
- ✅ Gestion des zones géographiques

### 📋 PRODUCT BACKLOG - ADMIN

| ID | User Story | Priorité | Statut | Sprint |
|----|-----------|----------|--------|--------|
| ADM-001 | En tant qu'admin, je veux créer des utilisateurs avec différents rôles | ✅ FAIT | Terminé | - |
| ADM-002 | En tant qu'admin, je veux modifier les informations d'un utilisateur | ✅ FAIT | Terminé | - |
| ADM-003 | En tant qu'admin, je veux désactiver/activer un compte utilisateur | ✅ FAIT | Terminé | - |
| ADM-004 | En tant qu'admin, je veux voir tous les logs d'audit du système | ✅ FAIT | Terminé | - |
| ADM-005 | En tant qu'admin, je veux configurer les seuils d'alertes KPI | ✅ FAIT | Terminé | - |
| ADM-006 | En tant qu'admin, je veux gérer les nomenclatures (applications, structures) | ✅ FAIT | Terminé | - |
| ADM-007 | En tant qu'admin, je veux voir les statistiques globales du système | ✅ FAIT | Terminé | - |
| ADM-008 | En tant qu'admin, je veux exporter les données en Excel/PDF | 🟡 EN COURS | En développement | Sprint 3 |
| ADM-009 | En tant qu'admin, je veux configurer les templates d'emails | 🟡 EN COURS | En développement | Sprint 3 |
| ADM-010 | En tant qu'admin, je veux gérer les permissions granulaires par rôle | 🔵 À FAIRE | Backlog | Sprint 4 |
| ADM-011 | En tant qu'admin, je veux planifier des sauvegardes automatiques | 🔵 À FAIRE | Backlog | Sprint 5 |
| ADM-012 | En tant qu'admin, je veux configurer l'authentification 2FA | 🔵 À FAIRE | Backlog | Sprint 5 |
| ADM-013 | En tant qu'admin, je veux voir un dashboard de monitoring en temps réel | 🔵 À FAIRE | Backlog | Sprint 4 |
| ADM-014 | En tant qu'admin, je veux gérer les webhooks externes (N8N, Slack) | 🔵 À FAIRE | Backlog | Sprint 6 |
| ADM-015 | En tant qu'admin, je veux configurer des règles de validation personnalisées | 🔵 À FAIRE | Backlog | Sprint 6 |

---

## 🟢 RÔLE: COMMERCIAL

### Description
Le commercial gère les conventions, les factures, les clients et suit les paiements.

### Fonctionnalités Actuelles
- ✅ Gestion des conventions (CRUD)
- ✅ Génération de factures
- ✅ Suivi des paiements
- ✅ Upload de preuves de paiement
- ✅ Dashboard commercial
- ✅ Statistiques personnelles
- ✅ Messagerie interne
- ✅ Notifications de rappel
- ✅ Calendrier des échéances

### 📋 PRODUCT BACKLOG - COMMERCIAL

| ID | User Story | Priorité | Statut | Sprint |
|----|-----------|----------|--------|--------|
| COM-001 | En tant que commercial, je veux créer une nouvelle convention | ✅ FAIT | Terminé | - |
| COM-002 | En tant que commercial, je veux générer des factures pour mes conventions | ✅ FAIT | Terminé | - |
| COM-003 | En tant que commercial, je veux uploader une preuve de paiement | ✅ FAIT | Terminé | - |
| COM-004 | En tant que commercial, je veux voir mes statistiques de performance | ✅ FAIT | Terminé | - |
| COM-005 | En tant que commercial, je veux recevoir des alertes pour les factures en retard | ✅ FAIT | Terminé | - |
| COM-006 | En tant que commercial, je veux voir un calendrier de mes échéances | ✅ FAIT | Terminé | - |
| COM-007 | En tant que commercial, je veux envoyer des messages au chef de projet | ✅ FAIT | Terminé | - |
| COM-008 | En tant que commercial, je veux filtrer mes conventions par statut/date | ✅ FAIT | Terminé | - |
| COM-009 | En tant que commercial, je veux exporter mes rapports mensuels | 🟡 EN COURS | En développement | Sprint 3 |
| COM-010 | En tant que commercial, je veux voir l'historique complet d'une convention | 🟡 EN COURS | En développement | Sprint 3 |
| COM-011 | En tant que commercial, je veux recevoir des notifications SMS pour les urgences | 🔵 À FAIRE | Backlog | Sprint 4 |
| COM-012 | En tant que commercial, je veux utiliser un chatbot pour des questions rapides | 🔵 À FAIRE | Backlog | Sprint 4 |
| COM-013 | En tant que commercial, je veux voir des suggestions de relance automatiques | 🔵 À FAIRE | Backlog | Sprint 5 |
| COM-014 | En tant que commercial, je veux dupliquer une convention existante | 🔵 À FAIRE | Backlog | Sprint 5 |
| COM-015 | En tant que commercial, je veux voir une carte géographique de mes clients | 🔵 À FAIRE | Backlog | Sprint 6 |
| COM-016 | En tant que commercial, je veux planifier des rappels personnalisés | 🔵 À FAIRE | Backlog | Sprint 6 |
| COM-017 | En tant que commercial, je veux voir des prévisions de revenus basées sur l'IA | 🔵 À FAIRE | Backlog | Sprint 7 |
| COM-018 | En tant que commercial, je veux intégrer mon calendrier Google/Outlook | 🔵 À FAIRE | Backlog | Sprint 7 |

---

## 🔵 RÔLE: DÉCIDEUR (DECISION MAKER)

### Description
Le décideur analyse les performances globales, prend des décisions stratégiques et délègue les alertes critiques.

### Fonctionnalités Actuelles
- ✅ Dashboard décisionnel avec KPI
- ✅ Graphiques d'analyse (évolution, tendances)
- ✅ Analyse comparative par période
- ✅ Filtres avancés (gouvernorat, structure, période)
- ✅ Réception d'alertes KPI automatiques
- ✅ Délégation d'alertes au chef de projet
- ✅ Export de rapports (PDF, Excel)
- ✅ Vue détaillée des données tabulaires

### 📋 PRODUCT BACKLOG - DÉCIDEUR

| ID | User Story | Priorité | Statut | Sprint |
|----|-----------|----------|--------|--------|
| DEC-001 | En tant que décideur, je veux voir les KPI globaux en temps réel | ✅ FAIT | Terminé | - |
| DEC-002 | En tant que décideur, je veux comparer les performances par période | ✅ FAIT | Terminé | - |
| DEC-003 | En tant que décideur, je veux filtrer les analyses par gouvernorat | ✅ FAIT | Terminé | - |
| DEC-004 | En tant que décideur, je veux recevoir des alertes KPI automatiques | ✅ FAIT | Terminé | - |
| DEC-005 | En tant que décideur, je veux déléguer une alerte au chef de projet | ✅ FAIT | Terminé | - |
| DEC-006 | En tant que décideur, je veux exporter un rapport mensuel en PDF | 🟡 EN COURS | En développement | Sprint 3 |
| DEC-007 | En tant que décideur, je veux voir l'historique de mes délégations | 🟡 EN COURS | En développement | Sprint 3 |
| DEC-008 | En tant que décideur, je veux créer des rapports personnalisés | 🟡 EN COURS | En développement | Sprint 3 |
| DEC-009 | En tant que décideur, je veux voir une heatmap régionale des performances | 🔵 À FAIRE | Backlog | Sprint 4 |
| DEC-010 | En tant que décideur, je veux configurer mes propres seuils d'alerte | 🔵 À FAIRE | Backlog | Sprint 4 |
| DEC-011 | En tant que décideur, je veux recevoir un digest hebdomadaire par email | 🔵 À FAIRE | Backlog | Sprint 5 |
| DEC-012 | En tant que décideur, je veux voir des prédictions basées sur l'IA | 🔵 À FAIRE | Backlog | Sprint 5 |
| DEC-013 | En tant que décideur, je veux comparer avec les benchmarks du secteur | 🔵 À FAIRE | Backlog | Sprint 6 |
| DEC-014 | En tant que décideur, je veux créer des tableaux de bord personnalisés | 🔵 À FAIRE | Backlog | Sprint 6 |
| DEC-015 | En tant que décideur, je veux voir l'impact financier des décisions | 🔵 À FAIRE | Backlog | Sprint 7 |

---

## 🟡 RÔLE: CHEF DE PROJET (PROJECT MANAGER)

### Description
Le chef de projet coordonne l'équipe commerciale, gère les tâches, suit les alertes déléguées et assure le suivi opérationnel.

### Fonctionnalités Actuelles
- ✅ Dashboard de gestion de projet
- ✅ Vue d'ensemble des conventions et factures
- ✅ Suivi de l'équipe commerciale
- ✅ Graphiques d'évolution et performance
- ✅ Diagramme de Gantt pour les tâches
- ✅ Timeline des processus
- ✅ Réception d'alertes déléguées
- ✅ Messagerie avec l'équipe
- ✅ Commentaires internes
- ✅ Monitoring en temps réel

### 📋 PRODUCT BACKLOG - CHEF DE PROJET

| ID | User Story | Priorité | Statut | Sprint |
|----|-----------|----------|--------|--------|
| CPR-001 | En tant que chef de projet, je veux voir les performances de mon équipe | ✅ FAIT | Terminé | - |
| CPR-002 | En tant que chef de projet, je veux recevoir les alertes déléguées par le décideur | 🟡 EN COURS | En développement | Sprint 3 |
| CPR-003 | En tant que chef de projet, je veux assigner des tâches aux commerciaux | ✅ FAIT | Terminé | - |
| CPR-004 | En tant que chef de projet, je veux voir un diagramme de Gantt des projets | ✅ FAIT | Terminé | - |
| CPR-005 | En tant que chef de projet, je veux suivre l'avancement des tâches | ✅ FAIT | Terminé | - |
| CPR-006 | En tant que chef de projet, je veux laisser des commentaires internes | ✅ FAIT | Terminé | - |
| CPR-007 | En tant que chef de projet, je veux voir les conventions à risque | ✅ FAIT | Terminé | - |
| CPR-008 | En tant que chef de projet, je veux envoyer des rappels à l'équipe | 🟡 EN COURS | En développement | Sprint 3 |
| CPR-009 | En tant que chef de projet, je veux voir les statistiques par commercial | ✅ FAIT | Terminé | - |
| CPR-010 | En tant que chef de projet, je veux exporter un rapport d'équipe | 🔵 À FAIRE | Backlog | Sprint 4 |
| CPR-011 | En tant que chef de projet, je veux créer des sprints de travail | 🔵 À FAIRE | Backlog | Sprint 4 |
| CPR-012 | En tant que chef de projet, je veux voir un burndown chart | 🔵 À FAIRE | Backlog | Sprint 4 |
| CPR-013 | En tant que chef de projet, je veux planifier des réunions d'équipe | 🔵 À FAIRE | Backlog | Sprint 5 |
| CPR-014 | En tant que chef de projet, je veux voir la charge de travail par commercial | 🔵 À FAIRE | Backlog | Sprint 5 |
| CPR-015 | En tant que chef de projet, je veux créer des templates de tâches | 🔵 À FAIRE | Backlog | Sprint 6 |
| CPR-016 | En tant que chef de projet, je veux voir des alertes de surcharge de travail | 🔵 À FAIRE | Backlog | Sprint 6 |
| CPR-017 | En tant que chef de projet, je veux intégrer avec Jira/Trello | 🔵 À FAIRE | Backlog | Sprint 7 |
| CPR-018 | En tant que chef de projet, je veux voir des métriques de vélocité d'équipe | 🔵 À FAIRE | Backlog | Sprint 7 |

---

## 📊 RÉSUMÉ DES PRIORITÉS

### Sprint 3 (En cours)
- 🟡 Export de rapports (Admin, Commercial, Décideur)
- 🟡 Historique des délégations (Décideur)
- 🟡 Notifications déléguées (Chef de Projet)
- 🟡 Templates d'emails (Admin)

### Sprint 4 (Prochain)
- 🔵 Permissions granulaires (Admin)
- 🔵 Notifications SMS (Commercial)
- 🔵 Chatbot (Commercial)
- 🔵 Heatmap régionale (Décideur)
- 🔵 Gestion des sprints (Chef de Projet)

### Sprint 5 (Futur proche)
- 🔵 Authentification 2FA (Admin)
- 🔵 Suggestions de relance IA (Commercial)
- 🔵 Digest hebdomadaire (Décideur)
- 🔵 Planification réunions (Chef de Projet)

### Sprint 6+ (Futur)
- 🔵 Webhooks externes (Admin)
- 🔵 Carte géographique clients (Commercial)
- 🔵 Tableaux de bord personnalisés (Décideur)
- 🔵 Intégration Jira/Trello (Chef de Projet)

---

## 🎯 MÉTRIQUES DE SUCCÈS

### Admin
- Temps de création d'un utilisateur < 2 minutes
- 100% des logs d'audit capturés
- Disponibilité système > 99.5%

### Commercial
- Temps de création convention < 5 minutes
- Taux de paiement à temps > 80%
- Satisfaction utilisateur > 4/5

### Décideur
- Temps de génération rapport < 30 secondes
- Précision des KPI = 100%
- Temps de prise de décision réduit de 40%

### Chef de Projet
- Taux de complétion des tâches > 85%
- Temps de réponse aux alertes < 2 heures
- Productivité équipe +25%

---

## 📝 NOTES TECHNIQUES

### Technologies Utilisées
- **Backend:** Java Spring Boot, MongoDB
- **Frontend:** Angular 17, Material Design
- **Temps Réel:** WebSocket (STOMP)
- **Notifications:** Email (Brevo), SMS (Twilio)
- **Monitoring:** Custom monitoring service
- **Export:** jsPDF, XLSX
- **Charts:** Chart.js, ApexCharts

### Architecture
- Microservices REST API
- Event-driven architecture
- Real-time notifications
- Role-based access control (RBAC)
- Audit logging
- Automated schedulers

---

**Document créé le:** 30 Octobre 2025  
**Version:** 1.0  
**Auteur:** Équipe Développement
