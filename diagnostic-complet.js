// Script MongoDB pour diagnostic complet
// Exécuter avec : mongosh demo_db < diagnostic-complet.js

use demo_db;

print("========================================");
print("🔍 DIAGNOSTIC COMPLET - UTILISATEUR");
print("========================================\n");

// Chercher l'utilisateur "eya ayari"
var user = db.users.findOne({ 
  $or: [
    { name: /eya ayari/i },
    { email: /Eya.Ayari@esprit.tn/i }
  ]
});

if (!user) {
  print("❌ Utilisateur 'eya ayari' non trouvé");
  print("\n📋 Tous les utilisateurs dans la base :");
  db.users.find({}, { username: 1, name: 1, email: 1 }).forEach(function(u) {
    print("   - " + u.username + " (" + u.name + ") - " + u.email);
  });
} else {
  print("✅ Utilisateur trouvé\n");
  
  print("📋 INFORMATIONS COMPLÈTES :");
  print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  print("🆔 ID           : " + user._id);
  print("👤 Username     : " + user.username);
  print("👔 Name         : " + user.name);
  print("📧 Email        : " + user.email);
  print("📱 PhoneNumber  : " + (user.phoneNumber || "❌ NULL"));
  print("🌍 Country      : " + (user.country || "❌ NULL"));
  print("");
  
  print("🎭 RÔLES :");
  if (user.roles && user.roles.length > 0) {
    user.roles.forEach(function(role) {
      if (typeof role === 'object' && role.name) {
        print("   ✅ " + role.name);
      } else if (typeof role === 'string') {
        print("   ✅ " + role);
      } else {
        print("   ⚠️  " + JSON.stringify(role));
      }
    });
  } else {
    print("   ❌ Aucun rôle");
  }
  print("");
  
  print("🔐 MOT DE PASSE :");
  if (user.password) {
    if (user.password.startsWith("$2")) {
      print("   ✅ Hash BCrypt valide : " + user.password.substring(0, 30) + "...");
    } else {
      print("   ❌ ATTENTION : Mot de passe NON hashé !");
    }
  } else {
    print("   ❌ Pas de mot de passe");
  }
  print("");
  
  print("🔒 STATUT COMPTE :");
  print("   Enabled              : " + (user.enabled !== false ? "✅ true" : "❌ false"));
  print("   Account Non Expired  : " + (user.accountNonExpired !== false ? "✅ true" : "❌ false"));
  print("   Account Non Locked   : " + (user.accountNonLocked !== false ? "✅ true" : "❌ false"));
  print("   Credentials Non Expired : " + (user.credentialsNonExpired !== false ? "✅ true" : "❌ false"));
  print("");
  
  // DIAGNOSTIC
  print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  print("🔍 DIAGNOSTIC :");
  print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  
  var problems = [];
  var fixes = [];
  
  if (!user.phoneNumber) {
    problems.push("❌ Téléphone manquant");
    fixes.push("db.users.updateOne({_id: ObjectId(\"" + user._id + "\")}, {$set: {phoneNumber: \"+21612345678\"}})");
  }
  
  if (!user.country) {
    problems.push("❌ Pays manquant");
    fixes.push("db.users.updateOne({_id: ObjectId(\"" + user._id + "\")}, {$set: {country: \"TN\"}})");
  }
  
  if (!user.roles || user.roles.length === 0) {
    problems.push("❌ Aucun rôle assigné");
  }
  
  if (!user.password || !user.password.startsWith("$2")) {
    problems.push("❌ Mot de passe non hashé");
  }
  
  if (problems.length > 0) {
    print("⚠️  PROBLÈMES DÉTECTÉS :");
    problems.forEach(function(p) {
      print("   " + p);
    });
    print("");
    
    if (fixes.length > 0) {
      print("💡 COMMANDES DE CORRECTION :");
      fixes.forEach(function(f) {
        print("   " + f + ";");
      });
      print("");
      
      print("📝 OU CORRECTION GROUPÉE :");
      print("db.users.updateOne(");
      print("  { _id: ObjectId(\"" + user._id + "\") },");
      print("  { $set: {");
      if (!user.phoneNumber) print("      phoneNumber: \"+21612345678\",");
      if (!user.country) print("      country: \"TN\",");
      print("      updatedAt: new Date()");
      print("    }");
      print("  }");
      print(");");
    }
  } else {
    print("✅ Aucun problème détecté !");
    print("   L'utilisateur est correctement configuré.");
  }
}

print("");
print("========================================");
