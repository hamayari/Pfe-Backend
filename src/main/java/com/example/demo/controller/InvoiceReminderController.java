package com.example.demo.controller;

import com.example.demo.service.InvoiceReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur pour tester et gérer les rappels automatiques de factures
 */
@RestController
@RequestMapping("/api/invoice-reminders")
@CrossOrigin(origins = "*")
public class InvoiceReminderController {
    
    @Autowired
    private InvoiceReminderService invoiceReminderService;
    
    /**
     * 🧪 TEST : Déclencher manuellement les rappels
     * Accessible par ADMIN, DECISION_MAKER, PROJECT_MANAGER, COMMERCIAL
     */
    @PostMapping("/send-test")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER', 'COMMERCIAL')")
    public ResponseEntity<Map<String, Object>> sendTestReminders() {
        System.out.println("========================================");
        System.out.println("🧪 [TEST] Déclenchement manuel des rappels");
        System.out.println("========================================");
        
        try {
            Map<String, Object> result = invoiceReminderService.sendTestReminders();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Erreur lors de l'envoi des rappels: " + e.getMessage()
            ));
        }
    }
    
    /**
     * ℹ️ INFO : Obtenir les informations sur le système de rappels
     */
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER', 'COMMERCIAL')")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = Map.of(
            "status", "active",
            "schedule", "Tous les jours à 8h00",
            "types", Map.of(
                "upcoming", "Rappels AVANT échéance (7, 3, 1 jour)",
                "overdue", "Rappels APRÈS échéance (factures en retard)",
                "pending", "Rappels factures en attente (> 30 jours)"
            ),
            "channels", Map.of(
                "inApp", "Notification dans l'application (toujours)",
                "email", "Email (si configuré)",
                "sms", "SMS (uniquement pour alertes critiques)"
            ),
            "testEndpoint", "/api/invoice-reminders/send-test"
        );
        
        return ResponseEntity.ok(info);
    }
}
