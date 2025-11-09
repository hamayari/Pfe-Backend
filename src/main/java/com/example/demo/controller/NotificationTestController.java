package com.example.demo.controller;

import com.example.demo.service.EmailService;
import com.example.demo.service.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contrôleur de test pour le système de notification
 * Permet de tester manuellement les différentes fonctionnalités
 */
@RestController
@RequestMapping("/api/notifications/test")
@CrossOrigin(origins = "*")
public class NotificationTestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationSchedulerService schedulerService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Tester l'envoi d'un email de rappel de facture
     */
    @PostMapping("/email/invoice-reminder")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, String>> testInvoiceReminderEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "John Doe") String userName,
            @RequestParam(defaultValue = "INV-2025-001") String invoiceNumber,
            @RequestParam(defaultValue = "Client ABC") String clientName,
            @RequestParam(defaultValue = "15/01/2025") String dueDate,
            @RequestParam(defaultValue = "3") String daysBefore,
            @RequestParam(defaultValue = "1500.00") String amount) {
        
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("commercialName", userName);
            variables.put("invoiceNumber", invoiceNumber);
            variables.put("clientName", clientName);
            variables.put("dueDate", dueDate);
            variables.put("daysBefore", daysBefore);
            variables.put("amount", amount);
            
            emailService.sendInvoiceReminderEmail(email, variables);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Email de rappel de facture envoyé avec succès à " + email);
            response.put("type", "invoice_reminder");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Tester l'envoi d'un email de rappel de convention
     */
    @PostMapping("/email/convention-reminder")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, String>> testConventionReminderEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "John Doe") String userName,
            @RequestParam(defaultValue = "CONV-2025-001") String conventionReference,
            @RequestParam(defaultValue = "Convention Test") String conventionTitle,
            @RequestParam(defaultValue = "15/01/2025") String dueDate,
            @RequestParam(defaultValue = "7") String daysBefore,
            @RequestParam(defaultValue = "5000.00") String amount) {
        
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("commercialName", userName);
            variables.put("conventionReference", conventionReference);
            variables.put("conventionTitle", conventionTitle);
            variables.put("dueDate", dueDate);
            variables.put("daysBefore", daysBefore);
            variables.put("amount", amount);
            
            emailService.sendConventionReminderEmail(email, variables);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Email de rappel de convention envoyé avec succès à " + email);
            response.put("type", "convention_reminder");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Tester l'envoi d'un email de facture en retard
     */
    @PostMapping("/email/overdue-invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, String>> testOverdueInvoiceEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "John Doe") String userName,
            @RequestParam(defaultValue = "INV-2025-001") String invoiceNumber,
            @RequestParam(defaultValue = "Client ABC") String clientName,
            @RequestParam(defaultValue = "01/01/2025") String dueDate,
            @RequestParam(defaultValue = "5") String daysOverdue,
            @RequestParam(defaultValue = "1500.00") String amount) {
        
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("commercialName", userName);
            variables.put("invoiceNumber", invoiceNumber);
            variables.put("clientName", clientName);
            variables.put("dueDate", dueDate);
            variables.put("daysOverdue", daysOverdue);
            variables.put("amount", amount);
            
            emailService.sendOverdueInvoiceEmail(email, variables);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Email de facture en retard envoyé avec succès à " + email);
            response.put("type", "overdue_invoice");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Tester le scheduler manuellement
     */
    @PostMapping("/scheduler/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerScheduler() {
        try {
            schedulerService.triggerManualCheck();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Scheduler déclenché manuellement avec succès");
            response.put("info", "Vérifiez les logs pour voir les notifications envoyées");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors du déclenchement: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtenir les informations de configuration
     */
    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("schedulerEnabled", true);
        config.put("cronExpression", "0 0 9 * * *");
        config.put("reminderDays", "7, 3, 1");
        config.put("emailEnabled", true);
        config.put("smsEnabled", true);
        config.put("availableTemplates", new String[]{
            "invoice_reminder",
            "invoice_reminder_enhanced",
            "convention_reminder",
            "convention_reminder_enhanced",
            "overdue_invoice",
            "payment_confirmation"
        });
        
        return ResponseEntity.ok(config);
    }

    /**
     * Endpoint de santé pour le système de notification
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Notification System");
        health.put("timestamp", System.currentTimeMillis());
        health.put("components", Map.of(
            "emailService", "UP",
            "schedulerService", "UP",
            "analyticsService", "UP"
        ));
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Tester l'envoi d'une notification KPI via WebSocket
     */
    @PostMapping("/websocket/kpi-alert")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER')")
    public ResponseEntity<Map<String, Object>> testKpiAlert(
            @RequestParam(defaultValue = "Taux de conversion") String kpiName,
            @RequestParam(defaultValue = "CRITICAL") String status,
            @RequestParam(defaultValue = "HIGH") String severity,
            @RequestParam(defaultValue = "Le taux de conversion est tombé à 12%, en dessous du seuil critique de 15%") String message,
            @RequestParam(defaultValue = "Analyser les causes de la baisse et mettre en place un plan d'action immédiat") String recommendation) {
        
        try {
            System.out.println("========================================");
            System.out.println("🧪 [TEST] Envoi notification KPI WebSocket");
            System.out.println("📊 KPI: " + kpiName);
            System.out.println("🚨 Sévérité: " + severity);
            System.out.println("========================================");
            
            // Créer la notification KPI
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", UUID.randomUUID().toString());
            notification.put("type", "KPI_ALERT");
            notification.put("kpiName", kpiName);
            notification.put("status", status);
            notification.put("severity", severity);
            notification.put("message", message);
            notification.put("recommendation", recommendation);
            notification.put("dimension", "global");
            notification.put("dimensionValue", "all");
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("currentValue", "12%");
            notification.put("threshold", "15%");
            
            // Envoyer au topic général
            messagingTemplate.convertAndSend("/topic/kpi-alerts", notification);
            System.out.println("✅ [TEST] Notification envoyée au topic /topic/kpi-alerts");
            
            // Envoyer aussi aux utilisateurs spécifiques (PROJECT_MANAGER et DECISION_MAKER)
            messagingTemplate.convertAndSendToUser("projectmanager", "/queue/kpi-alerts", notification);
            messagingTemplate.convertAndSendToUser("decisionmaker", "/queue/kpi-alerts", notification);
            System.out.println("✅ [TEST] Notifications personnelles envoyées");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification KPI envoyée via WebSocket");
            response.put("notification", notification);
            response.put("recipients", new String[]{"projectmanager", "decisionmaker"});
            
            System.out.println("========================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [TEST] Erreur: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Tester l'envoi d'une alerte facture via WebSocket
     */
    @PostMapping("/websocket/invoice-alert")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER')")
    public ResponseEntity<Map<String, Object>> testInvoiceAlert(
            @RequestParam(defaultValue = "INV-2025-001") String invoiceNumber,
            @RequestParam(defaultValue = "OVERDUE") String status,
            @RequestParam(defaultValue = "Facture en retard de 5 jours") String message) {
        
        try {
            System.out.println("========================================");
            System.out.println("🧪 [TEST] Envoi alerte facture WebSocket");
            System.out.println("💰 Facture: " + invoiceNumber);
            System.out.println("========================================");
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", UUID.randomUUID().toString());
            notification.put("type", "INVOICE_ALERT");
            notification.put("invoiceNumber", invoiceNumber);
            notification.put("status", status);
            notification.put("message", message);
            notification.put("timestamp", LocalDateTime.now().toString());
            
            messagingTemplate.convertAndSend("/topic/invoice-alerts", notification);
            System.out.println("✅ [TEST] Alerte facture envoyée");
            System.out.println("========================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alerte facture envoyée via WebSocket");
            response.put("notification", notification);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors de l'envoi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
