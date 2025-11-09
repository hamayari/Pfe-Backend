package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationCleanupService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Nettoie les notifications expirées toutes les 24h
     */
    @Scheduled(cron = "0 0 0 * * *") // Tous les jours à minuit
    public void cleanupExpiredNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> expiredNotifications = notificationRepository.findByExpiresAtBeforeAndAcknowledgedFalse(now);
        
        // Archive ou supprime les notifications expirées
        for (Notification notification : expiredNotifications) {
            if (notification.isRead()) {
                notificationRepository.delete(notification);
            } else {
                notification.setExpiresAt(now.plusDays(7)); // Extension pour notifications non lues
                notificationRepository.save(notification);
            }
        }
    }

    /**
     * Archive les anciennes notifications lues et acquittées après 30 jours
     */
    @Scheduled(cron = "0 0 1 * * *") // Tous les jours à 1h du matin
    public void archiveOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Notification> oldNotifications = notificationRepository.findByTimestampBeforeAndReadTrueAndAcknowledgedTrue(thirtyDaysAgo);
        
        // Déplacer vers une collection d'archive ou supprimer selon la politique
        notificationRepository.deleteAll(oldNotifications);
    }
}