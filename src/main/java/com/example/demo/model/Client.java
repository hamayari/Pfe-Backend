package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "clients")
public class Client {
    @Id
    private String id;
    
    private String email;
    private String password;
    private String name;
    private String phoneNumber;
    private String company;
    private String address;
    
    // Statut du compte
    private boolean active = true;
    private boolean emailVerified = false;
    private boolean forcePasswordChange = true;
    
    // Dates
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    
    // Historique des connexions
    private List<LoginHistory> loginHistory;
    
    // Factures associées
    private List<String> invoiceIds;
    
    // Notes et commentaires
    private String notes;
    private String createdBy;
    
    // Configuration des notifications
    private boolean emailNotificationsEnabled = true;
    private boolean smsNotificationsEnabled = false;
    
    public static class LoginHistory {
        private LocalDateTime loginTime;
        private String ipAddress;
        private String userAgent;
        private boolean successful;
        
        public LoginHistory(LocalDateTime loginTime, String ipAddress, String userAgent, boolean successful) {
            this.loginTime = loginTime;
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
            this.successful = successful;
        }
        
        // Getters et setters
        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        
        public boolean isSuccessful() { return successful; }
        public void setSuccessful(boolean successful) { this.successful = successful; }
    }
} 