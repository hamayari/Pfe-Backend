package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.ConventionHistory;
import com.example.demo.repository.ConventionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pour gérer l'historique des modifications des conventions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConventionHistoryService {

    private final ConventionHistoryRepository historyRepository;

    /**
     * Enregistre la création d'une convention
     */
    public void recordCreate(Convention convention, String userId, String userName) {
        try {
            ConventionHistory history = ConventionHistory.forCreate(
                convention.getId(),
                convention.getReference(),
                userId,
                userName
            );
            historyRepository.save(history);
            log.info("📝 Historique CREATE enregistré pour convention {}", convention.getReference());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'enregistrement de l'historique CREATE: {}", e.getMessage());
        }
    }

    /**
     * Enregistre une mise à jour de convention
     * Compare l'ancienne et la nouvelle version pour détecter les changements
     */
    public void recordUpdate(Convention oldConvention, Convention newConvention, String userId, String userName) {
        try {
            // Comparer les champs et enregistrer les différences
            compareAndRecord(oldConvention, newConvention, userId, userName);
            log.info("📝 Historique UPDATE enregistré pour convention {}", newConvention.getReference());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'enregistrement de l'historique UPDATE: {}", e.getMessage());
        }
    }

    /**
     * Enregistre un changement de statut
     */
    public void recordStatusChange(Convention convention, String oldStatus, String newStatus, String userId, String userName) {
        try {
            ConventionHistory history = ConventionHistory.forStatusChange(
                convention.getId(),
                convention.getReference(),
                oldStatus,
                newStatus,
                userId,
                userName
            );
            historyRepository.save(history);
            log.info("📝 Historique STATUS_CHANGE enregistré pour convention {}", convention.getReference());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'enregistrement de l'historique STATUS_CHANGE: {}", e.getMessage());
        }
    }

    /**
     * Enregistre la suppression d'une convention
     */
    public void recordDelete(Convention convention, String userId, String userName) {
        try {
            ConventionHistory history = ConventionHistory.forDelete(
                convention.getId(),
                convention.getReference(),
                userId,
                userName
            );
            historyRepository.save(history);
            log.info("📝 Historique DELETE enregistré pour convention {}", convention.getReference());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'enregistrement de l'historique DELETE: {}", e.getMessage());
        }
    }

    /**
     * Récupère l'historique complet d'une convention
     */
    public List<ConventionHistory> getConventionHistory(String conventionId) {
        return historyRepository.findByConventionIdOrderByModifiedAtDesc(conventionId);
    }

    /**
     * Récupère l'historique par référence
     */
    public List<ConventionHistory> getHistoryByReference(String reference) {
        return historyRepository.findByConventionReferenceOrderByModifiedAtDesc(reference);
    }

    /**
     * Récupère l'historique d'un utilisateur
     */
    public List<ConventionHistory> getUserHistory(String userId) {
        return historyRepository.findByModifiedByOrderByModifiedAtDesc(userId);
    }

    /**
     * Compte le nombre de modifications d'une convention
     */
    public long countModifications(String conventionId) {
        return historyRepository.countByConventionId(conventionId);
    }

    /**
     * Compare deux conventions et enregistre les différences
     */
    private void compareAndRecord(Convention oldConv, Convention newConv, String userId, String userName) {
        String conventionId = newConv.getId();
        String reference = newConv.getReference();

        // Comparer le titre
        if (!equals(oldConv.getTitle(), newConv.getTitle())) {
            saveFieldChange(conventionId, reference, "title", 
                          oldConv.getTitle(), newConv.getTitle(), userId, userName);
        }

        // Comparer la description
        if (!equals(oldConv.getDescription(), newConv.getDescription())) {
            saveFieldChange(conventionId, reference, "description", 
                          oldConv.getDescription(), newConv.getDescription(), userId, userName);
        }

        // Comparer le montant
        if (!equals(oldConv.getAmount(), newConv.getAmount())) {
            saveFieldChange(conventionId, reference, "amount", 
                          String.valueOf(oldConv.getAmount()), 
                          String.valueOf(newConv.getAmount()), userId, userName);
        }

        // Comparer les dates
        if (!equals(oldConv.getStartDate(), newConv.getStartDate())) {
            saveFieldChange(conventionId, reference, "startDate", 
                          formatDate(oldConv.getStartDate()), 
                          formatDate(newConv.getStartDate()), userId, userName);
        }

        if (!equals(oldConv.getEndDate(), newConv.getEndDate())) {
            saveFieldChange(conventionId, reference, "endDate", 
                          formatDate(oldConv.getEndDate()), 
                          formatDate(newConv.getEndDate()), userId, userName);
        }

        // Comparer le statut
        if (!equals(oldConv.getStatus(), newConv.getStatus())) {
            recordStatusChange(newConv, oldConv.getStatus(), newConv.getStatus(), userId, userName);
        }

        // Comparer la structure
        if (!equals(oldConv.getStructureId(), newConv.getStructureId())) {
            saveFieldChange(conventionId, reference, "structureId", 
                          oldConv.getStructureId(), newConv.getStructureId(), userId, userName);
        }

        // Comparer le gouvernorat
        if (!equals(oldConv.getGovernorate(), newConv.getGovernorate())) {
            saveFieldChange(conventionId, reference, "governorate", 
                          oldConv.getGovernorate(), newConv.getGovernorate(), userId, userName);
        }
    }

    /**
     * Enregistre un changement de champ
     */
    private void saveFieldChange(String conventionId, String reference, String fieldName, 
                                  String oldValue, String newValue, String userId, String userName) {
        ConventionHistory history = ConventionHistory.forUpdate(
            conventionId, reference, fieldName, oldValue, newValue, userId, userName
        );
        historyRepository.save(history);
    }

    /**
     * Compare deux objets (gère les null)
     */
    private boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 == null || obj2 == null) return false;
        return obj1.equals(obj2);
    }

    /**
     * Formate une date pour l'affichage
     */
    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "null";
    }
}
