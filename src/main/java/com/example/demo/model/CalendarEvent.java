package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "calendar_events")
public class CalendarEvent {

    @Id
    private String id;

    @Indexed
    private String title;

    @Indexed
    private LocalDateTime date;

    @Indexed
    private String type; // echeance, convention, facture

    @Indexed
    private String status; // pending, overdue, completed

    private String color;

    private String description;

    @Indexed
    private String userId;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructeurs
    public CalendarEvent() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CalendarEvent(String title, LocalDateTime date, String type, String status) {
        this();
        this.title = title;
        this.date = date;
        this.type = type;
        this.status = status;
    }

    public CalendarEvent(String title, LocalDateTime date, String type, String status, String description) {
        this(title, date, type, status);
        this.description = description;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Alias methods for compatibility
    public String getCreatedBy() {
        return userId;
    }

    public void setCreatedBy(String createdBy) {
        this.userId = createdBy;
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", color='" + color + '\'' +
                ", description='" + description + '\'' +
                ", userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 