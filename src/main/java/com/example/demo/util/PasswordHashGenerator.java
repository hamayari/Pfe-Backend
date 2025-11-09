package com.example.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitaire pour générer des hash BCrypt de mots de passe
 * Utilisé pour créer des utilisateurs de test avec des mots de passe corrects
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("========================================");
        System.out.println("🔐 GÉNÉRATEUR DE HASH BCRYPT");
        System.out.println("========================================\n");
        
        // Générer des hash pour les mots de passe courants
        String[] passwords = {
            "Test123!",
            "Commercial123!",
            "password",
            "admin123",
            "test123"
        };
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println();
        }
        
        System.out.println("========================================");
        System.out.println("💡 UTILISATION");
        System.out.println("========================================");
        System.out.println("Copiez le hash généré et utilisez-le dans MongoDB:");
        System.out.println();
        System.out.println("db.users.updateOne(");
        System.out.println("  { username: \"test_commercial\" },");
        System.out.println("  { $set: { password: \"COLLER_LE_HASH_ICI\" } }");
        System.out.println(");");
        System.out.println();
    }
}
