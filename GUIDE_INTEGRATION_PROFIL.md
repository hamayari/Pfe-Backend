# Guide d'Intégration de la Section Profil dans tous les Dashboards

## ✅ Composants Créés et Fonctionnels

### Backend
- ✅ `ChangePasswordRequest.java` - DTO pour le changement de mot de passe
- ✅ `UserProfileController.java` - Endpoint `/api/user-profile/change-password`
- ✅ Validation complète (ancien mot de passe, nouveau, confirmation)

### Frontend
- ✅ `ChangePasswordModalComponent` - Modal professionnel avec 3 champs
- ✅ `ProfileSectionComponent` - Section profil réutilisable
- ✅ Intégré dans `/profile` (page standalone)
- ✅ Intégré dans `commercial-dashboard`

## 📋 Pour intégrer dans les autres dashboards

### Étape 1: Imports dans le fichier `.ts`

Ajoutez ces imports en haut du fichier:

```typescript
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ChangePasswordModalComponent } from '../../shared/components/change-password-modal/change-password-modal.component';
```

Dans les `imports` du `@Component`:
```typescript
imports: [
  // ... autres imports
  MatDialogModule,
  MatSnackBarModule
],
```

Dans le `constructor`:
```typescript
constructor(
  // ... autres services
  private dialog: MatDialog,
  private snackBar: MatSnackBar
) {}
```

### Étape 2: Ajouter la méthode

```typescript
openChangePasswordModal(): void {
  console.log('🔐 Ouverture du modal de changement de mot de passe...');
  
  const dialogRef = this.dialog.open(ChangePasswordModalComponent, {
    width: '500px',
    maxWidth: '90vw',
    disableClose: false,
    panelClass: 'change-password-dialog'
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result?.success) {
      this.snackBar.open('✅ Mot de passe changé avec succès', 'Fermer', { 
        duration: 5000
      });
    }
  });
}
```

### Étape 3: Ajouter dans le HTML

Dans la sidebar, ajoutez (si pas déjà présent):
```html
<div class="menu-section">
  <div class="section-title">PROFIL</div>
  <div class="menu-item" [class.active]="activeSection === 'profile'" (click)="setActiveSection('profile')">
    <mat-icon>person</mat-icon>
    <span>Mon Profil</span>
  </div>
</div>
```

Dans le contenu principal:
```html
<!-- Section Profil -->
<div *ngIf="activeSection === 'profile'" class="profile-section-wrapper" style="padding: 20px;">
  <mat-card style="max-width: 800px; margin: 0 auto;">
    <mat-card-header>
      <mat-card-title style="display: flex; align-items: center; gap: 12px;">
        <mat-icon style="color: #e74c3c;">lock</mat-icon>
        Sécurité du compte
      </mat-card-title>
    </mat-card-header>
    <mat-card-content style="padding: 24px;">
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <button 
          mat-raised-button 
          color="primary" 
          (click)="openChangePasswordModal()"
          style="width: 100%; height: 60px; font-size: 16px;">
          <mat-icon>vpn_key</mat-icon>
          <span>Changer le mot de passe</span>
        </button>
        
        <button 
          mat-stroked-button 
          color="accent"
          style="width: 100%; height: 60px; font-size: 16px;">
          <mat-icon>security</mat-icon>
          <span>Authentification à deux facteurs</span>
        </button>
      </div>
    </mat-card-content>
  </mat-card>
</div>
```

## 🎯 Dashboards à mettre à jour

- ✅ Commercial Dashboard (FAIT)
- ⏳ Decideur Dashboard
- ⏳ Admin Dashboard  
- ⏳ Project Manager Dashboard

## 🔐 Fonctionnalités du Modal

- Validation de l'ancien mot de passe
- Nouveau mot de passe (min 6 caractères)
- Confirmation du nouveau mot de passe
- Icônes pour afficher/masquer les mots de passe
- Design professionnel avec dégradé violet
- Messages d'erreur clairs
- Encodage sécurisé BCrypt côté serveur
