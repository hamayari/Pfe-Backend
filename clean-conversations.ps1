# Script pour nettoyer les conversations dupliquées
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🧹 NETTOYAGE DES CONVERSATIONS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8085"

Write-Host "`n🗑️  Suppression de toutes les conversations..." -ForegroundColor Yellow

# Note: Cet endpoint doit être créé dans le backend
# Pour l'instant, utilise MongoDB Compass ou mongo shell

Write-Host "`n📝 INSTRUCTIONS MANUELLES:" -ForegroundColor Yellow
Write-Host "1. Ouvre MongoDB Compass" -ForegroundColor White
Write-Host "2. Connecte-toi à: mongodb://localhost:27017" -ForegroundColor White
Write-Host "3. Sélectionne la base de données 'commercial_pfe'" -ForegroundColor White
Write-Host "4. Ouvre la collection 'conversations'" -ForegroundColor White
Write-Host "5. Clique sur 'DELETE' et supprime TOUTES les conversations" -ForegroundColor White
Write-Host "6. Rafraîchis la page de messagerie (F5)" -ForegroundColor White

Write-Host "`n========================================" -ForegroundColor Cyan
