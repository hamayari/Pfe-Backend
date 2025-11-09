package com.example.demo.controller;

import com.example.demo.model.KpiAlert;
import com.example.demo.model.KpiThreshold;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.repository.KpiThresholdRepository;
import com.example.demo.service.KpiCalculatorService;
import com.example.demo.service.KpiEvaluatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Contrôleur pour le module Analyse & Notification KPI
 * Accessible par les DECIDEURS et PROJECT_MANAGERS
 */
@RestController
@RequestMapping("/api/kpi-analysis")
@CrossOrigin(origins = "*")
public class KpiAnalysisController {
    
    @Autowired
    private KpiCalculatorService calculatorService;
    
    @Autowired
    private KpiEvaluatorService evaluatorService;
    
    @Autowired
    private KpiThresholdRepository thresholdRepository;
    
    @Autowired
    private KpiAlertRepository alertRepository;
    
    /**
     * GET /api/kpi-analysis/global - KPI globaux
     */
    @GetMapping("/global")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_PROJECT_MANAGER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getGlobalKpis() {
        Map<String, KpiCalculatorService.KpiResult> kpis = calculatorService.calculateGlobalKpis();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("kpis", kpis);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/kpi-analysis/by-gouvernorat - KPI par gouvernorat
     */
    @GetMapping("/by-gouvernorat")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_PROJECT_MANAGER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getKpisByGouvernorat() {
        Map<String, Map<String, KpiCalculatorService.KpiResult>> kpis = calculatorService.calculateKpisByGouvernorat();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("kpis", kpis);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/kpi-analysis/by-structure - KPI par structure
     */
    @GetMapping("/by-structure")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_PROJECT_MANAGER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getKpisByStructure() {
        Map<String, Map<String, KpiCalculatorService.KpiResult>> kpis = calculatorService.calculateKpisByStructure();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("kpis", kpis);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/kpi-analysis/alerts - Toutes les alertes actives
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_PROJECT_MANAGER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getActiveAlerts() {
        try {
            List<KpiAlert> alerts = alertRepository.findByAlertStatus("ACTIVE");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("alerts", alerts != null ? alerts : new ArrayList<>());
            response.put("count", alerts != null ? alerts.size() : 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération alertes: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération des alertes: " + e.getMessage());
            errorResponse.put("alerts", new ArrayList<>());
            errorResponse.put("count", 0);
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * GET /api/kpi-analysis/alerts/critical - Alertes critiques
     */
    @GetMapping("/alerts/critical")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_PROJECT_MANAGER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCriticalAlerts() {
        try {
            List<KpiAlert> alerts = alertRepository.findByAlertStatusAndSeverityOrderByDetectedAtDesc("ACTIVE", "HIGH");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("alerts", alerts != null ? alerts : new ArrayList<>());
            response.put("count", alerts != null ? alerts.size() : 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération alertes critiques: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération des alertes critiques: " + e.getMessage());
            errorResponse.put("alerts", new ArrayList<>());
            errorResponse.put("count", 0);
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * POST /api/kpi-analysis/analyze - Déclencher une analyse manuelle
     */
    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerAnalysis() {
        List<KpiAlert> alerts = evaluatorService.analyzeAllKpis();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Analyse terminée");
        response.put("alertsGenerated", alerts.size());
        response.put("alerts", alerts);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * PUT /api/kpi-analysis/alerts/{id}/resolve - Résoudre une alerte
     */
    @PutMapping("/alerts/{id}/resolve")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_PROJECT_MANAGER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> resolveAlert(@PathVariable String id, @RequestParam String resolvedBy) {
        Optional<KpiAlert> alertOpt = alertRepository.findById(id);
        
        if (!alertOpt.isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Alerte non trouvée");
            return ResponseEntity.ok(errorResponse);
        }
        
        KpiAlert alert = alertOpt.get();
        alert.setAlertStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alertRepository.save(alert);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Alerte résolue");
        response.put("alert", alert);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/kpi-analysis/thresholds - Tous les seuils configurés
     */
    @GetMapping("/thresholds")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getThresholds() {
        List<KpiThreshold> thresholds = thresholdRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("thresholds", thresholds);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/kpi-analysis/thresholds - Créer/Modifier un seuil
     */
    @PostMapping("/thresholds")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> saveThreshold(@RequestBody KpiThreshold threshold) {
        threshold.setUpdatedAt(LocalDateTime.now());
        if (threshold.getCreatedAt() == null) {
            threshold.setCreatedAt(LocalDateTime.now());
        }
        
        KpiThreshold saved = thresholdRepository.save(threshold);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Seuil enregistré");
        response.put("threshold", saved);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/kpi-analysis/dashboard - Dashboard complet pour DECIDEUR
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ROLE_DECISION_MAKER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        // KPI globaux
        Map<String, KpiCalculatorService.KpiResult> globalKpis = calculatorService.calculateGlobalKpis();
        
        // KPI par gouvernorat
        Map<String, Map<String, KpiCalculatorService.KpiResult>> kpisByGov = calculatorService.calculateKpisByGouvernorat();
        
        // Alertes actives
        List<KpiAlert> activeAlerts = alertRepository.findByAlertStatus("ACTIVE");
        
        // Alertes critiques
        List<KpiAlert> criticalAlerts = alertRepository.findByAlertStatusAndSeverityOrderByDetectedAtDesc("ACTIVE", "HIGH");
        
        // Statistiques
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAlerts", activeAlerts.size());
        stats.put("criticalAlerts", criticalAlerts.size());
        stats.put("mediumAlerts", alertRepository.countBySeverity("MEDIUM"));
        stats.put("lowAlerts", alertRepository.countBySeverity("LOW"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("globalKpis", globalKpis);
        response.put("kpisByGouvernorat", kpisByGov);
        response.put("activeAlerts", activeAlerts);
        response.put("criticalAlerts", criticalAlerts);
        response.put("stats", stats);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}
