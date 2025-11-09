package com.example.demo.scheduler;

import com.example.demo.model.AlertConfiguration;
import com.example.demo.model.Convention;
import com.example.demo.model.Notification;
import com.example.demo.model.NotificationLog;
import com.example.demo.repository.AlertConfigurationRepository;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduler pour envoyer des alertes automatiques sur les conventions
 * Exécuté tous les jours à 9h du matin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConventionAlertScheduler {

    private final ConventionRepository conventionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final AlertConfigurationRepository alertConfigurationRepository;

    /**
     * Envoie des alertes pour les conventions proches de l'échéance
     * Exécuté tous les jours à 9h00
     */
    @Scheduled(cron = "0 0 9 * * ?") // Tous les jours à 9h00
    public void sendExpirationAlerts() {
        log.info("🔔 Début de l'envoi des alertes d'échéance des conventions");
        
        // Récupérer la configuration active
        AlertConfiguration config = alertConfigurationRepository.findFirstByActiveTrue()
                .orElseGet(() -> {
                    log.warn("⚠️ Aucune configuration d'alerte trouvée, utilisation de la configuration par défaut");
                    return AlertConfiguration.getDefaultConfiguration();
                });
        
        LocalDate today = LocalDate.now();
        List<Convention> activeConventions = conventionRepository.findByStatus("ACTIVE");
        
        int alertsSent = 0;
        
        for (Convention convention : activeConventions) {
            if (convention.getEndDate() == null) {
                continue;
            }
            
            long daysUntilExpiration = ChronoUnit.DAYS.between(today, convention.getEndDate());
            
            // Vérifier si une alerte doit être envoyée
            if (shouldSendAlert(daysUntilExpiration, config)) {
                sendAlert(convention, daysUntilExpiration, config);
                alertsSent++;
            }
        }
        
        log.info("✅ Envoi des alertes terminé : {} alertes envoyées", alertsSent);
    }

    /**
     * Détermine si une alerte doit être envoyée selon le nombre de jours restants
     */
    private boolean shouldSendAlert(long daysUntilExpiration, AlertConfiguration config) {
        List<Long> activeThresholds = new ArrayList<>();
        
        if (Boolean.TRUE.equals(config.getAlert30DaysEnabled())) {
            activeThresholds.add((long) config.getAlertThreshold30Days());
        }
        if (Boolean.TRUE.equals(config.getAlert15DaysEnabled())) {
            activeThresholds.add((long) config.getAlertThreshold15Days());
        }
        if (Boolean.TRUE.equals(config.getAlert7DaysEnabled())) {
            activeThresholds.add((long) config.getAlertThreshold7Days());
        }
        if (Boolean.TRUE.equals(config.getAlert1DayEnabled())) {
            activeThresholds.add((long) config.getAlertThreshold1Day());
        }
        if (Boolean.TRUE.equals(config.getAlertSameDayEnabled())) {
            activeThresholds.add(0L);
        }
        
        return activeThresholds.contains(daysUntilExpiration);
    }

    /**
     * Envoie une alerte pour une convention
     */
    private void sendAlert(Convention convention, long daysUntilExpiration, AlertConfiguration config) {
        String message = buildAlertMessage(convention, daysUntilExpiration);
        String priority = getAlertPriority(daysUntilExpiration);
        
        try {
            // Créer la notification
            Notification notification = new Notification();
            notification.setUserId(convention.getCreatedBy());
            notification.setType("CONVENTION_EXPIRATION");
            notification.setTitle("⚠️ Échéance de Convention");
            notification.setMessage(message);
            notification.setPriority(priority);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRelatedEntityId(convention.getId());
            notification.setRelatedEntityType("CONVENTION");
            
            // Sauvegarder
            notificationRepository.save(notification);
            
            // Envoyer via WebSocket en temps réel si activé
            if (Boolean.TRUE.equals(config.getWebsocketNotificationsEnabled())) {
                try {
                    // Créer un NotificationLog pour le WebSocket
                    NotificationLog notificationLog = new NotificationLog();
                    notificationLog.setType("SYSTEM");
                    notificationLog.setChannel("WEBSOCKET");
                    notificationLog.setRecipientId(convention.getCreatedBy());
                    notificationLog.setMessage(message);
                    notificationLog.setSubject("⚠️ Échéance de Convention");
                    notificationLog.setStatus("SENT");
                    notificationLog.setSentAt(LocalDateTime.now());
                    notificationLog.setConventionId(convention.getId());
                    
                    notificationService.sendNotificationToUser(notificationLog);
                    log.info("🔔 Notification WebSocket envoyée pour la convention {}", convention.getReference());
                } catch (Exception e) {
                    log.warn("⚠️ Impossible d'envoyer la notification en temps réel: {}", e.getMessage());
                }
            }
            
            log.info("📧 Alerte envoyée pour la convention {} ({} jours restants)", 
                    convention.getReference(), daysUntilExpiration);
                    
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'alerte pour la convention {}: {}", 
                    convention.getReference(), e.getMessage());
        }
    }

    /**
     * Construit le message d'alerte selon le nombre de jours restants
     */
    private String buildAlertMessage(Convention convention, long daysUntilExpiration) {
        String reference = convention.getReference();
        String title = convention.getTitle() != null ? convention.getTitle() : "Sans titre";
        
        if (daysUntilExpiration == 0) {
            return String.format("🚨 La convention %s (%s) expire AUJOURD'HUI ! Action immédiate requise.", 
                    reference, title);
        } else if (daysUntilExpiration == 1) {
            return String.format("⚠️ La convention %s (%s) expire DEMAIN ! Veuillez prendre les mesures nécessaires.", 
                    reference, title);
        } else if (daysUntilExpiration <= 7) {
            return String.format("⚠️ La convention %s (%s) expire dans %d jours. Préparez le renouvellement si nécessaire.", 
                    reference, title, daysUntilExpiration);
        } else if (daysUntilExpiration <= 15) {
            return String.format("📅 La convention %s (%s) expire dans %d jours. Pensez à planifier le renouvellement.", 
                    reference, title, daysUntilExpiration);
        } else {
            return String.format("📅 La convention %s (%s) expire dans %d jours.", 
                    reference, title, daysUntilExpiration);
        }
    }

    /**
     * Détermine la priorité de l'alerte selon le nombre de jours restants
     */
    private String getAlertPriority(long daysUntilExpiration) {
        if (daysUntilExpiration <= 1) {
            return "URGENT";
        } else if (daysUntilExpiration <= 7) {
            return "HIGH";
        } else if (daysUntilExpiration <= 15) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Méthode manuelle pour forcer l'envoi des alertes (utile pour les tests)
     */
    public void forceAlerts() {
        log.info("🔧 Envoi manuel forcé des alertes");
        sendExpirationAlerts();
    }
}
