package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WebSocketMessageDTO {
    private String id;
    private String senderId;
    private String senderName;
    private String recipientId;
    private String content;
    private String type;
    private LocalDateTime timestamp;
    private LocalDateTime sentAt; // Alias pour timestamp
    private String conversationId;
    
    // Getters pour compatibilité
    public LocalDateTime getSentAt() {
        return sentAt != null ? sentAt : timestamp;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
        this.timestamp = sentAt;
    }
}
