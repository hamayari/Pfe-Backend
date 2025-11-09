# Script pour nettoyer les anciennes notifications
$mongoUri = "mongodb://localhost:27017"
$database = "gestion_conventions"

Write-Host "🧹 Nettoyage des anciennes notifications..." -ForegroundColor Yellow

# Supprimer toutes les notifications sauf les 10 dernières
mongosh $mongoUri/$database --eval "
  var count = db.notifications.countDocuments();
  print('📊 Total notifications: ' + count);
  
  if (count > 10) {
    var toDelete = count - 10;
    var oldestNotifs = db.notifications.find().sort({timestamp: 1}).limit(toDelete).toArray();
    var idsToDelete = oldestNotifs.map(n => n._id);
    var result = db.notifications.deleteMany({_id: {\$in: idsToDelete}});
    print('🗑️ ' + result.deletedCount + ' notifications supprimées');
  } else {
    print('✅ Pas de nettoyage nécessaire');
  }
  
  var remaining = db.notifications.countDocuments();
  print('📊 Notifications restantes: ' + remaining);
"

Write-Host "✅ Nettoyage terminé!" -ForegroundColor Green
