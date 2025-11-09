// Script MongoDB pour supprimer TOUTES les alertes
// Exécuter avec: mongo gestionpro delete-all-alerts.js

print("🗑️  Suppression de TOUTES les alertes...");

// Compter avant suppression
var countBefore = db.kpiAlerts.count();
print("📊 Nombre d'alertes avant: " + countBefore);

// Supprimer TOUTES les alertes
var result = db.kpiAlerts.deleteMany({});
print("✅ Alertes supprimées: " + result.deletedCount);

// Vérifier après suppression
var countAfter = db.kpiAlerts.count();
print("📊 Nombre d'alertes après: " + countAfter);

if (countAfter === 0) {
    print("✅ Toutes les alertes ont été supprimées avec succès!");
    print("🔄 Redémarrez le backend pour générer les nouvelles alertes");
} else {
    print("⚠️  Il reste encore " + countAfter + " alertes");
}
