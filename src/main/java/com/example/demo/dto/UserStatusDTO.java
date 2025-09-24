package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserStatusDTO {
    private String userId;
    private String username;
    private String status; // ONLINE, OFFLINE, AWAY
    private String statusMessage;
    private LocalDateTime lastSeen;
    private LocalDateTime timestamp;
    private String currentActivity;
    
    // Getters pour compatibilité
    public String getStatusMessage() {
        return statusMessage != null ? statusMessage : status;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        if (this.lastSeen == null) {
            this.lastSeen = timestamp;
        }
    }
}
