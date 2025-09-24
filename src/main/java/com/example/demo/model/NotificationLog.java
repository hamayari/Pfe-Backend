package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "notification_logs")
public class NotificationLog {
    @Id
    private String id;
    private String type; // EMAIL, SMS, SYSTEM
    private LocalDateTime sentAt;
    private String recipient;
    private String recipientId;
    private String status;
    private String message;
    private String subject;
    
    // Reference to related entities
    private String invoiceId;
    private String conventionId;
    
    // Tracking
    private int retryCount = 0;
    private LocalDateTime lastRetryAt;
    private String errorMessage;
    
    public enum NotificationType {
        EMAIL,
        SMS,
        SYSTEM
    }
    
    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED,
        DELIVERED
    }
}
