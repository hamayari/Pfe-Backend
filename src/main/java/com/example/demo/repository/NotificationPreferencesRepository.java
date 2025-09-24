package com.example.demo.repository;

import com.example.demo.model.NotificationPreferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferencesRepository extends MongoRepository<NotificationPreferences, String> {
    
    /**
     * Trouver les préférences d'un utilisateur
     */
    Optional<NotificationPreferences> findByUserId(String userId);
    
    /**
     * Vérifier si un utilisateur a des préférences configurées
     */
    boolean existsByUserId(String userId);
    
    /**
     * Supprimer les préférences d'un utilisateur
     */
    void deleteByUserId(String userId);
}