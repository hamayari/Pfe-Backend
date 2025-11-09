package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class NotificationBatchService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private RealTimeNotificationService realTimeNotificationService;

    // Map pour suivre les lots de notifications par utilisateur
    private final Map<String, BatchStatus> userBatchStatus = new ConcurrentHashMap<>();

    // Taille du lot pour le traitement par lots
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000; // 5 secondes

    /**
     * Traiter les notifications en lots
     */
    @Async
    public CompletableFuture<Void> processPendingNotifications() {
        int page = 0;
        Page<Notification> notifications;
        
        do {
            notifications = notificationRepository.findByStatusOrderByTimestampAsc("PENDING",
                PageRequest.of(page, BATCH_SIZE));
            
            List<Notification> batch = notifications.getContent();
            
            // Grouper par utilisateur
            batch.stream()
                .collect(Collectors.groupingBy(Notification::getUserId))
                .forEach(this::processBatchForUser);
            
            page++;
        } while (notifications.hasNext());
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Traiter un lot de notifications pour un utilisateur
     */
    private void processBatchForUser(String userId, List<Notification> notifications) {
        BatchStatus status = userBatchStatus.computeIfAbsent(userId, 
            k -> new BatchStatus(notifications.size()));

        notifications.forEach(notification -> {
            if (status.canProcess()) {
                try {
                    processNotification(notification);
                    status.incrementProcessed();
                } catch (Exception e) {
                    handleProcessingError(notification, e);
                }
            } else {
                // Reprogrammer pour plus tard
                notification.setNextProcessingTime(LocalDateTime.now().plusMinutes(5));
                notificationRepository.save(notification);
            }
        });
    }

    /**
     * Traiter une notification individuelle
     */
    private void processNotification(Notification notification) {
        realTimeNotificationService.sendNotification(notification);
        notification.setStatus("SENT");
        notification.setProcessedTime(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Gérer les erreurs de traitement
     */
    private void handleProcessingError(Notification notification, Exception e) {
        int retryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;
        
        if (retryCount < MAX_RETRIES) {
            notification.setRetryCount(retryCount + 1);
            notification.setNextProcessingTime(
                LocalDateTime.now().plusSeconds(RETRY_DELAY_MS * (retryCount + 1) / 1000)
            );
            notification.setLastError(e.getMessage());
            notificationRepository.save(notification);
        } else {
            notification.setStatus("FAILED");
            notification.setLastError(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    /**
     * Nettoyer périodiquement le statut des lots
     */
    @Scheduled(fixedRate = 3600000) // 1 heure
    public void cleanupBatchStatus() {
        userBatchStatus.entrySet().removeIf(entry -> 
            entry.getValue().getLastProcessingTime()
                .isBefore(LocalDateTime.now().minusHours(1))
        );
    }

    /**
     * Classe interne pour suivre le statut des lots par utilisateur
     */
    private static class BatchStatus {
        private int processedCount;
        private LocalDateTime lastProcessingTime;
        private static final int MAX_BATCH_PER_MINUTE = 60;
        private int remainingRetries;

        public BatchStatus(int batchSize) {
            this.processedCount = 0;
            this.lastProcessingTime = LocalDateTime.now();
            this.remainingRetries = Math.max(1, batchSize / MAX_BATCH_PER_MINUTE);
        }

        public boolean canProcess() {
            LocalDateTime now = LocalDateTime.now();
            if (lastProcessingTime.plusMinutes(1).isBefore(now)) {
                // Réinitialiser le compteur après 1 minute
                processedCount = 0;
                lastProcessingTime = now;
                remainingRetries = Math.max(0, remainingRetries - 1);
            }
            return processedCount < MAX_BATCH_PER_MINUTE && remainingRetries > 0;
        }

        public void incrementProcessed() {
            processedCount++;
            lastProcessingTime = LocalDateTime.now();
        }

        public LocalDateTime getLastProcessingTime() {
            return lastProcessingTime;
        }
    }
}