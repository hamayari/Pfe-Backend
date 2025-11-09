package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "notification_templates")
public class NotificationTemplate {
    
    @Id
    private String id;
    
    private String name;
    private String description;
    private String type; // EMAIL, SMS, PUSH
    private String subject; // Pour les emails
    private String content; // Contenu du message
    private List<String> variables; // Variables disponibles dans le template
    private boolean active;
    
    // Audit fields
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Constructor for convenience
    public NotificationTemplate(String id, String name, String type, String subject, String content) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.subject = subject;
        this.content = content;
        this.active = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}











