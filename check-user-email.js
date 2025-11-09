// Script MongoDB pour vérifier si un utilisateur existe avec un email
// Exécuter dans MongoDB Compass ou mongo shell

use demo_db;

print("========================================");
print("🔍 Vérification des utilisateurs");
print("========================================");

// Email à vérifier
const emailToCheck = "eyayari123@gmail.com";

print("\n📧 Recherche de l'email: " + emailToCheck);

const user = db.users.findOne({ email: emailToCheck });

if (user) {
    print("\n✅ Utilisateur trouvé!");
    print("   ID: " + user._id);
    print("   Username: " + user.username);
    print("   Email: " + user.email);
    print("   Actif: " + (user.active !== false));
    
    if (user.resetToken) {
        print("\n⚠️ Token de réinitialisation existant:");
        print("   Token: " + user.resetToken);
        print("   Expiration: " + user.resetTokenExpiry);
    } else {
        print("\n✅ Aucun token de réinitialisation en cours");
    }
} else {
    print("\n❌ Aucun utilisateur trouvé avec cet email!");
    print("\n💡 Emails disponibles dans la base:");
    
    db.users.find({}, { email: 1, username: 1, _id: 0 }).forEach(function(u) {
        print("   - " + u.email + " (username: " + u.username + ")");
    });
}

print("\n========================================");
