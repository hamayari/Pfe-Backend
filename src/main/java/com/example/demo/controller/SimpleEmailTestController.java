package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de test simple pour diagnostiquer les problèmes d'email
 */
@RestController
@RequestMapping("/api/simple-email")
@CrossOrigin(origins = "*")
public class SimpleEmailTestController {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Test simple d'envoi d'email
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testEmail() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🧪 [SIMPLE TEST] Début du test email");
            System.out.println("🧪 [SIMPLE TEST] MailSender: " + (mailSender != null ? "OK" : "NULL"));
            
            // Test simple avec SimpleMailMessage
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("hamayari71@gmail.com");
            message.setSubject("🧪 Test Simple - GestionPro");
            message.setText("Ceci est un test simple d'envoi d'email depuis GestionPro.\n\nSi vous recevez ce message, la configuration SMTP fonctionne !\n\nTimestamp: " + java.time.LocalDateTime.now());
            message.setFrom("hamayari71@gmail.com");
            
            System.out.println("🧪 [SIMPLE TEST] Envoi du message...");
            mailSender.send(message);
            
            System.out.println("✅ [SIMPLE TEST] Email envoyé avec succès !");
            
            response.put("success", true);
            response.put("message", "Email envoyé avec succès");
            response.put("timestamp", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("❌ [SIMPLE TEST] Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            response.put("timestamp", java.time.LocalDateTime.now());
        }
        
        return ResponseEntity.ok(response);
    }
}




