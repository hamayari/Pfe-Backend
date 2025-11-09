package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class NotificationPriorityService {

    @Autowired
    private NotificationRepository notificationRepository;

    private static final Map<String, Integer> PRIORITY_WEIGHTS = new HashMap<>();
    static {
        PRIORITY_WEIGHTS.put("low", 1);
        PRIORITY_WEIGHTS.put("medium", 2);
        PRIORITY_WEIGHTS.put("high", 3);
        PRIORITY_WEIGHTS.put("critical", 4);
    }

    /**
     * Ajuster dynamiquement la priorité des notifications
     */
    public void adjustNotificationPriority(Notification notification) {
        // Augmente la priorité si plusieurs notifications similaires non lues
        List<Notification> similarNotifications = notificationRepository
            .findByUserIdAndCategoryAndReadFalse(notification.getUserId(), notification.getCategory());
        
        if (similarNotifications.size() >= 3) {
            escalatePriority(notification);
        }

        // Augmente la priorité des notifications anciennes non lues
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);
        if (!notification.isRead() && notification.getTimestamp().isBefore(threshold)) {
            escalatePriority(notification);
        }
    }

    /**
     * Escalade la priorité d'une notification
     */
    private void escalatePriority(Notification notification) {
        String currentPriority = notification.getPriority();
        int currentWeight = PRIORITY_WEIGHTS.getOrDefault(currentPriority, 0);
        
        if (currentWeight < PRIORITY_WEIGHTS.get("critical")) {
            // Monte d'un niveau
            String[] priorities = {"low", "medium", "high", "critical"};
            notification.setPriority(priorities[currentWeight]);
            notificationRepository.save(notification);
        }
    }

    /**
     * Vérifie si une notification est urgente
     */
    public boolean isUrgent(Notification notification) {
        return PRIORITY_WEIGHTS.getOrDefault(notification.getPriority(), 0) >= PRIORITY_WEIGHTS.get("high");
    }
}