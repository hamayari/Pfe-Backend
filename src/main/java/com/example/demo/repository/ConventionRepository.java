package com.example.demo.repository;

import com.example.demo.model.Convention;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface ConventionRepository extends MongoRepository<Convention, String> {
    List<Convention> findByCreatedBy(String userId);
    List<Convention> findByCreatedByAndDueDateBefore(String userId, LocalDate date);
    List<Convention> findByCreatedByAndStatus(String userId, String status);
    boolean existsByReference(String reference);
    List<Convention> findByStatus(String status);
    List<Convention> findByCommercial(String commercial);
    List<Convention> findByStructureId(String structureId);
    List<Convention> findByZoneGeographiqueId(String zoneGeographiqueId);
    List<Convention> findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(String zoneGeographiqueId, String structureId, String applicationId, LocalDate startDate, LocalDate endDate);
    List<Convention> findByPaymentStatus(String paymentStatus);
    long countByStatus(String status);
    long countByEndDateBefore(LocalDate date);
    
    // Méthodes pour le système de notifications
    List<Convention> findByEndDateBeforeAndStatus(LocalDate date, String status);
    long countByCreatedAtBetween(LocalDate start, LocalDate end);
    
    // Méthodes pour le scheduler de notifications
    List<Convention> findByEcheancesContaining(LocalDate echeance);
    List<Convention> findByEcheancesContainingAndStatusNot(LocalDate echeance, String status);
    
    // Recherche par client
    List<Convention> findByClient(String client);
    
    // Recherche par référence
    Convention findByReference(String reference);
}
