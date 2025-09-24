package com.example.demo.repository;

import com.example.demo.model.NotificationTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String> {
    
    /**
     * Trouver tous les templates actifs
     */
    List<NotificationTemplate> findByActiveTrue();
    
    /**
     * Trouver les templates par type
     */
    List<NotificationTemplate> findByTypeAndActiveTrue(String type);
    
    /**
     * Trouver les templates par type (tous)
     */
    List<NotificationTemplate> findByType(String type);
    
    /**
     * Vérifier si un template existe par ID
     */
    boolean existsById(String id);
}




