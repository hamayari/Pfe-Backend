package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service dédié à la gestion des notifications in-app
 * Gère le compteur de notifications par utilisateur avec WebSocket temps réel
 */
@Service
public class InAppNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(InAppNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * 1️⃣ Créer une nouvelle notification pour un utilisateur
     * Incrémente automatiquement le compteur via WebSocket
     */
    public Notification createNotification(String userId, String type, String title, 
                                          String message, String priority, String category) {
        logger.info("📝 Création notification pour userId={}, type={}", userId, type);
        
        Notification notification = new Notification(type, title, message, priority, category, userId);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notification.setAcknowledged(false);
        notification.setStatus("SENT");
        
        // Sauvegarder en base
        Notification saved = notificationRepository.save(notification);
        logger.info("✅ Notification créée: id={}", saved.getId());
        
        // Envoyer via WebSocket au canal spécifique de l'utilisateur
        sendNotificationToUser(userId, saved);
        
        // Envoyer mise à jour du compteur
        sendCounterUpdate(userId);
        
        return saved;
    }
    
    /**
     * 2️⃣ Marquer une notification comme lue
     * Décrémente le compteur via WebSocket
     */
    public boolean markAsRead(String notificationId, String userId) {
        logger.info("📖 Marquer comme lue: notificationId={}, userId={}", notificationId, userId);
        
        return notificationRepository.findById(notificationId)
            .map(notification -> {
                // Vérifier que la notification appartient bien à cet utilisateur
                if (!notification.getUserId().equals(userId)) {
                    logger.warn("⚠️ Tentative de lecture notification d'un autre user");
                    return false;
                }
                
                // Si déjà lue, ne rien faire
                if (notification.isRead()) {
                    logger.info("ℹ️ Notification déjà lue");
                    return true;
                }
                
                // Marquer comme lue
                notification.setRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
                
                logger.info("✅ Notification marquée comme lue");
                
                // Envoyer mise à jour du compteur
                sendCounterUpdate(userId);
                
                return true;
            })
            .orElse(false);
    }
    
    /**
     * 3️⃣ Marquer toutes les notifications comme lues
     * Réinitialise le compteur à 0
     */
    public int markAllAsRead(String userId) {
        logger.info("📚 Marquer toutes comme lues pour userId={}", userId);
        
        List<Notification> unreadNotifications = notificationRepository
            .findByUserIdAndReadFalseAndDeletedFalseOrderByTimestampDesc(userId);
        
        if (unreadNotifications.isEmpty()) {
            logger.info("ℹ️ Aucune notification non lue");
            return 0;
        }
        
        // Marquer toutes comme lues
        unreadNotifications.forEach(n -> {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(unreadNotifications);
        
        int count = unreadNotifications.size();
        logger.info("✅ {} notifications marquées comme lues", count);
        
        // Envoyer mise à jour du compteur (devrait être 0)
        sendCounterUpdate(userId);
        
        return count;
    }
    
    /**
     * 4️⃣ Marquer plusieurs notifications comme lues (bulk)
     */
    public int markAsReadBulk(String userId, List<String> notificationIds) {
        logger.info("📚 Marquer {} notifications comme lues pour userId={}", 
                   notificationIds.size(), userId);
        
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        
        // Filtrer uniquement les notifications de cet utilisateur
        List<Notification> userNotifications = notifications.stream()
            .filter(n -> n.getUserId().equals(userId))
            .filter(n -> !n.isRead()) // Uniquement les non lues
            .toList();
        
        if (userNotifications.isEmpty()) {
            return 0;
        }
        
        // Marquer comme lues
        userNotifications.forEach(n -> {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(userNotifications);
        
        int count = userNotifications.size();
        logger.info("✅ {} notifications marquées comme lues", count);
        
        // Envoyer mise à jour du compteur
        sendCounterUpdate(userId);
        
        return count;
    }
    
    /**
     * 5️⃣ Supprimer une notification (soft delete)
     */
    public boolean deleteNotification(String notificationId, String userId) {
        logger.info("🗑️ Suppression notification: id={}, userId={}", notificationId, userId);
        
        return notificationRepository.findById(notificationId)
            .map(notification -> {
                // Vérifier que la notification appartient à cet utilisateur
                if (!notification.getUserId().equals(userId)) {
                    logger.warn("⚠️ Tentative de suppression notification d'un autre user");
                    return false;
                }
                
                // Soft delete
                notification.setDeleted(true);
                notification.setDeletedAt(LocalDateTime.now());
                notificationRepository.save(notification);
                
                logger.info("✅ Notification marquée comme supprimée (soft delete)");
                
                // Si la notification n'était pas lue, mettre à jour le compteur
                if (!notification.isRead()) {
                    sendCounterUpdate(userId);
                }
                
                return true;
            })
            .orElse(false);
    }
    
    /**
     * 6️⃣ Obtenir le compteur de notifications non lues (exclut les supprimées)
     */
    public long getUnreadCount(String userId) {
        long count = notificationRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
        logger.debug("📊 Compteur non lues pour userId={}: {}", userId, count);
        return count;
    }
    
    /**
     * 7️⃣ Obtenir toutes les notifications d'un utilisateur (exclut les supprimées)
     */
    public List<Notification> getAllNotifications(String userId) {
        return notificationRepository.findByUserIdAndDeletedFalseOrderByTimestampDesc(userId);
    }
    
    /**
     * 8️⃣ Obtenir uniquement les notifications non lues (exclut les supprimées)
     */
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadFalseAndDeletedFalseOrderByTimestampDesc(userId);
    }
    
    /**
     * 9️⃣ Obtenir les notifications par catégorie
     */
    public List<Notification> getNotificationsByCategory(String userId, String category) {
        return notificationRepository.findByUserIdAndCategory(userId, category);
    }
    
    /**
     * 🔟 Obtenir les notifications par priorité
     */
    public List<Notification> getNotificationsByPriority(String userId, String priority) {
        return notificationRepository.findByUserIdAndPriority(userId, priority);
    }
    
    // ============================================
    // 🔔 MÉTHODES WEBSOCKET TEMPS RÉEL
    // ============================================
    
    /**
     * Envoyer une notification à un utilisateur spécifique via WebSocket
     * Canal: /topic/notifications/{userId}
     */
    private void sendNotificationToUser(String userId, Notification notification) {
        try {
            String destination = "/topic/notifications/" + userId;
            messagingTemplate.convertAndSend(destination, notification);
            logger.info("📤 Notification envoyée via WebSocket: {}", destination);
        } catch (Exception e) {
            logger.error("❌ Erreur envoi WebSocket notification: {}", e.getMessage());
        }
    }
    
    /**
     * Envoyer mise à jour du compteur via WebSocket
     * Canal: /topic/notifications/{userId}/count
     */
    private void sendCounterUpdate(String userId) {
        try {
            long count = getUnreadCount(userId);
            
            Map<String, Object> counterUpdate = new HashMap<>();
            counterUpdate.put("unreadCount", count);
            counterUpdate.put("timestamp", LocalDateTime.now().toString());
            
            String destination = "/topic/notifications/" + userId + "/count";
            messagingTemplate.convertAndSend(destination, counterUpdate);
            
            logger.info("📤 Compteur envoyé via WebSocket: {} -> count={}", destination, count);
        } catch (Exception e) {
            logger.error("❌ Erreur envoi WebSocket compteur: {}", e.getMessage());
        }
    }
    
    /**
     * Envoyer une notification broadcast à tous les utilisateurs (admin uniquement)
     */
    public void sendBroadcastNotification(String type, String title, String message, 
                                         String priority, String category) {
        logger.info("📢 Broadcast notification: type={}", type);
        
        // Cette méthode peut être utilisée pour des notifications système globales
        Map<String, Object> broadcast = new HashMap<>();
        broadcast.put("type", type);
        broadcast.put("title", title);
        broadcast.put("message", message);
        broadcast.put("priority", priority);
        broadcast.put("category", category);
        broadcast.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSend("/topic/notifications/broadcast", broadcast);
    }
    
    /**
     * Récupérer les alertes déléguées pour le Chef de Projet
     * Filtre les notifications de type ALERT_DELEGATED
     */
    public List<Notification> getDelegatedAlerts(String userId) {
        logger.info("🔍 Récupération alertes déléguées pour userId={}", userId);
        
        // Récupérer toutes les notifications de type ALERT_DELEGATED
        List<Notification> delegatedAlerts = notificationRepository
            .findByUserIdAndTypeAndDeletedFalseOrderByTimestampDesc(userId, "ALERT_DELEGATED");
        
        logger.info("✅ {} alertes déléguées trouvées", delegatedAlerts.size());
        return delegatedAlerts;
    }
    
    /**
     * Compter toutes les notifications
     */
    public long countAllNotifications() {
        return notificationRepository.count();
    }
    
    /**
     * Supprimer les anciennes notifications lues
     */
    public int deleteOldReadNotifications(LocalDateTime cutoffDate) {
        logger.info("🧹 Suppression des notifications lues avant {}", cutoffDate);
        
        // Trouver les notifications à supprimer
        List<Notification> oldNotifications = notificationRepository
            .findByTimestampBeforeAndReadTrueAndDeletedFalse(cutoffDate);
        
        int count = oldNotifications.size();
        
        if (count > 0) {
            // Soft delete
            oldNotifications.forEach(n -> {
                n.setDeleted(true);
                n.setDeletedAt(LocalDateTime.now());
            });
            notificationRepository.saveAll(oldNotifications);
            
            logger.info("✅ {} notifications supprimées", count);
        } else {
            logger.info("ℹ️ Aucune notification à supprimer");
        }
        
        return count;
    }
    
    /**
     * Méthodes de compatibilité pour les anciens appels
     */
    public List<Notification> getUserNotifications(String userId) {
        return getAllNotifications(userId);
    }
    
    public int getUnreadCountInt(String userId) {
        return (int) getUnreadCount(userId);
    }
    
    public void deleteNotification(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setDeleted(true);
            notification.setDeletedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }
}
