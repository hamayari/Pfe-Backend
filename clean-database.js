// Script MongoDB pour nettoyer la base de données
// Utilisation: mongo clean-database.js

print("🧹 Nettoyage de la base de données...");

// Se connecter à la base de données
use gestionpro;

// Supprimer tous les utilisateurs superadmin existants
print("Suppression des utilisateurs superadmin existants...");
db.users.deleteMany({username: "superadmin"});
print("Utilisateurs superadmin supprimés.");

// Supprimer tous les rôles SUPER_ADMIN existants
print("Suppression des rôles SUPER_ADMIN existants...");
db.roles.deleteMany({name: "ROLE_SUPER_ADMIN"});
print("Rôles SUPER_ADMIN supprimés.");

// Vérifier le nettoyage
print("Vérification du nettoyage...");
print("Utilisateurs superadmin restants:", db.users.countDocuments({username: "superadmin"}));
print("Rôles SUPER_ADMIN restants:", db.roles.countDocuments({name: "ROLE_SUPER_ADMIN"}));

print("✅ Nettoyage terminé !");

















