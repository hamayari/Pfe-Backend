package com.example.demo.service;

import com.example.demo.model.KpiAlert;
import com.example.demo.model.User;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service de gestion du cycle de vie des alertes KPI
 * Gère la résolution, l'archivage et la traçabilité complète
 */
@Service
public class KpiAlertManagementService {
    
    @Autowired
    private KpiAlertRepository alertRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Créer une nouvelle alerte avec traçabilité
     */
    public KpiAlert createAlert(KpiAlert alert, String createdBy) {
        alert.setDetectedAt(LocalDateTime.now());
        // Ne pas écraser le statut s'il est déjà défini
        if (alert.getAlertStatus() == null || alert.getAlertStatus().isEmpty()) {
            alert.setAlertStatus("PENDING_DECISION");
        }
        alert.setNotificationSent(false);
        
        // Ajouter l'action de création à l'historique
        User creator = userRepository.findById(createdBy).orElse(null);
        String creatorName = creator != null ? creator.getUsername() : "System";
        
        KpiAlert.AlertAction createAction = new KpiAlert.AlertAction(
            "CREATED",
            createdBy,
            creatorName,
            "Alerte créée automatiquement par le système"
        );
        createAction.setNewStatus("NEW");
        
        alert.getActionHistory().add(createAction);
        
        // Sauvegarder
        KpiAlert saved = alertRepository.save(alert);
        
        System.out.println("✅ Alerte KPI créée: " + saved.getId());
        
        return saved;
    }
    
    /**
     * Envoyer une alerte au Chef de Projet (depuis le Décideur)
     */
    public KpiAlert sendToProjectManager(String alertId, String deciderId) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                // Vérifier que l'alerte est bien en attente
                if (!"PENDING_DECISION".equals(alert.getAlertStatus())) {
                    throw new RuntimeException("Seules les alertes en attente peuvent être envoyées");
                }
                
                String previousStatus = alert.getAlertStatus();
                alert.setAlertStatus("SENT_TO_PM");
                alert.setNotificationSent(true);
                alert.setNotificationSentAt(LocalDateTime.now());
                
                // Ajouter à l'historique
                User decideur = userRepository.findById(deciderId).orElse(null);
                String decideurName = decideur != null ? decideur.getUsername() : "Décideur";
                
                KpiAlert.AlertAction action = new KpiAlert.AlertAction(
                    "SENT_TO_PM",
                    deciderId,
                    decideurName,
                    "Alerte envoyée au Chef de Projet"
                );
                action.setPreviousStatus(previousStatus);
                action.setNewStatus("SENT_TO_PM");
                
                alert.getActionHistory().add(action);
                
                // Sauvegarder et notifier
                KpiAlert updated = alertRepository.save(alert);
                notifyAlertUpdate(updated, "SENT_TO_PM");
                
                System.out.println("📨 Alerte " + alertId + " envoyée au Chef de Projet par " + decideurName);
                
