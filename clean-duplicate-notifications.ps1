# Script PowerShell pour nettoyer les notifications en double dans MongoDB

Write-Host "🧹 Nettoyage des notifications en double..." -ForegroundColor Cyan

# Connexion à MongoDB
$mongoUri = "mongodb://localhost:27017"
$database = "commercial_pfe"
$collection = "notifications"

Write-Host "📊 Analyse des notifications..." -ForegroundColor Yellow

# Commande MongoDB pour compter les notifications
$countCommand = @"
db.notifications.countDocuments()
"@

# Exécuter la commande
$totalCount = mongosh $mongoUri/$database --quiet --eval $countCommand

Write-Host "📈 Total de notifications: $totalCount" -ForegroundColor White

# Supprimer les notifications en double (garder la plus récente de chaque type par utilisateur)
$cleanupCommand = @"
// Trouver les doublons
var duplicates = db.notifications.aggregate([
  {
    `$group: {
      _id: {
        userId: '`$userId',
        type: '`$type',
        title: '`$title',
        message: '`$message'
      },
      ids: { `$push: '`$_id' },
      count: { `$sum: 1 }
    }
  },
  {
    `$match: {
      count: { `$gt: 1 }
    }
  }
]).toArray();

print('🔍 Groupes de doublons trouvés: ' + duplicates.length);

var deletedCount = 0;

// Pour chaque groupe de doublons, garder le plus récent
duplicates.forEach(function(doc) {
  // Récupérer toutes les notifications du groupe
  var notifs = db.notifications.find({
    _id: { `$in: doc.ids }
  }).sort({ timestamp: -1 }).toArray();
  
  // Garder la première (plus récente), supprimer les autres
  for (var i = 1; i < notifs.length; i++) {
    db.notifications.deleteOne({ _id: notifs[i]._id });
    deletedCount++;
  }
});

print('🗑️  Notifications supprimées: ' + deletedCount);

// Compter le nouveau total
var newCount = db.notifications.countDocuments();
print('✅ Nouveau total: ' + newCount);
"@

Write-Host "🔄 Suppression des doublons..." -ForegroundColor Yellow
mongosh $mongoUri/$database --quiet --eval $cleanupCommand

Write-Host ""
Write-Host "✅ Nettoyage terminé!" -ForegroundColor Green
Write-Host ""
Write-Host "💡 Conseil: Redémarrez l'application pour voir les changements" -ForegroundColor Cyan
