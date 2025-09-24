package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponseDTO {
    private String id;
    private String smsId; // Alias pour id
    private String twilioSid;
    private String status;
    private String message;
    private String phoneNumber;
    private LocalDateTime sentAt;
    private boolean success;
    private String errorMessage;
    
    // Constructor needed by SmsController
    public SmsResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }
    
    // Getters pour compatibilité
    public String getSmsId() {
        return smsId != null ? smsId : id;
    }
    
    public void setSmsId(String smsId) {
        this.smsId = smsId;
        this.id = smsId;
    }
}
