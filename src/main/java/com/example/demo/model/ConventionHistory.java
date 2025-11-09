package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Modèle pour l'historique des modifications des conventions
 * Permet de tracer toutes les modifications apportées à une convention
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "convention_history")
public class ConventionHistory {
    
    @Id
    private String id;
    
    @Indexed
    private String conventionId; // ID de la convention modifiée
    
    private String conventionReference; // Référence pour faciliter la recherche
    
    @Indexed
    private String action; // CREATE, UPDATE, DELETE, STATUS_CHANGE
    
    private String fieldName; // Nom du champ modifié (null pour CREATE/DELETE)
    
    private String oldValue; // Ancienne valeur (format String)
    
    private String newValue; // Nouvelle valeur (format String)
    
    @Indexed
    private String modifiedBy; // ID de l'utilisateur qui a fait la modification
    
    private String modifiedByName; // Nom de l'utilisateur pour affichage
    
    @Indexed
    private LocalDateTime modifiedAt; // Date et heure de la modification
    
    private String ipAddress; // Adresse IP de l'utilisateur
    
    private String userAgent; // User agent du navigateur
    
    private Map<String, Object> metadata; // Métadonnées supplémentaires
    
    private String comment; // Commentaire optionnel sur la modification
    
    /**
     * Constructeur pour une création de convention
     */
    public static ConventionHistory forCreate(String conventionId, String reference, String userId, String userName) {
        ConventionHistory history = new ConventionHistory();
        history.setConventionId(conventionId);
        history.setConventionReference(reference);
        history.setAction("CREATE");
        history.setModifiedBy(userId);
        history.setModifiedByName(userName);
        history.setModifiedAt(LocalDateTime.now());
        history.setComment("Convention créée");
        return history;
    }
    
    /**
     * Constructeur pour une mise à jour de champ
     */
    public static ConventionHistory forUpdate(String conventionId, String reference, String fieldName, 
                                               String oldValue, String newValue, String userId, String userName) {
        ConventionHistory history = new ConventionHistory();
        history.setConventionId(conventionId);
        history.setConventionReference(reference);
        history.setAction("UPDATE");
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setModifiedBy(userId);
        history.setModifiedByName(userName);
        history.setModifiedAt(LocalDateTime.now());
        return history;
    }
    
    /**
     * Constructeur pour un changement de statut
     */
    public static ConventionHistory forStatusChange(String conventionId, String reference, 
                                                     String oldStatus, String newStatus, String userId, String userName) {
        ConventionHistory history = new ConventionHistory();
        history.setConventionId(conventionId);
        history.setConventionReference(reference);
        history.setAction("STATUS_CHANGE");
        history.setFieldName("status");
        history.setOldValue(oldStatus);
        history.setNewValue(newStatus);
        history.setModifiedBy(userId);
        history.setModifiedByName(userName);
        history.setModifiedAt(LocalDateTime.now());
        return history;
    }
    
    /**
     * Constructeur pour une suppression
     */
    public static ConventionHistory forDelete(String conventionId, String reference, String userId, String userName) {
        ConventionHistory history = new ConventionHistory();
        history.setConventionId(conventionId);
        history.setConventionReference(reference);
        history.setAction("DELETE");
        history.setModifiedBy(userId);
        history.setModifiedByName(userName);
        history.setModifiedAt(LocalDateTime.now());
        history.setComment("Convention supprimée");
        return history;
    }
    
    /**
     * Retourne une description lisible de la modification
     */
    public String getDescription() {
        switch (action) {
            case "CREATE":
                return "Convention créée";
            case "DELETE":
                return "Convention supprimée";
            case "STATUS_CHANGE":
                return String.format("Statut changé de '%s' à '%s'", oldValue, newValue);
            case "UPDATE":
                if (fieldName != null) {
                    return String.format("Champ '%s' modifié : '%s' → '%s'", 
                                       getFieldLabel(fieldName), oldValue, newValue);
                }
                return "Convention modifiée";
            default:
                return "Action inconnue";
        }
    }
    
    /**
     * Retourne le label français du champ
     */
    private String getFieldLabel(String field) {
        switch (field) {
            case "title": return "Titre";
            case "description": return "Description";
            case "amount": return "Montant";
            case "startDate": return "Date de début";
            case "endDate": return "Date de fin";
            case "status": return "Statut";
            case "structureId": return "Structure";
            case "governorate": return "Gouvernorat";
            case "paymentTerms": return "Modalités de paiement";
            default: return field;
        }
    }
    
    /**
     * Retourne une icône selon le type d'action
     */
    public String getActionIcon() {
        switch (action) {
            case "CREATE": return "add_circle";
            case "DELETE": return "delete";
            case "STATUS_CHANGE": return "swap_horiz";
            case "UPDATE": return "edit";
            default: return "info";
        }
    }
    
    /**
     * Retourne une couleur selon le type d'action
     */
    public String getActionColor() {
        switch (action) {
            case "CREATE": return "#4CAF50"; // Vert
            case "DELETE": return "#F44336"; // Rouge
            case "STATUS_CHANGE": return "#FF9800"; // Orange
            case "UPDATE": return "#2196F3"; // Bleu
            default: return "#9E9E9E"; // Gris
        }
    }
}
