package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    List<Notification> findByUserIdOrderByTimestampDesc(String userId);
    
    List<Notification> findByUserIdAndReadFalseOrderByTimestampDesc(String userId);
    
    List<Notification> findByUserIdAndAcknowledgedFalseOrderByTimestampDesc(String userId);
    
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
    
    long countByUserIdAndReadFalse(String userId);
    
    long countByUserIdAndAcknowledgedFalse(String userId);
}