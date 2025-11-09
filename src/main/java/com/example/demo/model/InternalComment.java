package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "internal_comments")
public class InternalComment {
    @Id
    private String id;
    
    private String author; // Username du chef de projet
    private String content;
    private LocalDateTime date;
    private String mentionedCommercialId; // ID du commercial mentionné (optionnel)
    private String mentionedCommercialName;
    
    // Métadonnées
    private LocalDateTime createdAt;
    private String createdBy;
}
