package com.example.demo.controller;

import com.example.demo.service.DashboardAlertToNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur pour synchroniser les alertes dashboard → notifications navbar
 */
@RestController
@RequestMapping("/api/dashboard-alerts")
@CrossOrigin(origins = "*")
public class DashboardAlertSyncController {
    
    @Autowired
    private DashboardAlertToNotificationService syncService;
    
    /**
     * 🔄 Forcer la synchronisation manuelle
     * Les alertes du dashboard apparaîtront dans la navbar (🔔)
     */
    @PostMapping("/sync-now")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER', 'COMMERCIAL')")
    public ResponseEntity<Map<String, Object>> syncNow() {
        System.out.println("========================================");
        System.out.println("🔄 [SYNC] Synchronisation manuelle des alertes");
        System.out.println("========================================");
        
        try {
            Map<String, Object> result = syncService.forceSyncNow();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Erreur lors de la synchronisation: " + e.getMessage()
            ));
        }
    }
    
    /**
     * ℹ️ Informations sur le système de synchronisation
     */
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER', 'COMMERCIAL')")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = Map.of(
            "status", "active",
            "schedule", "Toutes les 5 minutes",
            "description", "Synchronise automatiquement les alertes du dashboard vers les notifications navbar",
            "alertTypes", Map.of(
                "upcoming_invoices", "Factures à échéance proche (≤ 7 jours)",
                "overdue_invoices", "Factures en retard",
                "expired_conventions", "Conventions expirées",
                "upcoming_conventions", "Conventions à renouveler (≤ 30 jours)"
            ),
            "behavior", Map.of(
                "dashboard", "Les alertes restent visibles dans le dashboard",
                "navbar", "Les alertes apparaissent aussi dans l'icône 🔔",
                "badge", "Le compteur de notifications est mis à jour automatiquement"
            ),
            "testEndpoint", "/api/dashboard-alerts/sync-now"
        );
        
        return ResponseEntity.ok(info);
    }
}
