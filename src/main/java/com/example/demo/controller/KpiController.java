package com.example.demo.controller;

import com.example.demo.model.KpiAlert;
import com.example.demo.service.KpiEvaluatorService;
import com.example.demo.repository.KpiAlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kpi")
@RequiredArgsConstructor
@Tag(name = "KPI", description = "KPI Analysis and Alerts APIs")
public class KpiController {

    private final KpiEvaluatorService kpiEvaluatorService;
    private final KpiAlertRepository alertRepository;
    
    /**
     * Nettoie les alertes obsolètes au démarrage du backend
     * ⚠️ DÉSACTIVÉ pour les tests - MongoDB peut ne pas être disponible
     */
    @PostConstruct
    public void cleanupObsoleteAlertsOnStartup() {
        try {
            System.out.println("========================================");
            System.out.println("🧹 VÉRIFICATION DES ALERTES AU DÉMARRAGE");
            long count = alertRepository.count();
            System.out.println("📊 Alertes existantes: " + count);
            
            // Les alertes seront nettoyées automatiquement lors du prochain /api/kpi/analyze
            // On ne supprime plus tout au démarrage pour préserver les alertes valides
            
            System.out.println("✅ Prêt à analyser les KPI");
            System.out.println("========================================");
        } catch (Exception e) {
            System.out.println("⚠️ MongoDB non disponible - Mode dégradé");
            System.out.println("========================================");
        }
    }

    /**
     * Déclenche l'analyse de tous les KPI et crée les alertes
     * ⚠️ Endpoint PUBLIC pour permettre le déclenchement manuel sans authentification
     */
    @PostMapping("/analyze")
    @Operation(summary = "Analyze KPIs", description = "Analyzes all KPIs and creates alerts for overdue invoices")
    public ResponseEntity<?> analyzeKpis() {
        System.out.println("========================================");
        System.out.println("🔍 DÉCLENCHEMENT MANUEL DE L'ANALYSE");
        System.out.println("========================================");
        
        try {
            List<KpiAlert> alerts = kpiEvaluatorService.analyzeAllKpis();
            
            System.out.println("✅ Analyse terminée avec succès");
            System.out.println("========================================");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Analyse des KPI terminée",
                "alertsCreated", alerts.size(),
                "alerts", alerts
            ));
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'analyse des KPI: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de l'analyse: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint de test pour vérifier que le contrôleur fonctionne
     */
    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Tests if the KPI controller is working")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "KPI Controller is working"
        ));
    }
    
    /**
     * Supprime toutes les alertes (pour nettoyage manuel)
     */
    @DeleteMapping("/alerts")
    @Operation(summary = "Delete all alerts", description = "Deletes all KPI alerts from the database")
    public ResponseEntity<?> deleteAllAlerts() {
        System.out.println("🗑️ Suppression manuelle de toutes les alertes...");
        long count = alertRepository.count();
        
        if (count > 0) {
            alertRepository.deleteAll();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Toutes les alertes ont été supprimées",
                "deletedCount", count
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Aucune alerte à supprimer",
            "deletedCount", 0
        ));
    }
}
