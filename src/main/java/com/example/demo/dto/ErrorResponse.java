package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Classe de réponse pour les erreurs API
 */
@Data
@NoArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    /**
     * Constructeur avec timestamp, status, error, message et path
     * @param timestamp Le timestamp de l'erreur
     * @param status Le code d'état HTTP
     * @param error Le type d'erreur
     * @param message Le message détaillé de l'erreur
     * @param path Le chemin de la requête qui a causé l'erreur
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
    
    /**
     * Constructeur avec un seul message d'erreur
     * @param message Le message d'erreur
     */
    public ErrorResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.status = 500;
        this.error = "Erreur";
        this.message = message;
    }
    
    /**
     * Constructeur avec titre d'erreur et message détaillé
     * @param error Le titre de l'erreur
     * @param message Le message détaillé de l'erreur
     */
    public ErrorResponse(String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = 400;
        this.error = error;
        this.message = message;
    }
}
