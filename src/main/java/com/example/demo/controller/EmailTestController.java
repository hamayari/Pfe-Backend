package com.example.demo.controller;

import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/email-config")
    public ResponseEntity<Map<String, String>> testEmailConfig() {
        Map<String, String> response = new HashMap<>();
        
        try {
            response.put("status", "success");
            response.put("message", "Configuration email chargée");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/send-test-email")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        
        try {
            System.out.println("🧪 Test d'envoi d'email à: " + email);
            
            String subject = "🧪 Test Email - GestionPro";
            String content = "<h2>Test Email</h2><p>Si vous recevez cet email, la configuration fonctionne!</p>";
            
            emailService.sendEmail(email, subject, content);
            
            response.put("status", "success");
            response.put("message", "Email envoyé avec succès à " + email);
            System.out.println("✅ Email de test envoyé avec succès");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de test:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            e.printStackTrace();
            
            response.put("status", "error");
            response.put("message", "Erreur: " + e.getMessage());
            response.put("cause", e.getCause() != null ? e.getCause().getMessage() : "N/A");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/send-password-reset")
    public ResponseEntity<Map<String, String>> sendPasswordResetTest(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        
        try {
            System.out.println("🧪 Test d'envoi d'email de réinitialisation à: " + email);
            
            String resetToken = "TEST_TOKEN_123456";
            emailService.sendPasswordResetEmail(email, resetToken);
            
            response.put("status", "success");
            response.put("message", "Email de réinitialisation envoyé avec succès");
            System.out.println("✅ Email de réinitialisation envoyé avec succès");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de réinitialisation:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            e.printStackTrace();
            
            response.put("status", "error");
            response.put("message", "Erreur: " + e.getMessage());
            response.put("cause", e.getCause() != null ? e.getCause().getMessage() : "N/A");
            return ResponseEntity.status(500).body(response);
        }
    }
}
