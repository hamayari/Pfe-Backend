package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationSettingsDTO {
    private String userId;
    private boolean emailNotifications;
    private boolean emailEnabled; // Alias
    private boolean smsNotifications;
    private boolean smsEnabled;   // Alias
    private boolean pushNotifications;
    private boolean pushEnabled;  // Alias
    private boolean invoiceNotifications;
    private boolean conventionNotifications;
    private boolean paymentNotifications;
    private boolean systemNotifications;
    private String timezone;
    private List<Integer> reminderDays;
    private boolean autoReminderEnabled;
    private String reminderFrequency;
    private boolean quietHoursEnabled;
    
    // Getters pour compatibilité
    public boolean isEmailEnabled() {
        return emailEnabled || emailNotifications;
    }
    
    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
        this.emailNotifications = emailEnabled;
    }
    
    public boolean isSmsEnabled() {
        return smsEnabled || smsNotifications;
    }
    
    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
        this.smsNotifications = smsEnabled;
    }

    public boolean isQuietHoursEnabled() {
        return quietHoursEnabled;
    }
}