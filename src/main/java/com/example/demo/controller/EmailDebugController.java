package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de debug pour tester l'email avec différentes configurations
 */
@RestController
@RequestMapping("/api/debug/email")
@CrossOrigin(origins = "*")
public class EmailDebugController {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Test simple d'email avec configuration minimale
     */
    @PostMapping("/simple-test")
    public ResponseEntity<Map<String, Object>> simpleEmailTest() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🧪 [EMAIL DEBUG] Test simple d'email");
            
            // Test avec configuration minimale
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("hamayari71@gmail.com");
            message.setSubject("🧪 Test Simple - GestionPro");
            message.setText("Ceci est un test simple d'envoi d'email depuis GestionPro.\n\nSi vous recevez ce message, la configuration SMTP fonctionne !\n\nTimestamp: " + java.time.LocalDateTime.now());
            message.setFrom("hamayari71@gmail.com");
            
            System.out.println("🧪 [EMAIL DEBUG] Envoi du message...");
            mailSender.send(message);
            
            System.out.println("✅ [EMAIL DEBUG] Email envoyé avec succès !");
            
            response.put("success", true);
            response.put("message", "Email envoyé avec succès");
            response.put("timestamp", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("❌ [EMAIL DEBUG] Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            response.put("timestamp", java.time.LocalDateTime.now());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test avec configuration Gmail alternative
     */
    @PostMapping("/gmail-test")
    public ResponseEntity<Map<String, Object>> gmailTest() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🧪 [GMAIL DEBUG] Test Gmail avec configuration alternative");
            
            // Test avec configuration Gmail alternative
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("hamayari71@gmail.com");
            message.setSubject("🧪 Test Gmail - GestionPro");
            message.setText("Test Gmail avec configuration alternative.\n\nTimestamp: " + java.time.LocalDateTime.now());
            message.setFrom("hamayari71@gmail.com");
            
            System.out.println("🧪 [GMAIL DEBUG] Envoi du message Gmail...");
            mailSender.send(message);
            
            System.out.println("✅ [GMAIL DEBUG] Email Gmail envoyé avec succès !");
            
            response.put("success", true);
            response.put("message", "Email Gmail envoyé avec succès");
            response.put("timestamp", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("❌ [GMAIL DEBUG] Erreur envoi email Gmail: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            response.put("timestamp", java.time.LocalDateTime.now());
        }
        
        return ResponseEntity.ok(response);
    }
}




