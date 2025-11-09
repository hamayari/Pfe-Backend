package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "notifications")
public class Notification {
    
    @Id
    private String id;
    
    @Indexed
    private String type; // info, success, warning, error, system
    
    private String title;
    private String message;
    
    @Indexed
    private String priority; // low, medium, high, critical
    
    @Indexed
    private String category; // dashboard, convention, invoice, payment, system, user, monitoring
    
    @Indexed
    private String userId;
    
    @Indexed
    private LocalDateTime timestamp;
    
    private boolean read;
    private boolean acknowledged;
    
    @Indexed
    private boolean deleted = false; // Soft delete
    
    private LocalDateTime readAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime deletedAt;
    
    private Map<String, Object> metadata;
    private String source;
    private LocalDateTime expiresAt;
    
    // Champs pour lier à une entité
    private String relatedEntityId;
    private String relatedEntityType; // CONVENTION, INVOICE, USER, etc.
    
    // Champs d'audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Champs pour le traitement par lots
    @Indexed
    private String status = "PENDING"; // PENDING, SENT, FAILED
    private Integer retryCount = 0;
    private LocalDateTime processedTime;
    private LocalDateTime nextProcessingTime;
    private String lastError;
    
    // Constructor for convenience
    public Notification(String type, String title, String message, String priority, String category, String userId) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.category = category;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
        this.read = false;
        this.acknowledged = false;
        this.deleted = false;
        this.status = "PENDING";
        this.retryCount = 0;
    }
}