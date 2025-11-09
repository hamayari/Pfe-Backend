package com.example.demo.controller;

import com.example.demo.service.NotificationAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour les analyses et statistiques des notifications
 */
@RestController
@RequestMapping("/api/notifications/analytics")
@CrossOrigin(origins = "*")
public class NotificationAnalyticsController {

    @Autowired
    private NotificationAnalyticsService analyticsService;

    /**
     * Obtenir les statistiques globales
     */
    @GetMapping("/global")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, Object>> getGlobalStatistics() {
        return ResponseEntity.ok(analyticsService.getGlobalStatistics());
    }

    /**
     * Obtenir les statistiques par période
     */
    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, Object>> getStatisticsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getStatisticsByPeriod(startDate, endDate));
    }

    /**
     * Obtenir les statistiques par utilisateur
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'COMMERCIAL')")
    public ResponseEntity<Map<String, Object>> getStatisticsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsService.getStatisticsByUser(userId));
    }

    /**
     * Obtenir les statistiques par type de notification
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, Object>> getStatisticsByType(@PathVariable String type) {
        return ResponseEntity.ok(analyticsService.getStatisticsByType(type));
    }

    /**
     * Obtenir le rapport de performance
     */
    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, Object>> getPerformanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getPerformanceReport(startDate, endDate));
    }

    /**
     * Obtenir les échecs récents
     */
    @GetMapping("/failures")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<List<Map<String, Object>>> getRecentFailures(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getRecentFailures(limit));
    }

    /**
     * Obtenir les métriques d'engagement
     */
    @GetMapping("/engagement")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, Object>> getUserEngagementMetrics() {
        return ResponseEntity.ok(analyticsService.getUserEngagementMetrics());
    }

    /**
     * Obtenir les recommandations d'amélioration
     */
    @GetMapping("/recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<List<String>> getImprovementRecommendations() {
        return ResponseEntity.ok(analyticsService.getImprovementRecommendations());
    }

    /**
     * Obtenir le dashboard complet d'analytics
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Par défaut: derniers 30 jours
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Map<String, Object> dashboard = Map.of(
            "globalStats", analyticsService.getGlobalStatistics(),
            "periodStats", analyticsService.getStatisticsByPeriod(startDate, endDate),
            "performanceReport", analyticsService.getPerformanceReport(startDate, endDate),
            "engagement", analyticsService.getUserEngagementMetrics(),
            "recentFailures", analyticsService.getRecentFailures(5),
            "recommendations", analyticsService.getImprovementRecommendations()
        );
        
        return ResponseEntity.ok(dashboard);
    }
}
