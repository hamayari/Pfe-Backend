package com.example.demo.repository;

import com.example.demo.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    List<NotificationLog> findByRecipientId(String recipientId);
    List<NotificationLog> findByInvoiceId(String invoiceId);
    long countByRecipientIdAndStatusNot(String recipientId, String status);
    List<NotificationLog> findByIdInAndRecipientId(List<String> ids, String recipientId);
    
    // Méthodes pour analytics
    long countByStatus(String status);
    long countByChannel(String channel);
    List<NotificationLog> findBySentAtBetween(LocalDateTime start, LocalDateTime end);
    List<NotificationLog> findByType(String type);
    List<NotificationLog> findByStatus(String status);
    List<NotificationLog> findByStatusOrderBySentAtDesc(String status);
}