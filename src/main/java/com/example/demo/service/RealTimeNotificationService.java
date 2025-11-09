package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.dto.NotificationPreferencesDTO;
import com.example.demo.dto.NotificationStatsDTO;
import com.example.demo.model.Notification;
import com.example.demo.model.NotificationPreferences;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.NotificationPreferencesRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class RealTimeNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationPreferencesRepository preferencesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SmsService smsService;

    /**
     * Envoyer une notification immédiatement
     */
    public void sendNotification(Notification notification) {
        try {
            // 1. Envoyer via WebSocket
            messagingTemplate.convertAndSendToUser(
                notification.getUserId(),
                "/queue/notifications",
                notification
            );

            // 2. Vérifier les préférences utilisateur pour email/SMS
            NotificationPreferences prefs = preferencesRepository.findByUserId(notification.getUserId())
                .orElse(null);

            if (prefs != null) {
                // Envoyer par email si activé
                if (prefs.isEmailEnabled()) {
                    sendEmailNotification(notification);
                }

                // Envoyer par SMS si activé et urgent
                if (prefs.isSmsEnabled() && "high".equals(notification.getPriority())) {
                    sendSmsNotification(notification);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de la notification: " + e.getMessage(), e);
        }
    }

    private void sendEmailNotification(Notification notification) {
        // Logique d'envoi email existante...
    }

    private void sendSmsNotification(Notification notification) {
        // Logique d'envoi SMS existante...
    }

    /**
     * Créer et envoyer une notification
     */
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        Notification notification = new Notification();
        notification.setType(notificationDTO.getType());
        notification.setTitle(notificationDTO.getTitle());
        notification.setMessage(notificationDTO.getMessage());
        notification.setPriority(notificationDTO.getPriority());
        notification.setCategory(notificationDTO.getCategory());
        notification.setUserId(notificationDTO.getUserId());
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notification.setAcknowledged(false);
        notification.setSource(notificationDTO.getSource());
        notification.setMetadata(notificationDTO.getMetadata());
        notification.setExpiresAt(notificationDTO.getExpiresAt());

        Notification savedNotification = notificationRepository.save(notification);
        
        // Envoyer en temps réel via WebSocket
        sendRealTimeNotification(savedNotification);
        
        // Envoyer par email/SMS selon les préférences
        sendNotificationByPreferences(savedNotification);
        
        return convertToDTO(savedNotification);
    }

    /**
     * Récupérer les notifications d'un utilisateur
     */
    public List<NotificationDTO> getUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marquer une notification comme lue
     */
    public void markAsRead(String notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isPresent()) {
            Notification n = notification.get();
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }

    /**
     * Marquer une notification comme acquittée
     */
    public void markAsAcknowledged(String notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isPresent()) {
            Notification n = notification.get();
            n.setAcknowledged(true);
            n.setAcknowledgedAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }

    /**
     * Supprimer une notification
     */
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Supprimer toutes les notifications d'un utilisateur
     */
    public void deleteAllUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
        notificationRepository.deleteAll(notifications);
    }

    /**
     * Obtenir les statistiques de notifications
     */
    public NotificationStatsDTO getNotificationStats(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
        
        NotificationStatsDTO stats = new NotificationStatsDTO();
        stats.setTotal(notifications.size());
        stats.setUnread(notifications.stream().filter(n -> !n.isRead()).count());
        stats.setUnacknowledged(notifications.stream().filter(n -> !n.isAcknowledged()).count());
        
        // Statistiques par catégorie
        Map<String, Long> byCategory = new HashMap<>();
        notifications.stream()
                .collect(Collectors.groupingBy(Notification::getCategory, Collectors.counting()))
                .forEach(byCategory::put);
        stats.setByCategory(byCategory);
        
        // Statistiques par priorité
        Map<String, Long> byPriority = new HashMap<>();
        notifications.stream()
                .collect(Collectors.groupingBy(Notification::getPriority, Collectors.counting()))
                .forEach(byPriority::put);
        stats.setByPriority(byPriority);
        
        // Taux de lecture et d'acquittement
        if (stats.getTotal() > 0) {
            stats.setReadRate((double) (stats.getTotal() - stats.getUnread()) / stats.getTotal() * 100);
            stats.setAcknowledgmentRate((double) (stats.getTotal() - stats.getUnacknowledged()) / stats.getTotal() * 100);
        }
        
        return stats;
    }

    /**
     * Envoyer une notification en temps réel via WebSocket
     */
    private void sendRealTimeNotification(Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                notification.getUserId(),
                "/queue/notifications",
                convertToDTO(notification)
            );
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification WebSocket: " + e.getMessage());
        }
    }

    /**
     * Envoyer une notification selon les préférences utilisateur
     */
    private void sendNotificationByPreferences(Notification notification) {
        Optional<NotificationPreferences> preferences = preferencesRepository.findByUserId(notification.getUserId());
        if (preferences.isEmpty()) {
            return;
        }

        NotificationPreferences prefs = preferences.get();
        Optional<User> user = userRepository.findById(notification.getUserId());
        if (user.isEmpty()) {
            return;
        }

        User u = user.get();

        // Envoyer par email si activé
        if (prefs.isEmailEnabled() && u.getEmail() != null) {
            try {
                // TODO: Envoyer email de notification
                System.out.println("Email de notification pour " + u.getEmail() + ": " + notification.getTitle());
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi d'email: " + e.getMessage());
            }
        }

        // Envoyer par SMS si activé (placeholder)
        if (prefs.isSmsEnabled() && u.getPhoneNumber() != null) {
            try {
                // TODO: Implémenter l'envoi SMS
                System.out.println("SMS notification to " + u.getPhoneNumber() + ": " + notification.getMessage());
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de SMS: " + e.getMessage());
            }
        }
    }

    /**
     * Convertir une entité Notification en DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setPriority(notification.getPriority());
        dto.setCategory(notification.getCategory());
        dto.setUserId(notification.getUserId());
        dto.setTimestamp(notification.getTimestamp());
        dto.setRead(notification.isRead());
        dto.setAcknowledged(notification.isAcknowledged());
        dto.setReadAt(notification.getReadAt());
        dto.setAcknowledgedAt(notification.getAcknowledgedAt());
        dto.setMetadata(notification.getMetadata());
        dto.setSource(notification.getSource());
        dto.setExpiresAt(notification.getExpiresAt());
        return dto;
    }
}