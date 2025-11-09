# 🔗 Intégration Backend-Frontend - Rapports et Historique

## ✅ Backend Implémenté

### 1. **API Rapports** (`/api/reports`)

#### Endpoints créés:

```
GET /api/reports/financial          → Rapport financier global
GET /api/reports/performance        → Rapport de performance
GET /api/reports/by-governorate     → Rapport par gouvernorat
GET /api/reports/by-month           → Rapport par période
```

#### DTOs créés:
- `FinancialReportDTO` - Rapport financier
- `PerformanceReportDTO` - Rapport de performance
- `GovernorateReportDTO` - Rapport par gouvernorat
- `MonthlyReportDTO` - Rapport mensuel

#### Service:
- `ReportService` - Calculs avec vraies données depuis MongoDB

---

### 2. **API Historique/Audit** (`/api/audit`)

#### Endpoints disponibles:

```
GET  /api/audit                      → Tous les logs (avec filtres et pagination)
GET  /api/audit/user/{username}      → Logs d'un utilisateur spécifique
POST /api/audit                      → Créer une entrée d'audit
```

#### Paramètres de filtrage:
- `entityType` - Type d'entité (CONVENTION, FACTURE)
- `entityId` - ID de l'entité
- `action` - Type d'action
- `username` - Utilisateur
- `startDate` - Date de début
- `endDate` - Date de fin
- `page` - Numéro de page (défaut: 0)
- `size` - Taille de page (défaut: 5)

---

## 🔧 Intégration Frontend

### **Étape 1: Créer un service Angular pour les Rapports**

Créez `src/app/services/report.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  getFinancialReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/financial`);
  }

  getPerformanceReport(): Observable<any> {
    return this.http.get(`${this.apiUrl}/performance`);
  }

  getReportByGovernorate(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/by-governorate`);
  }

  getReportByMonth(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/by-month`);
  }
}
```

---

### **Étape 2: Créer un service Angular pour l'Audit**

Créez `src/app/services/audit.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private apiUrl = 'http://localhost:8080/api/audit';

  constructor(private http: HttpClient) {}

  getAuditLogs(username?: string, page: number = 0, size: number = 5): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (username) {
      params = params.set('username', username);
    }

    return this.http.get(`${this.apiUrl}`, { params });
  }

  getUserAuditLogs(username: string, page: number = 0, size: number = 5): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get(`${this.apiUrl}/user/${username}`, { params });
  }

  createAuditLog(auditLog: any): Observable<any> {
    return this.http.post(this.apiUrl, auditLog);
  }
}
```

---

### **Étape 3: Modifier le Component pour utiliser les services**

Dans `commercial-dashboard.component.ts`:

```typescript
import { ReportService } from '../../services/report.service';
import { AuditService } from '../../services/audit.service';

export class CommercialDashboardComponent implements OnInit {
  
  constructor(
    private reportService: ReportService,
    private auditService: AuditService,
    // ... autres services
  ) {}

  // Remplacer getFinancialReport() par:
  getFinancialReport(): any {
    this.reportService.getFinancialReport().subscribe(
      (data) => {
        this.financialReport = data;
      },
      (error) => console.error('Erreur chargement rapport financier:', error)
    );
    return this.financialReport || {};
  }

  // Remplacer getFullHistory() par:
  loadUserAuditHistory(): void {
    const username = this.currentUser.username || 'commercial';
    this.auditService.getUserAuditLogs(username, this.historyPageIndex, this.historyPageSize)
      .subscribe(
        (response) => {
          this.auditLog = response.content; // Spring Page retourne 'content'
          this.totalAuditEntries = response.totalElements;
        },
        (error) => console.error('Erreur chargement historique:', error)
      );
  }

  // Appeler dans ngOnInit():
  ngOnInit(): void {
    this.loadDashboardData();
    this.generateCalendar();
    this.loadUserAuditHistory(); // Charger l'historique depuis l'API
  }

