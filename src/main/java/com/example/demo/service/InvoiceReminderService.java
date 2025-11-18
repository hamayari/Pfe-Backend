package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.model.User;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service de rappels automatiques pour les factures
 * Envoie des notifications proactives au commercial AVANT et APRÈS l'échéance
 */
@Service
public class InvoiceReminderService {
    
    private static final Logger logger = LoggerFactory.getLogger(InvoiceReminderService.class);
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InAppNotificationService inAppNotificationService;
    
    @Autowired(required = false)
    private EmailService emailService;
    
    @Autowired(required = false)
    private SmsService smsService;
    
    /**
     * 🔔 RAPPEL AUTOMATIQUE : Exécuté tous les jours à 8h00
     * Vérifie les factures et envoie des rappels au commercial
     */
    @Scheduled(cron = "0 0 8 * * *") // Tous les jours à 8h00
    public void sendDailyReminders() {
        logger.info("========================================");
        logger.info("🔔 [RAPPEL AUTO] Vérification quotidienne des factures");
        logger.info("========================================");
        
        try {
            // 1. Rappels AVANT échéance (7 jours, 3 jours, 1 jour)
            sendUpcomingDueReminders();
            
            // 2. Rappels APRÈS échéance (factures en retard)
            sendOverdueReminders();
            
            // 3. Rappels pour factures en attente (PENDING)
            sendPendingInvoiceReminders();
            
            logger.info("✅ [RAPPEL AUTO] Vérification terminée");
            logger.info("========================================");
            
        } catch (Exception e) {
            logger.error("❌ [RAPPEL AUTO] Erreur: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 📅 RAPPEL AVANT ÉCHÉANCE
     * Notifie le commercial 7, 3 et 1 jour(s) avant l'échéance
     */
    private void sendUpcomingDueReminders() {
        logger.info("📅 [RAPPEL] Vérification factures à échéance proche...");
        
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);
        LocalDate in3Days = today.plusDays(3);
        LocalDate in1Day = today.plusDays(1);
        
        // Factures PENDING avec échéance proche
        List<Invoice> allPendingInvoices = invoiceRepository.findByStatus("PENDING");
        
        int count7Days = 0;
        int count3Days = 0;
        int count1Day = 0;
        
        for (Invoice invoice : allPendingInvoices) {
            LocalDate dueDate = invoice.getDueDate();
            if (dueDate == null) continue;
            
            // Rappel 7 jours avant
            if (dueDate.equals(in7Days)) {
                sendReminderToCommercial(invoice, 7, "UPCOMING");
                count7Days++;
            }
            // Rappel 3 jours avant
            else if (dueDate.equals(in3Days)) {
                sendReminderToCommercial(invoice, 3, "UPCOMING");
                count3Days++;
            }
            // Rappel 1 jour avant
            else if (dueDate.equals(in1Day)) {
                sendReminderToCommercial(invoice, 1, "UPCOMING");
                count1Day++;
            }
        }
        
        logger.info("✅ [RAPPEL] Factures à échéance:");
        logger.info("   - Dans 7 jours: {} rappels envoyés", count7Days);
        logger.info("   - Dans 3 jours: {} rappels envoyés", count3Days);
        logger.info("   - Dans 1 jour: {} rappels envoyés", count1Day);
    }
    
    /**
     * 🚨 RAPPEL APRÈS ÉCHÉANCE
     * Notifie le commercial pour les factures en retard
     */
    private void sendOverdueReminders() {
        logger.info("🚨 [RAPPEL] Vérification factures en retard...");
        
        List<Invoice> overdueInvoices = invoiceRepository.findByStatus("OVERDUE");
        
        int countCritical = 0; // > 30 jours
        int countHigh = 0;     // 15-30 jours
        int countMedium = 0;   // 1-14 jours
        
        for (Invoice invoice : overdueInvoices) {
            LocalDate dueDate = invoice.getDueDate();
            if (dueDate == null) continue;
            
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            
            if (daysOverdue > 30) {
                sendReminderToCommercial(invoice, (int) daysOverdue, "CRITICAL");
                countCritical++;
            } else if (daysOverdue >= 15) {
                sendReminderToCommercial(invoice, (int) daysOverdue, "HIGH");
                countHigh++;
            } else {
                sendReminderToCommercial(invoice, (int) daysOverdue, "MEDIUM");
                countMedium++;
            }
        }
        
        logger.info("✅ [RAPPEL] Factures en retard:");
        logger.info("   - Critiques (>30j): {} rappels", countCritical);
        logger.info("   - Élevées (15-30j): {} rappels", countHigh);
        logger.info("   - Moyennes (1-14j): {} rappels", countMedium);
    }
    
    /**
     * ⏳ RAPPEL FACTURES EN ATTENTE
     * Notifie le commercial pour les factures PENDING anciennes (> 30 jours)
     */
    private void sendPendingInvoiceReminders() {
        logger.info("⏳ [RAPPEL] Vérification factures en attente...");
        
        List<Invoice> pendingInvoices = invoiceRepository.findByStatus("PENDING");
        LocalDate today = LocalDate.now();
        
        int count = 0;
        
        for (Invoice invoice : pendingInvoices) {
            LocalDate issueDate = invoice.getIssueDate();
            if (issueDate == null) continue;
            
            long daysSinceIssue = ChronoUnit.DAYS.between(issueDate, today);
            
            // Rappel si facture en attente depuis plus de 30 jours
            if (daysSinceIssue > 30) {
                sendPendingReminderToCommercial(invoice, (int) daysSinceIssue);
                count++;
            }
        }
        
        logger.info("✅ [RAPPEL] Factures en attente: {} rappels envoyés", count);
    }
    
    /**
     * 📧 ENVOYER RAPPEL AU COMMERCIAL
     */
    private void sendReminderToCommercial(Invoice invoice, int days, String type) {
        try {
            // Trouver le commercial qui a créé la facture
            String commercialId = invoice.getCreatedBy();
            if (commercialId == null) {
                logger.warn("⚠️ Facture {} sans créateur", invoice.getReference());
                return;
            }
            
            User commercial = userRepository.findById(commercialId).orElse(null);
            if (commercial == null) {
                commercial = userRepository.findByUsername(commercialId).orElse(null);
            }
            
            if (commercial == null) {
                logger.warn("⚠️ Commercial {} introuvable", commercialId);
                return;
            }
            
            // Préparer le message selon le type
            String title;
            String message;
            String priority;
            String emoji;
            
            switch (type) {
                case "UPCOMING":
                    emoji = "📅";
                    title = String.format("Facture à échéance dans %d jour(s)", days);
                    message = String.format(
                        "La facture %s arrive à échéance dans %d jour(s).\n" +
                        "Montant: %.2f TND\n" +
                        "Client: %s\n\n" +
                        "💡 Action recommandée: Contactez le client pour confirmer le paiement.",
                        invoice.getReference(),
                        days,
                        invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0,
                        invoice.getClientEmail() != null ? invoice.getClientEmail() : "N/A"
                    );
                    priority = days <= 1 ? "high" : "medium";
                    break;
                    
                case "CRITICAL":
                    emoji = "🔥";
                    title = String.format("URGENT: Facture en retard de %d jours", days);
                    message = String.format(
                        "La facture %s est en retard de %d jours !\n" +
                        "Montant: %.2f TND\n" +
                        "Client: %s\n\n" +
                        "🚨 Action URGENTE requise:\n" +
                        "1. Contact immédiat du client\n" +
                        "2. Mise en demeure si nécessaire\n" +
                        "3. Envisager procédure de recouvrement",
                        invoice.getReference(),
                        days,
                        invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0,
                        invoice.getClientEmail() != null ? invoice.getClientEmail() : "N/A"
                    );
                    priority = "urgent";
                    break;
                    
                case "HIGH":
                    emoji = "🚨";
                    title = String.format("Facture en retard de %d jours", days);
                    message = String.format(
                        "La facture %s est en retard de %d jours.\n" +
                        "Montant: %.2f TND\n" +
                        "Client: %s\n\n" +
                        "⚠️ Actions recommandées:\n" +
                        "1. Relancer le client par téléphone\n" +
                        "2. Envoyer rappel formel par email\n" +
                        "3. Proposer échéancier de paiement",
                        invoice.getReference(),
                        days,
                        invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0,
                        invoice.getClientEmail() != null ? invoice.getClientEmail() : "N/A"
                    );
                    priority = "high";
                    break;
                    
                default: // MEDIUM
                    emoji = "⚠️";
                    title = String.format("Facture en retard de %d jours", days);
                    message = String.format(
                        "La facture %s est en retard de %d jours.\n" +
                        "Montant: %.2f TND\n" +
                        "Client: %s\n\n" +
                        "💡 Actions recommandées:\n" +
                        "1. Envoyer rappel amical au client\n" +
                        "2. Vérifier si paiement en cours\n" +
                        "3. Confirmer coordonnées bancaires",
                        invoice.getReference(),
                        days,
                        invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0,
                        invoice.getClientEmail() != null ? invoice.getClientEmail() : "N/A"
                    );
                    priority = "medium";
            }
            
            // 1. Notification In-App
            inAppNotificationService.createNotification(
                commercial.getId(),
                "invoice_reminder",
                emoji + " " + title,
                message,
                priority,
                "invoice"
            );
            
            // 2. Email (si service disponible)
            if (emailService != null && commercial.getEmail() != null) {
                try {
                    emailService.sendEmail(
                        commercial.getEmail(),
                        emoji + " " + title,
                        message
                    );
                    logger.info("📧 Email envoyé à {}", commercial.getEmail());
                } catch (Exception e) {
                    logger.error("❌ Erreur envoi email: {}", e.getMessage());
                }
            }
            
            // 3. SMS (uniquement pour CRITICAL et URGENT)
            if (smsService != null && 
                commercial.getPhoneNumber() != null && 
                ("CRITICAL".equals(type) || priority.equals("urgent"))) {
                try {
                    String smsMessage = String.format(
                        "%s Facture %s en retard de %d jours. Montant: %.2f TND. Action urgente requise!",
                        emoji,
                        invoice.getReference(),
                        days,
                        invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0
                    );
                    
                    Map<String, String> variables = new HashMap<>();
                    variables.put("message", smsMessage);
                    
                    smsService.sendSmsWithTemplate(
                        commercial.getPhoneNumber(),
                        "invoice_reminder",
                        variables
                    );
                    logger.info("📱 SMS envoyé à {}", commercial.getPhoneNumber());
                } catch (Exception e) {
                    logger.error("❌ Erreur envoi SMS: {}", e.getMessage());
                }
            }
            
            logger.info("✅ Rappel envoyé à {} pour facture {}", 
                       commercial.getName(), invoice.getReference());
            
        } catch (Exception e) {
            logger.error("❌ Erreur envoi rappel: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 📧 ENVOYER RAPPEL FACTURE EN ATTENTE
     */
    private void sendPendingReminderToCommercial(Invoice invoice, int daysSinceIssue) {
        try {
            String commercialId = invoice.getCreatedBy();
            if (commercialId == null) return;
            
            User commercial = userRepository.findById(commercialId).orElse(null);
            if (commercial == null) {
                commercial = userRepository.findByUsername(commercialId).orElse(null);
            }
            if (commercial == null) return;
            
            String title = String.format("⏳ Facture en attente depuis %d jours", daysSinceIssue);
            String message = String.format(
                "La facture %s est en attente depuis %d jours.\n" +
                "Montant: %.2f TND\n" +
                "Client: %s\n\n" +
                "💡 Actions recommandées:\n" +
                "1. Vérifier le statut avec le client\n" +
                "2. Relancer si nécessaire\n" +
                "3. Mettre à jour le statut dans le système",
                invoice.getReference(),
                daysSinceIssue,
                invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0,
                invoice.getClientEmail() != null ? invoice.getClientEmail() : "N/A"
            );
            
            // Notification In-App
            inAppNotificationService.createNotification(
                commercial.getId(),
                "invoice_pending",
                title,
                message,
                "medium",
                "invoice"
            );
            
            logger.info("✅ Rappel PENDING envoyé à {} pour facture {}", 
                       commercial.getName(), invoice.getReference());
            
        } catch (Exception e) {
            logger.error("❌ Erreur rappel PENDING: {}", e.getMessage());
        }
    }
    
    /**
     * 🧪 MÉTHODE DE TEST MANUELLE
     * Permet de déclencher les rappels manuellement pour tester
     */
    public Map<String, Object> sendTestReminders() {
        logger.info("🧪 [TEST] Déclenchement manuel des rappels");
        
        sendDailyReminders();
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Rappels de test envoyés");
        result.put("timestamp", LocalDate.now().toString());
        
        return result;
    }
}
