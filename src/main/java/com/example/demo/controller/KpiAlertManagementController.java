package com.example.demo.controller;

import com.example.demo.model.KpiAlert;
import com.example.demo.service.KpiAlertManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST pour la gestion des alertes KPI
 */
@RestController
@RequestMapping("/api/kpi-alerts/manage")
@CrossOrigin(origins = "*")
public class KpiAlertManagementController {
    
    @Autowired
    private KpiAlertManagementService alertService;
    
    /**
     * Obtenir les alertes actives de l'utilisateur connecté
     * Pour le Décideur: retourne TOUTES les alertes PENDING_DECISION
     * Pour le Chef de Projet: retourne les alertes qui lui sont assignées
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getActiveAlerts(Authentication auth) {
        try {
            String userId = auth.getName();
            
            // Vérifier si l'utilisateur est un Décideur
            boolean isDecisionMaker = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DECISION_MAKER"));
            
            List<KpiAlert> alerts;
            if (isDecisionMaker) {
                // Pour le Décideur: retourner TOUTES les alertes PENDING_DECISION
                alerts = alertService.getAllPendingDecisionAlerts();
            } else {
                // Pour les autres: filtrer par userId
                alerts = alertService.getActiveAlerts(userId);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", alerts.size());
            response.put("alerts", alerts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtenir les statistiques des alertes
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAlertStats(Authentication auth) {
        try {
            String userId = auth.getName();
            
            // Obtenir les statistiques des alertes
            Map<String, Object> stats = alertService.getAlertStatistics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtenir les alertes résolues récemment
     */
    @GetMapping("/resolved")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getResolvedAlerts(Authentication auth) {
        try {
            String userId = auth.getName();
            List<KpiAlert> alerts = alertService.getRecentlyResolvedAlerts(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", alerts.size());
            response.put("alerts", alerts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtenir les alertes archivées
     */
    @GetMapping("/archived")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getArchivedAlerts(Authentication auth) {
        try {
            String userId = auth.getName();
            List<KpiAlert> alerts = alertService.getArchivedAlerts(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", alerts.size());
            response.put("alerts", alerts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Envoyer une alerte au Chef de Projet (Décideur)
     */
    @PostMapping("/{alertId}/send-to-pm")
    @PreAuthorize("hasAnyRole('DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> sendToProjectManager(
            @PathVariable String alertId,
            Authentication auth) {
        try {
            String userId = auth.getName();
            KpiAlert updated = alertService.sendToProjectManager(alertId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alerte envoyée au Chef de Projet avec succès");
            response.put("alert", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Marquer une alerte comme "En cours"
     */
    @PostMapping("/{alertId}/in-progress")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> markAsInProgress(
            @PathVariable String alertId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        try {
            String userId = auth.getName();
            String comment = body != null ? body.get("comment") : null;
            
            KpiAlert updated = alertService.markAsInProgress(alertId, userId, comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alerte marquée en cours de traitement");
            response.put("alert", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Résoudre une alerte
     */
    @PostMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> resolveAlert(
            @PathVariable String alertId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            String userId = auth.getName();
            String resolutionComment = body.get("resolutionComment");
            String actionsTaken = body.get("actionsTaken");
            
            if (resolutionComment == null || resolutionComment.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Le commentaire de résolution est obligatoire");
                return ResponseEntity.badRequest().body(error);
            }
            
            KpiAlert updated = alertService.resolveAlert(alertId, userId, resolutionComment, actionsTaken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alerte résolue avec succès");
            response.put("alert", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Marquer une alerte comme "Informé" (Chef de Projet)
     */
    @PostMapping("/{alertId}/acknowledge")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> acknowledgeAlert(
            @PathVariable String alertId,
            Authentication auth) {
        try {
            String userId = auth.getName();
            KpiAlert updated = alertService.acknowledgeAlert(alertId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alerte marquée comme informé");
            response.put("alert", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Archiver une alerte
     */
    @PostMapping("/{alertId}/archive")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> archiveAlert(
            @PathVariable String alertId,
            Authentication auth) {
        try {
            String userId = auth.getName();
            KpiAlert updated = alertService.archiveAlert(alertId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Alerte archivée avec succès");
            response.put("alert", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Ajouter un commentaire à une alerte
     */
    @PostMapping("/{alertId}/comment")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable String alertId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            String userId = auth.getName();
            String comment = body.get("comment");
            
            if (comment == null || comment.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Le commentaire ne peut pas être vide");
                return ResponseEntity.badRequest().body(error);
            }
            
            KpiAlert updated = alertService.addComment(alertId, userId, comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Commentaire ajouté avec succès");
            response.put("alert", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtenir l'historique complet d'une alerte
     */
    @GetMapping("/{alertId}/history")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAlertHistory(@PathVariable String alertId) {
        try {
            List<KpiAlert.AlertAction> history = alertService.getAlertHistory(alertId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", history.size());
            response.put("history", history);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtenir les statistiques des alertes
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'DECISION_MAKER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics(Authentication auth) {
        try {
            String userId = auth.getName();
            Map<String, Object> stats = alertService.getAlertStatistics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("statistics", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
