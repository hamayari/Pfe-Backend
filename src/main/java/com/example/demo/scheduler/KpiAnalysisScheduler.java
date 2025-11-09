package com.example.demo.scheduler;

import com.example.demo.model.KpiAlert;
import com.example.demo.service.KpiEvaluatorService;
import com.example.demo.service.KpiNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ❌ SCHEDULER DÉSACTIVÉ - Remplacé par AlertScheduler
 * Ce scheduler créait des doublons avec AlertScheduler
 * @deprecated Utiliser AlertScheduler à la place
 */
@Deprecated
// @Component - DÉSACTIVÉ pour éviter les doublons
public class KpiAnalysisScheduler {
    
    @Autowired
    private KpiEvaluatorService evaluatorService;
    
    @Autowired
    private KpiNotificationService notificationService;
    
    /**
     * Analyse quotidienne des KPI (chaque jour à 8h00)
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void dailyKpiAnalysis() {
        System.out.println("========================================");
        System.out.println("🔍 [KPI SCHEDULER] Analyse quotidienne démarrée à " + LocalDateTime.now());
        System.out.println("========================================");
        
        try {
            // 1. Analyser tous les KPI
            List<KpiAlert> alerts = evaluatorService.analyzeAllKpis();
            
            System.out.println("📊 [KPI SCHEDULER] " + alerts.size() + " alerte(s) détectée(s)");
            
            // 2. Envoyer les notifications
            if (!alerts.isEmpty()) {
                notificationService.sendAlertNotifications(alerts);
                System.out.println("📨 [KPI SCHEDULER] Notifications envoyées");
            }
            
            System.out.println("✅ [KPI SCHEDULER] Analyse quotidienne terminée");
            
        } catch (Exception e) {
            System.err.println("❌ [KPI SCHEDULER] Erreur lors de l'analyse: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("========================================");
    }
    
    /**
     * Analyse hebdomadaire des KPI (chaque lundi à 9h00)
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void weeklyKpiAnalysis() {
        System.out.println("========================================");
        System.out.println("📅 [KPI SCHEDULER] Analyse hebdomadaire démarrée à " + LocalDateTime.now());
        System.out.println("========================================");
        
        try {
            // Analyse complète avec rapport détaillé
            List<KpiAlert> alerts = evaluatorService.analyzeAllKpis();
            
            System.out.println("📊 [KPI SCHEDULER] Rapport hebdomadaire : " + alerts.size() + " alerte(s)");
            
            // Envoyer un rapport hebdomadaire au décideur
            notificationService.sendWeeklyReport(alerts);
            
            System.out.println("✅ [KPI SCHEDULER] Rapport hebdomadaire envoyé");
            
        } catch (Exception e) {
            System.err.println("❌ [KPI SCHEDULER] Erreur lors du rapport hebdomadaire: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("========================================");
    }
    
    /**
     * Analyse mensuelle des KPI (le 1er de chaque mois à 10h00)
     */
    @Scheduled(cron = "0 0 10 1 * *")
    public void monthlyKpiAnalysis() {
        System.out.println("========================================");
        System.out.println("📆 [KPI SCHEDULER] Analyse mensuelle démarrée à " + LocalDateTime.now());
        System.out.println("========================================");
        
        try {
            // Analyse complète avec tendances
            List<KpiAlert> alerts = evaluatorService.analyzeAllKpis();
            
            System.out.println("📊 [KPI SCHEDULER] Rapport mensuel : " + alerts.size() + " alerte(s)");
            
            // Envoyer un rapport mensuel complet
            notificationService.sendMonthlyReport(alerts);
            
            System.out.println("✅ [KPI SCHEDULER] Rapport mensuel envoyé");
            
        } catch (Exception e) {
            System.err.println("❌ [KPI SCHEDULER] Erreur lors du rapport mensuel: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("========================================");
    }
    
    /**
     * Vérification toutes les 6 heures (au lieu de toutes les heures)
     * Pour détecter rapidement les anomalies critiques sans surcharger
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void periodicKpiCheck() {
        System.out.println("⏰ [KPI SCHEDULER] Vérification périodique à " + LocalDateTime.now());
        
        try {
            // Analyser uniquement les KPI critiques
            List<KpiAlert> criticalAlerts = evaluatorService.analyzeAllKpis().stream()
                .filter(alert -> "HIGH".equals(alert.getSeverity()) || "CRITICAL".equals(alert.getSeverity()))
                .toList();
            
            if (!criticalAlerts.isEmpty()) {
                System.out.println("🚨 [KPI SCHEDULER] " + criticalAlerts.size() + " alerte(s) critique(s) détectée(s)");
                notificationService.sendUrgentAlerts(criticalAlerts);
            } else {
                System.out.println("✅ [KPI SCHEDULER] Aucune alerte critique");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [KPI SCHEDULER] Erreur lors de la vérification périodique: " + e.getMessage());
        }
    }
}
