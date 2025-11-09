package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    // Méthodes avec filtre deleted=false (soft delete)
    List<Notification> findByUserIdAndDeletedFalseOrderByTimestampDesc(String userId);
    
    List<Notification> findByUserIdAndReadFalseAndDeletedFalseOrderByTimestampDesc(String userId);
    
    List<Notification> findByUserIdAndAcknowledgedFalseAndDeletedFalseOrderByTimestampDesc(String userId);
    
    List<Notification> findByUserIdAndCategoryAndReadFalseAndDeletedFalse(String userId, String category);
    
    // Méthodes legacy (sans filtre deleted) - à déprécier
    List<Notification> findByUserIdOrderByTimestampDesc(String userId);
    
    List<Notification> findByUserIdAndReadFalseOrderByTimestampDesc(String userId);
    
    List<Notification> findByUserIdAndAcknowledgedFalseOrderByTimestampDesc(String userId);
    
    List<Notification> findByUserIdAndCategoryAndReadFalse(String userId, String category);
    
    @Query("{'userId': ?0, 'category': ?1}")
    List<Notification> findByUserIdAndCategory(String userId, String category);
    
    @Query("{'userId': ?0, 'priority': ?1}")
    List<Notification> findByUserIdAndPriority(String userId, String priority);
    
    @Query("{'userId': ?0, 'timestamp': {$gte: ?1}}")
    List<Notification> findByUserIdAndTimestampAfter(String userId, LocalDateTime timestamp);
    
    @Query("{'userId': ?0, 'read': false, 'timestamp': {$gte: ?1}}")
    List<Notification> findUnreadByUserIdAndTimestampAfter(String userId, LocalDateTime timestamp);
    
    @Query("{'userId': ?0, 'acknowledged': false}")
    List<Notification> findUnacknowledgedByUserId(String userId);
    
    @Query("{'expiresAt': {$lt: ?0}}")
    List<Notification> findExpiredNotifications(LocalDateTime now);
    
    void deleteByUserIdAndTimestampBefore(String userId, LocalDateTime timestamp);
    
    // Compteurs avec filtre deleted=false
    long countByUserIdAndReadFalseAndDeletedFalse(String userId);
    
    long countByUserIdAndAcknowledgedFalseAndDeletedFalse(String userId);
    
    // Compteurs legacy (sans filtre deleted)
    long countByUserIdAndReadFalse(String userId);
    
    long countByUserIdAndAcknowledgedFalse(String userId);
    
    // Méthodes pour le traitement par lots
    Page<Notification> findByStatusOrderByTimestampAsc(String status, Pageable pageable);
    
    @Query("{'status': 'PENDING', 'nextProcessingTime': {$lte: ?0}}")
    Page<Notification> findRetryableNotifications(LocalDateTime now, Pageable pageable);
    
    @Query("{'status': 'FAILED', 'retryCount': {$lt: ?0}}")
    List<Notification> findFailedWithRetryCount(int maxRetries);

    // Méthodes pour le nettoyage automatique
    @Query("{'expiresAt': {$lt: ?0}, 'acknowledged': false}")
    List<Notification> findByExpiresAtBeforeAndAcknowledgedFalse(LocalDateTime expirationDate);
    
    @Query("{'timestamp': {$lt: ?0}, 'read': true, 'acknowledged': true}")
    List<Notification> findByTimestampBeforeAndReadTrueAndAcknowledgedTrue(LocalDateTime date);
    
    @Query("{'status': {$in: ['SENT', 'FAILED']}, 'timestamp': {$lt: ?0}}")
    List<Notification> findProcessedNotificationsOlderThan(LocalDateTime date);
    
    // Méthode pour analytics
    List<Notification> findByReadFalse();
    
    // Méthode pour récupérer les alertes déléguées
    List<Notification> findByUserIdAndTypeAndDeletedFalseOrderByTimestampDesc(String userId, String type);
    
    // Méthode pour trouver les anciennes notifications lues
    List<Notification> findByTimestampBeforeAndReadTrueAndDeletedFalse(java.time.LocalDateTime timestamp);
}