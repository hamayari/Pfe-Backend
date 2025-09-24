package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "notification_preferences")
public class NotificationPreferences {
    
    @Id
    private String id;
    
    private String userId;
    
    // Email notifications
    private boolean emailEnabled;
    private String emailFrequency; // immediate, hourly, daily, weekly
    private EmailTypes emailTypes;
    
    // SMS notifications
    private boolean smsEnabled;
    private SmsTypes smsTypes;
    
    // Push notifications
    private boolean pushEnabled;
    private PushTypes pushTypes;
    
    // Quiet hours
    private boolean quietHoursEnabled;
    private String quietHoursStart; // HH:mm format
    private String quietHoursEnd;   // HH:mm format
    private String[] quietHoursDays; // ["monday", "tuesday", ...]
    
    // Thresholds
    private NotificationThresholds thresholds;
    
    // Channels
    private NotificationChannels channels;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor for convenience
    public NotificationPreferences(String userId) {
        this.userId = userId;
        this.emailEnabled = true;
        this.emailFrequency = "daily";
        this.smsEnabled = false;
        this.pushEnabled = true;
        this.quietHoursEnabled = false;
        this.quietHoursStart = "22:00";
        this.quietHoursEnd = "08:00";
        this.quietHoursDays = new String[]{"monday", "tuesday", "wednesday", "thursday", "friday"};
        
        // Initialize types with defaults
        this.emailTypes = new EmailTypes();
        this.smsTypes = new SmsTypes();
        this.pushTypes = new PushTypes();
        
        // Initialize thresholds with defaults
        this.thresholds = new NotificationThresholds();
        
        // Initialize channels
        this.channels = new NotificationChannels();
        
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @Data
    @NoArgsConstructor
    public static class EmailTypes {
        private boolean conventions = true;
        private boolean invoices = true;
        private boolean payments = true;
        private boolean system = false;
        private boolean security = true;
    }
    
    @Data
    @NoArgsConstructor
    public static class SmsTypes {
        private boolean urgent = true;
        private boolean overdue = true;
        private boolean system = false;
    }
    
    @Data
    @NoArgsConstructor
    public static class PushTypes {
        private boolean conventions = true;
        private boolean invoices = true;
        private boolean payments = true;
        private boolean system = false;
    }
    
    @Data
    @NoArgsConstructor
    public static class NotificationThresholds {
        private int overdueInvoices = 7; // jours avant échéance pour rappel
        private double lowBalance = 1000.0; // seuil de solde faible
        private int systemErrors = 10; // nombre d'erreurs système
    }
    
    @Data
    @NoArgsConstructor
    public static class NotificationChannels {
        private String email;
        private String sms;
        private String slack;
        private String teams;
    }
}