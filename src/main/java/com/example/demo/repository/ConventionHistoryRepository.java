package com.example.demo.repository;

import com.example.demo.model.ConventionHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConventionHistoryRepository extends MongoRepository<ConventionHistory, String> {
    
    /**
     * Trouve l'historique d'une convention spécifique
     */
    List<ConventionHistory> findByConventionIdOrderByModifiedAtDesc(String conventionId);
    
    /**
     * Trouve l'historique par référence de convention
     */
    List<ConventionHistory> findByConventionReferenceOrderByModifiedAtDesc(String reference);
    
    /**
     * Trouve l'historique par utilisateur
     */
    List<ConventionHistory> findByModifiedByOrderByModifiedAtDesc(String userId);
    
    /**
     * Trouve l'historique par type d'action
     */
    List<ConventionHistory> findByActionOrderByModifiedAtDesc(String action);
    
    /**
     * Trouve l'historique dans une période
     */
    List<ConventionHistory> findByModifiedAtBetweenOrderByModifiedAtDesc(LocalDateTime start, LocalDateTime end);
    
    /**
     * Compte les modifications d'une convention
     */
    long countByConventionId(String conventionId);
}
