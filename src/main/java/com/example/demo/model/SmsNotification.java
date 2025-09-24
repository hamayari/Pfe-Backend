package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "sms_notifications")
public class SmsNotification {

    @Id
    private String id;

    @Indexed
    private String to;

    private String message;

    @Indexed
    private String status; // SENT, FAILED, DELIVERED, UNDELIVERED

    @Indexed
    private String type; // ECHEANCE_REMINDER, INVOICE_NOTIFICATION, PAYMENT_CONFIRMATION, etc.

    @Indexed
    private String userId;

    private String twilioSid;

    private String errorMessage;

    @Indexed
    private LocalDateTime sentAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime updatedAt;

    // Constructeurs
    public SmsNotification() {
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SmsNotification(String to, String message, String type, String userId) {
        this();
        this.to = to;
        this.message = message;
        this.type = type;
        this.userId = userId;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTwilioSid() {
        return twilioSid;
    }

    public void setTwilioSid(String twilioSid) {
        this.twilioSid = twilioSid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "SmsNotification{" +
                "id='" + id + '\'' +
                ", to='" + to + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", userId='" + userId + '\'' +
                ", twilioSid='" + twilioSid + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", sentAt=" + sentAt +
                ", deliveredAt=" + deliveredAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 