package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NotificationDTO {
    private String id;
    private String recipientId;
    private String userId;  // Alias pour recipientId
    private String title;
    private String message;
    private String type;
    private String priority;
    private String category;
    private boolean read;
    private boolean acknowledged;
    private LocalDateTime createdAt;
    private LocalDateTime timestamp; // Alias pour createdAt
    private LocalDateTime readAt;
    private LocalDateTime acknowledgedAt;
    private String sourceId;
    private String sourceType;
    private String source;
    private String actionUrl;
    private Map<String, Object> metadata;
    private LocalDateTime expiresAt;
    
    // Getters et setters personnalisés pour les alias
    public String getUserId() {
        return userId != null ? userId : recipientId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
        this.recipientId = userId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp != null ? timestamp : createdAt;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        this.createdAt = timestamp;
    }
    
    public String getSource() {
        return source != null ? source : sourceType;
    }
    
    public void setSource(String source) {
        this.source = source;
        this.sourceType = source;
    }
}
