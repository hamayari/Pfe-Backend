# Script pour nettoyer les alertes KPI en double
$mongoUri = "mongodb://localhost:27017"
$database = "gestion_conventions"

Write-Host "Nettoyage des alertes KPI en double..." -ForegroundColor Yellow

mongosh $mongoUri/$database --eval "
  print('Analyse des doublons...');
  
  var duplicates = db.kpi_alerts.aggregate([
    {
      \$match: {
        relatedInvoiceId: { \$exists: true, \$ne: null }
      }
    },
    {
      \$group: {
        _id: '\$relatedInvoiceId',
        count: { \$sum: 1 },
        ids: { \$push: '\$_id' }
      }
    },
    {
      \$match: {
        count: { \$gt: 1 }
      }
    }
  ]).toArray();
  
  print('Factures avec doublons: ' + duplicates.length);
  
  var totalDeleted = 0;
  
  duplicates.forEach(function(dup) {
    print('Facture ' + dup._id + ': ' + dup.count + ' alertes');
    
    var alerts = db.kpi_alerts.find({
      relatedInvoiceId: dup._id
    }).sort({ detectedAt: -1 }).toArray();
    
    var toKeep = alerts[0]._id;
    print('  Garder: ' + toKeep);
    
    for (var i = 1; i < alerts.length; i++) {
      print('  Supprimer: ' + alerts[i]._id);
      db.kpi_alerts.deleteOne({ _id: alerts[i]._id });
      totalDeleted++;
    }
  });
  
  print('');
  print('Nettoyage termine!');
  print('Alertes supprimees: ' + totalDeleted);
  print('Alertes restantes: ' + db.kpi_alerts.countDocuments());
"

Write-Host "Nettoyage termine!" -ForegroundColor Green
