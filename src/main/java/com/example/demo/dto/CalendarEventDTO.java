package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalendarEventDTO {
    private String id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime start; // Alias pour startDate
    private LocalDateTime end;   // Alias pour endDate
    private String eventType;
    private String type;     // Alias pour eventType
    private String status;
    private String organizerId;
    private String organizerName;
    private String createdBy;
    private String location;
    private boolean allDay;
    private String recurrence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String color;
    
    // Getters et setters personnalisés pour les alias
    public LocalDateTime getStart() {
        return start != null ? start : startDate;
    }
    
    public void setStart(LocalDateTime start) {
        this.start = start;
        this.startDate = start;
    }
    
    public LocalDateTime getEnd() {
        return end != null ? end : endDate;
    }
    
    public void setEnd(LocalDateTime end) {
        this.end = end;
        this.endDate = end;
    }
    
    public String getType() {
        return type != null ? type : eventType;
    }
    
    public void setType(String type) {
        this.type = type;
        this.eventType = type;
    }
    
    public String getCreatedBy() {
        return createdBy != null ? createdBy : organizerId;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        this.organizerId = createdBy;
    }
}
