package com.example.demo.controller;

import com.example.demo.service.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller pour tester manuellement le système de notifications
 * Permet de déclencher les vérifications d'échéances sans attendre le scheduler
 */
@RestController
@RequestMapping("/api/test/scheduler")
@CrossOrigin(origins = "*")
public class NotificationSchedulerController {

    @Autowired
    private NotificationSchedulerService notificationSchedulerService;

    /**
     * Déclencher manuellement la vérification des échéances
     * Point 6 du cahier des charges : Test du processus planifié
     */
    @PostMapping("/trigger-check")
    public ResponseEntity<Map<String, Object>> triggerManualCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🧪 [TEST MANUEL] Déclenchement manuel du scheduler de notifications");
            
            // Déclencher la vérification manuelle
            notificationSchedulerService.triggerManualCheck();
            
            response.put("success", true);
            response.put("message", "Vérification des échéances déclenchée avec succès");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            System.out.println("✅ [TEST MANUEL] Vérification terminée");
            
        } catch (Exception e) {
            System.err.println("❌ [TEST MANUEL] Erreur: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir le statut du scheduler
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("scheduler", "NotificationSchedulerService");
        response.put("status", "active");
        response.put("cron", "0 0 9 * * *"); // Tous les jours à 9h00
        response.put("reminderDays", "7,3,1");
        response.put("emailEnabled", true);
        response.put("smsEnabled", true);
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}