  // Méthode pour enregistrer une action dans l'audit
  logAuditToBackend(action: string, entityType: string, entityId: string, description: string): void {
    const auditEntry = {
      action: action,
      entityType: entityType,
      entityId: entityId,
      username: this.currentUser.username,
      timestamp: new Date().toISOString(),
      details: description
    };

    this.auditService.createAuditLog(auditEntry).subscribe(
      () => console.log('✅ Audit enregistré dans la DB'),
      (error) => console.error('❌ Erreur enregistrement audit:', error)
    );
  }
}
```

---

## 📊 Exemple d'utilisation complète

### **Dans le HTML (rapports):**

```html
<!-- Le HTML reste identique, mais les données viennent maintenant du backend -->
<div class="stat-value primary">
  {{ financialReport?.totalRevenue | currency:'EUR' }}
</div>
```

### **Dans le TypeScript:**

```typescript
// Charger tous les rapports au démarrage
ngOnInit(): void {
  this.loadAllReports();
}

loadAllReports(): void {
  // Rapport financier
  this.reportService.getFinancialReport().subscribe(data => {
    this.financialReport = data;
  });

  // Rapport de performance
  this.reportService.getPerformanceReport().subscribe(data => {
    this.performanceReport = data;
  });

  // Rapport par gouvernorat
  this.reportService.getReportByGovernorate().subscribe(data => {
    this.governorateReport = data;
  });

  // Rapport mensuel
  this.reportService.getReportByMonth().subscribe(data => {
    this.monthlyReport = data;
  });
}
```

---

## 🔒 Sécurité et Bonnes Pratiques

### 1. **Authentification**
- Ajouter JWT token dans les headers HTTP
- Filtrer les données par utilisateur côté backend

### 2. **Validation**
- Valider toutes les entrées côté backend
- Utiliser `@Valid` sur les DTOs

### 3. **Pagination**
- Toujours paginer les résultats (déjà implémenté)
- Limiter la taille maximale des pages

### 4. **Cache**
- Mettre en cache les rapports (ex: 5 minutes)
- Utiliser `@Cacheable` de Spring

### 5. **Logs**
- Logger toutes les actions importantes
- Utiliser SLF4J pour les logs

---

## 📝 Checklist d'intégration

- [x] Backend: Entités créées
- [x] Backend: Repositories créés
- [x] Backend: Services implémentés avec vraies données
- [x] Backend: Controllers REST créés
- [x] Backend: Pagination implémentée
- [ ] Frontend: Services Angular créés
- [ ] Frontend: Injection des services dans components
- [ ] Frontend: Appels HTTP configurés
- [ ] Frontend: Gestion des erreurs
- [ ] Tests: Tests unitaires backend
- [ ] Tests: Tests d'intégration
- [ ] Documentation: API documentée (Swagger)

---

## 🚀 Prochaines étapes

1. **Créer les services Angular** (`report.service.ts` et `audit.service.ts`)
2. **Injecter les services** dans `commercial-dashboard.component.ts`
3. **Remplacer les méthodes locales** par des appels HTTP
4. **Tester l'intégration** avec le backend
5. **Ajouter la gestion d'erreurs** et les loaders
6. **Optimiser les performances** (cache, lazy loading)

---

## 📞 Endpoints Résumé

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/reports/financial` | GET | Rapport financier |
| `/api/reports/performance` | GET | Rapport de performance |
| `/api/reports/by-governorate` | GET | Rapport par gouvernorat |
| `/api/reports/by-month` | GET | Rapport mensuel |
| `/api/audit` | GET | Historique avec filtres |
| `/api/audit/user/{username}` | GET | Historique utilisateur |
| `/api/audit` | POST | Créer entrée d'audit |

---

**✅ Backend est maintenant prêt et suit les bonnes pratiques!**
**🔧 Il reste à connecter le frontend aux APIs.**
