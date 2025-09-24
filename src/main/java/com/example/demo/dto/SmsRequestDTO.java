package com.example.demo.dto;

import lombok.Data;

@Data
public class SmsRequestDTO {
    private String phoneNumber;
    private String to; // Alias pour phoneNumber
    private String message;
    private String type;
    private String priority;
    private String userId;
    
    // Getters pour compatibilité
    public String getTo() {
        return to != null ? to : phoneNumber;
    }
    
    public void setTo(String to) {
        this.to = to;
        this.phoneNumber = to;
    }
}