                return updated;
            })
            .orElseThrow(() -> new RuntimeException("Alerte non trouvée: " + alertId));
    }
    
    /**
     * Marquer une alerte comme "En cours de traitement"
     */
    public KpiAlert markAsInProgress(String alertId, String userId, String comment) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                String previousStatus = alert.getAlertStatus();
                alert.setAlertStatus("IN_PROGRESS");
                
                // Ajouter à l'historique
                User user = userRepository.findById(userId).orElse(null);
                String userName = user != null ? user.getUsername() : "Unknown";
                
                KpiAlert.AlertAction action = new KpiAlert.AlertAction(
                    "IN_PROGRESS",
                    userId,
                    userName,
                    comment != null ? comment : "Prise en charge de l'alerte"
                );
                action.setPreviousStatus(previousStatus);
                action.setNewStatus("IN_PROGRESS");
                
                alert.getActionHistory().add(action);
                
                // Sauvegarder et notifier
                KpiAlert updated = alertRepository.save(alert);
                notifyAlertUpdate(updated, "IN_PROGRESS");
                
                System.out.println("📝 Alerte " + alertId + " marquée en cours par " + userName);
                
                return updated;
            })
            .orElseThrow(() -> new RuntimeException("Alerte non trouvée: " + alertId));
    }
    
    /**
     * Résoudre une alerte avec commentaire et actions prises
     */
    public KpiAlert resolveAlert(String alertId, String userId, String resolutionComment, String actionsTaken) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                String previousStatus = alert.getAlertStatus();
                alert.setAlertStatus("RESOLVED");
                alert.setResolvedAt(LocalDateTime.now());
                alert.setResolvedBy(userId);
                alert.setResolutionComment(resolutionComment);
                alert.setActionsTaken(actionsTaken);
                
                // Ajouter à l'historique
                User user = userRepository.findById(userId).orElse(null);
                String userName = user != null ? user.getUsername() : "Unknown";
                alert.setResolvedByName(userName);
                
                KpiAlert.AlertAction action = new KpiAlert.AlertAction(
                    "RESOLVED",
                    userId,
                    userName,
                    resolutionComment
                );
                action.setPreviousStatus(previousStatus);
                action.setNewStatus("RESOLVED");
                
                alert.getActionHistory().add(action);
                
                // Sauvegarder et notifier
                KpiAlert updated = alertRepository.save(alert);
                notifyAlertUpdate(updated, "RESOLVED");
                
                System.out.println("✅ Alerte " + alertId + " résolue par " + userName);
                
                return updated;
            })
            .orElseThrow(() -> new RuntimeException("Alerte non trouvée: " + alertId));
    }
    
    /**
     * Marquer une alerte comme "Informé" (Chef de Projet)
     * L'alerte passe de SENT_TO_PM à ACKNOWLEDGED
     */
    public KpiAlert acknowledgeAlert(String alertId, String userId) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                String previousStatus = alert.getAlertStatus();
                alert.setAlertStatus("ACKNOWLEDGED");
                alert.setAcknowledgedAt(LocalDateTime.now());
                
                // Ajouter à l'historique
                User user = userRepository.findById(userId).orElse(null);
                String userName = user != null ? user.getUsername() : "Chef de Projet";
                
                KpiAlert.AlertAction action = new KpiAlert.AlertAction(
                    "ACKNOWLEDGED",
                    userId,
                    userName,
                    "Alerte prise en compte par le Chef de Projet"
                );
                action.setPreviousStatus(previousStatus);
                action.setNewStatus("ACKNOWLEDGED");
                
                alert.getActionHistory().add(action);
                
                // Sauvegarder et notifier
                KpiAlert updated = alertRepository.save(alert);
                notifyAlertUpdate(updated, "ACKNOWLEDGED");
                
                System.out.println("✅ Alerte " + alertId + " marquée comme informé par " + userName);
                
                return updated;
            })
            .orElseThrow(() -> new RuntimeException("Alerte non trouvée: " + alertId));
    }
    
    /**
     * Archiver une alerte résolue
     */
    public KpiAlert archiveAlert(String alertId, String userId) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                if (!"RESOLVED".equals(alert.getAlertStatus())) {
                    throw new RuntimeException("Seules les alertes résolues peuvent être archivées");
                }
                
                String previousStatus = alert.getAlertStatus();
                alert.setAlertStatus("ARCHIVED");
                alert.setArchivedAt(LocalDateTime.now());
                alert.setArchivedBy(userId);
                
                // Ajouter à l'historique
                User user = userRepository.findById(userId).orElse(null);
                String userName = user != null ? user.getUsername() : "Unknown";
                
                KpiAlert.AlertAction action = new KpiAlert.AlertAction(
                    "ARCHIVED",
                    userId,
                    userName,
                    "Alerte archivée"
                );
                action.setPreviousStatus(previousStatus);
                action.setNewStatus("ARCHIVED");
                
                alert.getActionHistory().add(action);
                
                // Sauvegarder et notifier
                KpiAlert updated = alertRepository.save(alert);
                notifyAlertUpdate(updated, "ARCHIVED");
                
                System.out.println("📦 Alerte " + alertId + " archivée par " + userName);
                
                return updated;
            })
            .orElseThrow(() -> new RuntimeException("Alerte non trouvée: " + alertId));
    }
    
    /**
     * Ajouter un commentaire à une alerte
     */
    public KpiAlert addComment(String alertId, String userId, String comment) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                User user = userRepository.findById(userId).orElse(null);
                String userName = user != null ? user.getUsername() : "Unknown";
                
                KpiAlert.AlertAction action = new KpiAlert.AlertAction(
                    "COMMENTED",
                    userId,
                    userName,
                    comment
                );
                
                alert.getActionHistory().add(action);
                
                KpiAlert updated = alertRepository.save(alert);
                
                System.out.println("💬 Commentaire ajouté à l'alerte " + alertId + " par " + userName);
                
                return updated;
            })
            .orElseThrow(() -> new RuntimeException("Alerte non trouvée: " + alertId));
    }
    
    /**
     * Obtenir TOUTES les alertes PENDING_DECISION (pour le Décideur)
     * ✅ FILTRE: Retourne UNIQUEMENT les alertes de factures PENDING
     */
    public List<KpiAlert> getAllPendingDecisionAlerts() {
        List<KpiAlert> allAlerts = alertRepository.findByAlertStatus("PENDING_DECISION");
        
        // ✅ FILTRER pour garder UNIQUEMENT les alertes de factures PENDING
        List<KpiAlert> filteredAlerts = allAlerts.stream()
            .filter(alert -> "FACTURE_PENDING".equals(alert.getKpiName()))
            .collect(java.util.stream.Collectors.toList());
        
        System.out.println("✅ Récupération de " + filteredAlerts.size() + " alertes de factures PENDING (sur " + allAlerts.size() + " alertes totales)");
        
        return filteredAlerts;
    }
    
    /**
     * Obtenir les alertes actives (PENDING_DECISION + SENT_TO_PM + IN_PROGRESS) pour un utilisateur
     */
    public List<KpiAlert> getActiveAlerts(String userId) {
        try {
            List<String> activeStatuses = Arrays.asList("PENDING_DECISION", "SENT_TO_PM", "IN_PROGRESS", "NEW");
            List<KpiAlert> alerts = alertRepository.findByRecipientsContainingAndAlertStatusIn(userId, activeStatuses);
            System.out.println("✅ Récupération de " + alerts.size() + " alertes pour userId: " + userId);
            return alerts;
        } catch (Exception e) {
            System.err.println("❌ Erreur getActiveAlerts: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtenir les alertes résolues récemment (7 derniers jours)
     */
    public List<KpiAlert> getRecentlyResolvedAlerts(String userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<KpiAlert> allResolved = alertRepository.findByAlertStatusAndResolvedAtAfter("RESOLVED", sevenDaysAgo);
        
        // Filtrer par destinataire
        return allResolved.stream()
            .filter(alert -> alert.getRecipients() != null && alert.getRecipients().contains(userId))
            .toList();
    }
    
    /**
     * Obtenir les alertes archivées
     */
    public List<KpiAlert> getArchivedAlerts(String userId) {
        List<KpiAlert> allArchived = alertRepository.findByAlertStatusOrderByArchivedAtDesc("ARCHIVED");
        
        // Filtrer par destinataire
        return allArchived.stream()
            .filter(alert -> alert.getRecipients() != null && alert.getRecipients().contains(userId))
            .toList();
    }
    
    /**
     * Obtenir l'historique complet d'une alerte
     */
    public List<KpiAlert.AlertAction> getAlertHistory(String alertId) {
        return alertRepository.findById(alertId)
            .map(KpiAlert::getActionHistory)
            .orElse(new ArrayList<>());
    }
    
    /**
     * Obtenir les statistiques des alertes
     */
    public Map<String, Object> getAlertStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<KpiAlert> userAlerts = alertRepository.findByRecipientsContaining(userId);
        
        long newCount = userAlerts.stream().filter(a -> "NEW".equals(a.getAlertStatus())).count();
        long inProgressCount = userAlerts.stream().filter(a -> "IN_PROGRESS".equals(a.getAlertStatus())).count();
        long resolvedCount = userAlerts.stream().filter(a -> "RESOLVED".equals(a.getAlertStatus())).count();
        long archivedCount = userAlerts.stream().filter(a -> "ARCHIVED".equals(a.getAlertStatus())).count();
        
        stats.put("new", newCount);
        stats.put("inProgress", inProgressCount);
        stats.put("resolved", resolvedCount);
        stats.put("archived", archivedCount);
        stats.put("total", userAlerts.size());
        stats.put("active", newCount + inProgressCount);
        
        return stats;
    }
    
    /**
     * Notifier les utilisateurs d'une mise à jour d'alerte
     */
    private void notifyAlertUpdate(KpiAlert alert, String action) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("alertId", alert.getId());
        notification.put("action", action);
        notification.put("status", alert.getAlertStatus());
        notification.put("kpiName", alert.getKpiName());
        notification.put("timestamp", LocalDateTime.now().toString());
        
        // Envoyer au topic général
        messagingTemplate.convertAndSend("/topic/alert-updates", notification);
        
        // Envoyer à chaque destinataire
        if (alert.getRecipients() != null) {
            for (String recipientId : alert.getRecipients()) {
                User recipient = userRepository.findById(recipientId).orElse(null);
                if (recipient != null) {
                    messagingTemplate.convertAndSendToUser(
                        recipient.getUsername(),
                        "/queue/alert-updates",
                        notification
                    );
                }
            }
        }
    }
    
    /**
     * Archiver automatiquement les alertes résolues depuis plus de 30 jours
     */
    public int autoArchiveOldResolvedAlerts() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<KpiAlert> oldResolved = alertRepository.findByAlertStatusAndResolvedAtAfter("RESOLVED", thirtyDaysAgo);
        
        int archived = 0;
        for (KpiAlert alert : oldResolved) {
            try {
                archiveAlert(alert.getId(), "system");
                archived++;
            } catch (Exception e) {
                System.err.println("Erreur archivage auto: " + e.getMessage());
            }
        }
        
        System.out.println("📦 " + archived + " alertes archivées automatiquement");
        return archived;
    }
}
