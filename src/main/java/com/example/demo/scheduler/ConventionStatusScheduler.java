package com.example.demo.scheduler;

import com.example.demo.model.Convention;
import com.example.demo.repository.ConventionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler pour mettre à jour automatiquement les statuts des conventions
 * Exécuté tous les jours à 1h du matin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConventionStatusScheduler {

    private final ConventionRepository conventionRepository;

    /**
     * Met à jour les statuts des conventions selon leurs dates
     * - EXPIRED : Date de fin dépassée
     * - PROCHE_ECHEANCE : Moins de 30 jours avant la fin
     * - ACTIVE : Entre date début et fin (plus de 30 jours restants)
     */
    @Scheduled(cron = "0 0 1 * * ?") // Tous les jours à 1h00
    public void updateConventionStatuses() {
        log.info("🔄 Début de la mise à jour automatique des statuts des conventions");
        
        LocalDate today = LocalDate.now();
        LocalDate alertThreshold = today.plusDays(30);
        
        List<Convention> allConventions = conventionRepository.findAll();
        int updatedCount = 0;
        int expiredCount = 0;
        int nearExpirationCount = 0;
        
        for (Convention convention : allConventions) {
            String oldStatus = convention.getStatus();
            String newStatus = calculateStatus(convention, today, alertThreshold);
            
            if (!newStatus.equals(oldStatus)) {
                convention.setStatus(newStatus);
                convention.setUpdatedAt(LocalDate.now());
                conventionRepository.save(convention);
                updatedCount++;
                
                if ("EXPIRED".equals(newStatus)) {
                    expiredCount++;
                    log.info("⏰ Convention {} est maintenant EXPIRED", convention.getReference());
                } else if ("PROCHE_ECHEANCE".equals(newStatus)) {
                    nearExpirationCount++;
                    log.info("⚠️ Convention {} est maintenant PROCHE_ECHEANCE", convention.getReference());
                }
            }
        }
        
        log.info("✅ Mise à jour terminée : {} conventions mises à jour", updatedCount);
        log.info("   - {} conventions expirées", expiredCount);
        log.info("   - {} conventions proches de l'échéance", nearExpirationCount);
    }

    /**
     * Calcule le statut d'une convention selon ses dates
     */
    private String calculateStatus(Convention convention, LocalDate today, LocalDate alertThreshold) {
        LocalDate startDate = convention.getStartDate();
        LocalDate endDate = convention.getEndDate();
        
        // Si pas de dates définies, garder le statut actuel
        if (startDate == null || endDate == null) {
            return convention.getStatus();
        }
        
        // Convention pas encore commencée
        if (today.isBefore(startDate)) {
            return "PENDING";
        }
        
        // Convention expirée
        if (today.isAfter(endDate)) {
            return "EXPIRED";
        }
        
        // Convention proche de l'échéance (moins de 30 jours)
        if (endDate.isBefore(alertThreshold)) {
            return "PROCHE_ECHEANCE";
        }
        
        // Convention active
        return "ACTIVE";
    }

    /**
     * Méthode manuelle pour forcer la mise à jour (utile pour les tests)
     */
    public void forceUpdate() {
        log.info("🔧 Mise à jour manuelle forcée des statuts");
        updateConventionStatuses();
    }
}
