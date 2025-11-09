package com.example.demo.service;

import com.example.demo.model.KpiAlert;
import com.example.demo.repository.KpiAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour gérer l'historique des notifications
 * Le Chef de Projet garde TOUJOURS l'historique complet
 */
@Service
public class NotificationHistoryService {
    
    @Autowired
    private KpiAlertRepository alertRepository;
    
    /**
     * Obtenir l'historique complet des notifications pour un utilisateur
     * Inclut TOUTES les alertes (actives + résolues + archivées)
     */
    public Map<String, Object> getNotificationHistory(String userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        // Récupérer toutes les alertes où l'utilisateur est destinataire
        List<KpiAlert> allAlerts = alertRepository.findByRecipientsContaining(userId);
        
        // Filtrer par date
        List<KpiAlert> recentAlerts = allAlerts.stream()
            .filter(alert -> alert.getDetectedAt() != null && alert.getDetectedAt().isAfter(since))
            .sorted((a, b) -> b.getDetectedAt().compareTo(a.getDetectedAt()))
            .collect(Collectors.toList());
        
        // Grouper par statut
        Map<String, List<KpiAlert>> groupedByStatus = recentAlerts.stream()
            .collect(Collectors.groupingBy(KpiAlert::getAlertStatus));
        
        // Statistiques
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", recentAlerts.size());
        stats.put("pendingDecision", groupedByStatus.getOrDefault("PENDING_DECISION", Collections.emptyList()).size());
        stats.put("sentToPm", groupedByStatus.getOrDefault("SENT_TO_PM", Collections.emptyList()).size());
        stats.put("inProgress", groupedByStatus.getOrDefault("IN_PROGRESS", Collections.emptyList()).size());
        stats.put("resolved", groupedByStatus.getOrDefault("RESOLVED", Collections.emptyList()).size());
        stats.put("archived", groupedByStatus.getOrDefault("ARCHIVED", Collections.emptyList()).size());
        
        // Timeline des événements
        List<Map<String, Object>> timeline = buildTimeline(recentAlerts);
        
        Map<String, Object> result = new HashMap<>();
        result.put("alerts", recentAlerts);
        result.put("statistics", stats);
        result.put("timeline", timeline);
        result.put("period", days + " derniers jours");
        
        return result;
    }
    
    /**
     * Construire une timeline des événements
     */
    private List<Map<String, Object>> buildTimeline(List<KpiAlert> alerts) {
        List<Map<String, Object>> timeline = new ArrayList<>();
        
        for (KpiAlert alert : alerts) {
            // Événement de création
            Map<String, Object> created = new HashMap<>();
            created.put("type", "CREATED");
            created.put("alertId", alert.getId());
            created.put("kpiName", alert.getKpiName());
            created.put("message", alert.getMessage());
            created.put("timestamp", alert.getDetectedAt());
            created.put("status", "PENDING_DECISION");
            timeline.add(created);
            
            // Événements de l'historique
            if (alert.getActionHistory() != null) {
                for (KpiAlert.AlertAction action : alert.getActionHistory()) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("type", action.getActionType());
                    event.put("alertId", alert.getId());
                    event.put("kpiName", alert.getKpiName());
                    event.put("performedBy", action.getPerformedByName());
                    event.put("comment", action.getComment());
                    event.put("timestamp", action.getPerformedAt());
                    event.put("previousStatus", action.getPreviousStatus());
                    event.put("newStatus", action.getNewStatus());
                    timeline.add(event);
                }
            }
        }
        
        // Trier par date décroissante
        timeline.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeB.compareTo(timeA);
        });
        
        return timeline;
    }
    
    /**
     * Obtenir les notifications non lues
     */
    public List<KpiAlert> getUnreadNotifications(String userId) {
        List<KpiAlert> allAlerts = alertRepository.findByRecipientsContaining(userId);
        
        // Filtrer les alertes SENT_TO_PM (nouvelles pour le Chef de Projet)
        return allAlerts.stream()
            .filter(alert -> "SENT_TO_PM".equals(alert.getAlertStatus()))
            .sorted((a, b) -> b.getNotificationSentAt().compareTo(a.getNotificationSentAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * Marquer une notification comme lue
     */
    public void markAsRead(String alertId, String userId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            // Ajouter une action "VIEWED" à l'historique
            KpiAlert.AlertAction viewAction = new KpiAlert.AlertAction(
                "VIEWED",
                userId,
                "Chef de Projet",
                "Notification consultée"
            );
            alert.getActionHistory().add(viewAction);
            alertRepository.save(alert);
            
            System.out.println("👁️ Notification " + alertId + " marquée comme lue par " + userId);
        });
    }
    
    /**
     * Obtenir le compteur de notifications non lues
     */
    public int getUnreadCount(String userId) {
        return getUnreadNotifications(userId).size();
    }
}
