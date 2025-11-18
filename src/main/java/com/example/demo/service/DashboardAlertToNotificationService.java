package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.model.Convention;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.ConventionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service qui convertit les alertes du dashboard en notifications navbar
 * Les alertes restent dans le dashboard ET apparaissent dans l'icône 🔔
 */
@Service
public class DashboardAlertToNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardAlertToNotificationService.class);
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InAppNotificationService inAppNotificationService;
    
    // Cache pour éviter les doublons de notifications
    private final Set<String> processedAlerts = new HashSet<>();
    
    /**
     * 🔔 Vérification automatique toutes les 5 minutes
     * Crée des notifications pour les alertes du dashboard
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300000 ms
    public void syncDashboardAlertsToNotifications() {
        logger.info("🔄 [SYNC] Synchronisation alertes dashboard → notifications navbar");
        
        try {
            // 1. Factures à échéance proche (7 jours)
            syncUpcomingInvoices();
            
            // 2. Factures en retard
            syncOverdueInvoices();
            
            // 3. Conventions expirées
            syncExpiredConventions();
            
            // 4. Conventions à échéance proche
            syncUpcomingConventions();
            
            logger.info("✅ [SYNC] Synchronisation terminée");
            
        } catch (Exception e) {
            logger.error("❌ [SYNC] Erreur: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 📅 Factures à échéance proche (dans les 7 prochains jours)
     */
    private void syncUpcomingInvoices() {
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);
        
        // Trouver toutes les factures PENDING avec échéance < 7 jours
        List<Invoice> allInvoices = invoiceRepository.findByStatus("PENDING");
        
        int count = 0;
        
        for (Invoice invoice : allInvoices) {
            if (invoice.getDueDate() == null) continue;
            
            long daysUntilDue = ChronoUnit.DAYS.between(today, invoice.getDueDate());
            
            // Échéance dans les 7 prochains jours
            if (daysUntilDue >= 0 && daysUntilDue <= 7) {
                String alertKey = "upcoming_invoice_" + invoice.getId() + "_" + daysUntilDue;
                
                // Éviter les doublons
                if (!processedAlerts.contains(alertKey)) {
                    createUpcomingInvoiceNotification(invoice, (int) daysUntilDue);
                    processedAlerts.add(alertKey);
                    count++;
                }
            }
        }
        
        logger.info("📅 [SYNC] Factures à échéance proche: {} notifications créées", count);
    }
    
    /**
     * 🚨 Factures en retard
     */
    private void syncOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findByStatus("OVERDUE");
        
        int count = 0;
        
        for (Invoice invoice : overdueInvoices) {
            if (invoice.getDueDate() == null) continue;
            
            long daysOverdue = ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());
            String alertKey = "overdue_invoice_" + invoice.getId() + "_" + daysOverdue;
            
            // Éviter les doublons
            if (!processedAlerts.contains(alertKey)) {
                createOverdueInvoiceNotification(invoice, (int) daysOverdue);
                processedAlerts.add(alertKey);
                count++;
            }
        }
        
        logger.info("🚨 [SYNC] Factures en retard: {} notifications créées", count);
    }
    
    /**
     * ⏰ Conventions expirées
     */
    private void syncExpiredConventions() {
        List<Convention> expiredConventions = conventionRepository.findByStatus("EXPIRED");
        
        int count = 0;
        
        for (Convention convention : expiredConventions) {
            String alertKey = "expired_convention_" + convention.getId();
            
            // Éviter les doublons
            if (!processedAlerts.contains(alertKey)) {
                createExpiredConventionNotification(convention);
                processedAlerts.add(alertKey);
                count++;
            }
        }
        
        logger.info("⏰ [SYNC] Conventions expirées: {} notifications créées", count);
    }
    
    /**
     * 📆 Conventions à échéance proche
     */
    private void syncUpcomingConventions() {
        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        
        List<Convention> activeConventions = conventionRepository.findByStatus("ACTIVE");
        
        int count = 0;
        
        for (Convention convention : activeConventions) {
            if (convention.getEndDate() == null) continue;
            
            long daysUntilEnd = ChronoUnit.DAYS.between(today, convention.getEndDate());
            
            // Échéance dans les 30 prochains jours
            if (daysUntilEnd >= 0 && daysUntilEnd <= 30) {
                String alertKey = "upcoming_convention_" + convention.getId() + "_" + daysUntilEnd;
                
                // Éviter les doublons
                if (!processedAlerts.contains(alertKey)) {
                    createUpcomingConventionNotification(convention, (int) daysUntilEnd);
                    processedAlerts.add(alertKey);
                    count++;
                }
            }
        }
        
        logger.info("📆 [SYNC] Conventions à échéance: {} notifications créées", count);
    }
    
    /**
     * Créer notification pour facture à échéance proche
     */
    private void createUpcomingInvoiceNotification(Invoice invoice, int daysUntilDue) {
        String commercialId = invoice.getCreatedBy();
        if (commercialId == null) return;
        
        String title = String.format("📅 Échéance proche - %d jour(s)", daysUntilDue);
        String message = String.format(
            "Facture %s arrive à échéance dans %d jour(s)\n" +
            "Montant: %.2f TND\n" +
            "Échéance: %s",
            invoice.getReference(),
            daysUntilDue,
            invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0,
            invoice.getDueDate()
        );
        
        String priority = daysUntilDue <= 3 ? "high" : "medium";
        
        inAppNotificationService.createNotification(
            commercialId,
            "invoice_upcoming",
            title,
            message,
            priority,
            "invoice"
        );
        
        logger.debug("✅ Notification créée: Facture {} - {} jours", invoice.getReference(), daysUntilDue);
    }
    
    /**
     * Créer notification pour facture en retard
     */
    private void createOverdueInvoiceNotification(Invoice invoice, int daysOverdue) {
        String commercialId = invoice.getCreatedBy();
        if (commercialId == null) return;
        
        String emoji = daysOverdue > 30 ? "🔥" : "🚨";
        String title = String.format("%s Facture en retard - %d jour(s)", emoji, daysOverdue);
        String message = String.format(
            "Facture %s est en retard de %d jour(s)\n" +
            "Montant: %.2f TND\n" +
            "Échéance dépassée: %s\n\n" +
            "Action requise: Relancer le client",
            invoice.getReference(),
            daysOverdue,
            invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0,
            invoice.getDueDate()
        );
        
        String priority = daysOverdue > 30 ? "urgent" : "high";
        
        inAppNotificationService.createNotification(
            commercialId,
            "invoice_overdue",
            title,
            message,
            priority,
            "invoice"
        );
        
        logger.debug("✅ Notification créée: Facture {} - {} jours retard", invoice.getReference(), daysOverdue);
    }
    
    /**
     * Créer notification pour convention expirée
     */
    private void createExpiredConventionNotification(Convention convention) {
        String commercialId = convention.getCreatedBy();
        if (commercialId == null) return;
        
        String title = "⏰ Convention expirée";
        String message = String.format(
            "Convention %s est arrivée à terme\n" +
            "Client: %s\n" +
            "Date fin: %s\n\n" +
            "Action: Envisager un renouvellement",
            convention.getReference(),
            convention.getClient() != null ? convention.getClient() : "N/A",
            convention.getEndDate()
        );
        
        inAppNotificationService.createNotification(
            commercialId,
            "convention_expired",
            title,
            message,
            "medium",
            "convention"
        );
        
        logger.debug("✅ Notification créée: Convention {} expirée", convention.getReference());
    }
    
    /**
     * Créer notification pour convention à échéance proche
     */
    private void createUpcomingConventionNotification(Convention convention, int daysUntilEnd) {
        String commercialId = convention.getCreatedBy();
        if (commercialId == null) return;
        
        String title = String.format("📆 Convention à renouveler - %d jour(s)", daysUntilEnd);
        String message = String.format(
            "Convention %s arrive à terme dans %d jour(s)\n" +
            "Client: %s\n" +
            "Date fin: %s\n\n" +
            "Action: Préparer le renouvellement",
            convention.getReference(),
            daysUntilEnd,
            convention.getClient() != null ? convention.getClient() : "N/A",
            convention.getEndDate()
        );
        
        String priority = daysUntilEnd <= 7 ? "high" : "medium";
        
        inAppNotificationService.createNotification(
            commercialId,
            "convention_upcoming",
            title,
            message,
            priority,
            "convention"
        );
        
        logger.debug("✅ Notification créée: Convention {} - {} jours", convention.getReference(), daysUntilEnd);
    }
    
    /**
     * Nettoyer le cache des alertes traitées (tous les jours à minuit)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanProcessedAlertsCache() {
        logger.info("🧹 [SYNC] Nettoyage du cache des alertes traitées");
        processedAlerts.clear();
        logger.info("✅ [SYNC] Cache nettoyé");
    }
    
    /**
     * Forcer la synchronisation manuelle (pour tests)
     */
    public Map<String, Object> forceSyncNow() {
        logger.info("🔄 [SYNC] Synchronisation manuelle déclenchée");
        
        // Nettoyer le cache pour forcer la recréation
        processedAlerts.clear();
        
        // Lancer la synchronisation
        syncDashboardAlertsToNotifications();
        
        return Map.of(
            "status", "success",
            "message", "Synchronisation forcée terminée",
            "timestamp", LocalDate.now().toString()
        );
    }
}
