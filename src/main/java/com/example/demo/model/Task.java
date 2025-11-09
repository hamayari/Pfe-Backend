package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "tasks")
public class Task {
    
    @Id
    private String id;
    
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer progress; // 0-100
    private String status; // not-started, in-progress, completed, delayed
    private String assignee;
    private String assigneeId;
    private String projectId;
    private String conventionId;
    private String priority; // low, medium, high
    private List<String> dependencies; // IDs des tâches dépendantes
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Constructeurs
    public Task() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.progress = 0;
        this.status = "not-started";
    }
    
    public Task(String name, LocalDateTime startDate, LocalDateTime endDate) {
        this();
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
        this.updateStatus();
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAssignee() {
        return assignee;
    }
    
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
    
    public String getAssigneeId() {
        return assigneeId;
    }
    
    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getConventionId() {
        return conventionId;
    }
    
    public void setConventionId(String conventionId) {
        this.conventionId = conventionId;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
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
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    // Méthodes utilitaires
    private void updateStatus() {
        if (this.progress == 0) {
            this.status = "not-started";
        } else if (this.progress == 100) {
            this.status = "completed";
        } else {
            this.status = "in-progress";
        }
        
        // Vérifier si en retard
        if (this.endDate != null && LocalDateTime.now().isAfter(this.endDate) && this.progress < 100) {
            this.status = "delayed";
        }
    }
    
    public boolean isDelayed() {
        return this.endDate != null && 
               LocalDateTime.now().isAfter(this.endDate) && 
               this.progress < 100;
    }
    
    public long getDurationInDays() {
        if (this.startDate != null && this.endDate != null) {
            return java.time.Duration.between(this.startDate, this.endDate).toDays();
        }
        return 0;
    }
}
