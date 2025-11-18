package com.example.demo.controller;

import com.example.demo.service.AutomaticKpiAlertService;
import com.example.demo.service.InvoiceAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour déclencher manuellement la vérification des KPI
 */
@RestController
@RequestMapping("/api/kpi-alerts")
@CrossOrigin(origins = "*")
public class KpiAlertController {
    
    @Autowired
    private AutomaticKpiAlertService automaticKpiAlertService;
    
    @Autowired
    private InvoiceAlertService invoiceAlertService;
    
    /**
     * Déclencher manuellement la vérification des KPI (ancien système)
     */
    @PostMapping("/check-now")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER')")
    public ResponseEntity<Map<String, Object>> checkKpiNow() {
        System.out.println("========================================");
        System.out.println("🧪 [MANUAL TRIGGER] Vérification manuelle des KPI");
        System.out.println("========================================");
        
        try {
            // Déclencher la vérification automatique
            automaticKpiAlertService.checkKpiAnomalies();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Vérification des KPI effectuée avec succès");
            response.put("info", "Les notifications ont été envoyées au Chef de Projet si des anomalies ont été détectées");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [MANUAL TRIGGER] Erreur: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors de la vérification: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * ✅ Vérifier les factures PENDING et créer des alertes individuelles
     * NOUVEAU SYSTÈME: 1 alerte = 1 facture PENDING
     * Le Décideur reçoit les alertes et peut les déléguer au Chef de Projet
     */
    @PostMapping("/check-pending-invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER')")
    public ResponseEntity<Map<String, Object>> checkPendingInvoices() {
        try {
            var alerts = invoiceAlertService.checkPendingInvoices();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", alerts.size() + " alertes créées pour les factures PENDING");
            response.put("count", alerts.size());
            response.put("alerts", alerts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur vérification factures PENDING: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Vérifier les factures en retard et créer des alertes individuelles
     * NOUVEAU SYSTÈME: 1 alerte = 1 facture
     */
    @PostMapping("/check-overdue-invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER')")
    public ResponseEntity<Map<String, Object>> checkOverdueInvoices() {
        try {
            var alerts = invoiceAlertService.checkOverdueInvoices();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", alerts.size() + " alertes créées pour les factures en retard");
            response.put("count", alerts.size());
            response.put("alerts", alerts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur vérification factures: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Obtenir la configuration des seuils KPI
     */
    @GetMapping("/thresholds")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER')")
    public ResponseEntity<Map<String, Object>> getThresholds() {
        Map<String, Object> thresholds = new HashMap<>();
        
        Map<String, Object> tauxRetard = new HashMap<>();
        tauxRetard.put("warningThreshold", 10.0);
        tauxRetard.put("criticalThreshold", 15.0);
        tauxRetard.put("displayName", "Taux de factures en retard");
        tauxRetard.put("unit", "%");
        thresholds.put("TAUX_RETARD", tauxRetard);
        
        Map<String, Object> montantImpaye = new HashMap<>();
        montantImpaye.put("warningThreshold", 20000.0);
        montantImpaye.put("criticalThreshold", 30000.0);
        montantImpaye.put("displayName", "Montant total impayé");
        montantImpaye.put("unit", "TND");
        thresholds.put("MONTANT_IMPAYE", montantImpaye);
        
        Map<String, Object> tauxRegul = new HashMap<>();
        tauxRegul.put("warningThreshold", 70.0);
        tauxRegul.put("criticalThreshold", 60.0);
        tauxRegul.put("displayName", "Taux de régularisation");
        tauxRegul.put("unit", "%");
        thresholds.put("TAUX_REGULARISATION", tauxRegul);
        
        Map<String, Object> delaiPaiement = new HashMap<>();
        delaiPaiement.put("warningThreshold", 30.0);
        delaiPaiement.put("criticalThreshold", 45.0);
        delaiPaiement.put("displayName", "Délai moyen de paiement");
        delaiPaiement.put("unit", "jours");
        thresholds.put("DELAI_PAIEMENT", delaiPaiement);
        
        Map<String, Object> tauxConv = new HashMap<>();
        tauxConv.put("warningThreshold", 15.0);
        tauxConv.put("criticalThreshold", 12.0);
        tauxConv.put("displayName", "Taux de conversion");
        tauxConv.put("unit", "%");
        thresholds.put("TAUX_CONVERSION", tauxConv);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("thresholds", thresholds);
        response.put("checkInterval", "5 minutes");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de santé
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Automatic KPI Alert Service");
        health.put("checkInterval", "5 minutes");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Vérifier l'état actuel des alertes vs factures OVERDUE
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER')")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            // Compter les factures OVERDUE
            long overdueInvoicesCount = invoiceAlertService.countOverdueInvoices();
            
            // Compter les alertes
            long alertsCount = automaticKpiAlertService.countAlerts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("overdueInvoices", overdueInvoicesCount);
            response.put("alerts", alertsCount);
            response.put("match", overdueInvoicesCount == alertsCount);
            response.put("message", overdueInvoicesCount == alertsCount 
                ? "✅ Parfait: 1 alerte = 1 facture OVERDUE" 
                : "⚠️ Désynchronisation détectée");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Diagnostic complet du système d'alertes
     */
    @GetMapping("/diagnostic")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER')")
    public ResponseEntity<Map<String, Object>> getDiagnostic() {
        try {
            Map<String, Object> diagnostic = new HashMap<>();
            
            // Compter par dimension
            long alertsFACTURE = automaticKpiAlertService.countAlertsByDimension("FACTURE");
            long alertsINVOICE = automaticKpiAlertService.countAlertsByDimension("INVOICE");
            long alertsOther = automaticKpiAlertService.countAlerts() - alertsFACTURE - alertsINVOICE;
            
            diagnostic.put("alertes_FACTURE", alertsFACTURE);
            diagnostic.put("alertes_INVOICE", alertsINVOICE);
            diagnostic.put("alertes_autres", alertsOther);
            diagnostic.put("total_alertes", automaticKpiAlertService.countAlerts());
            diagnostic.put("factures_OVERDUE", invoiceAlertService.countOverdueInvoices());
            
            // Problèmes détectés
            List<String> problems = new ArrayList<>();
            if (alertsINVOICE > 0) {
                problems.add("⚠️ " + alertsINVOICE + " alertes avec dimension INVOICE (anciennes, à supprimer)");
            }
            if (alertsOther > 0) {
                problems.add("⚠️ " + alertsOther + " alertes avec dimension inconnue");
            }
            
            long expected = invoiceAlertService.countOverdueInvoices();
            long actual = alertsFACTURE;
            if (actual != expected) {
                problems.add("❌ Désynchronisation: " + expected + " factures OVERDUE mais " + actual + " alertes FACTURE");
            }
            
            diagnostic.put("problems", problems);
            diagnostic.put("healthy", problems.isEmpty());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "diagnostic", diagnostic
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Déléguer une alerte du Décideur au Chef de Projet
     */
    @PostMapping("/{alertId}/delegate-to-pm")
    @PreAuthorize("hasRole('DECISION_MAKER')")
    public ResponseEntity<Map<String, Object>> delegateToProjectManager(@PathVariable String alertId) {
        try {
            System.out.println("📤 Délégation d'alerte: " + alertId + " → Chef de Projet");
            
            // Déléguer l'alerte
            boolean success = automaticKpiAlertService.delegateAlertToProjectManager(alertId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Alerte déléguée au Chef de Projet avec succès");
                response.put("alertId", alertId);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Alerte non trouvée"
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur délégation alerte: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}
