package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationRecipient {
    
    private String userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean primary; // true = destinataire principal, false = en copie
    private NotificationPreferences preferences;
    
    // Constructor for convenience
    public NotificationRecipient(String userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.primary = true;
    }
}




