// Script MongoDB pour nettoyer les alertes en double
// Exécuter dans MongoDB Compass ou mongo shell

// Connexion à la base de données
use demo_db;

print("========================================");
print("🧹 Nettoyage des alertes en double");
print("========================================");

// 1. Compter les alertes avant nettoyage
const countBefore = db.kpiAlerts.countDocuments();
print("📊 Nombre d'alertes avant nettoyage: " + countBefore);

// 2. Trouver les alertes en double (même KPI, dimension, dimensionValue et statut PENDING_DECISION)
const duplicates = db.kpiAlerts.aggregate([
    {
        $match: {
            alertStatus: "PENDING_DECISION"
        }
    },
    {
        $group: {
            _id: {
                kpiName: "$kpiName",
                dimension: "$dimension",
                dimensionValue: "$dimensionValue"
            },
            count: { $sum: 1 },
            ids: { $push: "$_id" },
            dates: { $push: "$detectedAt" }
        }
    },
    {
        $match: {
            count: { $gt: 1 }
        }
    }
]).toArray();

print("🔍 Groupes d'alertes en double trouvés: " + duplicates.length);

// 3. Pour chaque groupe de doublons, garder seulement la plus récente
let deletedCount = 0;
duplicates.forEach(function(group) {
    print("\n📋 Groupe: " + group._id.kpiName + " - " + group._id.dimensionValue);
    print("   Nombre de doublons: " + group.count);
    
    // Trier les IDs par date (garder la plus récente)
    const sortedIds = group.ids.map((id, index) => ({
        id: id,
        date: group.dates[index]
    })).sort((a, b) => new Date(b.date) - new Date(a.date));
    
    // Garder le premier (le plus récent), supprimer les autres
    const toKeep = sortedIds[0].id;
    const toDelete = sortedIds.slice(1).map(item => item.id);
    
    print("   ✅ Garder: " + toKeep + " (date: " + sortedIds[0].date + ")");
    print("   🗑️  Supprimer: " + toDelete.length + " alerte(s)");
    
    // Supprimer les doublons
    const result = db.kpiAlerts.deleteMany({
        _id: { $in: toDelete }
    });
    
    deletedCount += result.deletedCount;
});

// 4. Compter les alertes après nettoyage
const countAfter = db.kpiAlerts.countDocuments();
print("\n========================================");
print("📊 Résumé du nettoyage:");
print("   Avant: " + countBefore + " alertes");
print("   Supprimées: " + deletedCount + " alertes");
print("   Après: " + countAfter + " alertes");
print("========================================");

// 5. Afficher les alertes restantes par type
print("\n📊 Alertes restantes par KPI:");
db.kpiAlerts.aggregate([
    {
        $match: {
            alertStatus: "PENDING_DECISION"
        }
    },
    {
        $group: {
            _id: "$kpiName",
            count: { $sum: 1 }
        }
    },
    {
        $sort: { count: -1 }
    }
]).forEach(function(item) {
    print("   " + item._id + ": " + item.count + " alerte(s)");
});

print("\n✅ Nettoyage terminé!");
