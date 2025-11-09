package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.NotificationLog;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service de rappels automatiques proactifs
 * Envoie des notifications avant les échéances sans intervention utilisateur
 */
@Service
public class ProactiveReminderService {

    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Vérifie les conventions expirant bientôt
     * Exécuté tous les jours à 9h00
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringConventions() {
        System.out.println("🔔 [PROACTIF] Vérification des conventions expirant bientôt...");
        
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);
        LocalDate in3Days = today.plusDays(3);
        LocalDate in1Day = today.plusDays(1);
        
        List<Convention> allConventions = conventionRepository.findAll();
        
        for (Convention convention : allConventions) {
            if (convention.getEndDate() == null) continue;
            
            LocalDate endDate = convention.getEndDate();
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, endDate);
            
            // Rappel 7 jours avant
            if (endDate.equals(in7Days)) {
                sendConventionReminder(convention, 7);
            }
            // Rappel 3 jours avant
            else if (endDate.equals(in3Days)) {
                sendConventionReminder(convention, 3);
            }
            // Rappel 1 jour avant
            else if (endDate.equals(in1Day)) {
                sendConventionReminder(convention, 1);
            }
            // Alerte : déjà expiré
            else if (endDate.isBefore(today) && "ACTIVE".equals(convention.getStatus())) {
                sendConventionExpiredAlert(convention);
            }
        }
        
        System.out.println("✅ [PROACTIF] Vérification terminée");
    }
    
    /**
     * Vérifie les factures en retard
     * Exécuté tous les jours à 10h00
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void checkOverdueInvoices() {
        System.out.println("🔔 [PROACTIF] Vérification des factures en retard...");
        
        LocalDate today = LocalDate.now();
        List<Invoice> allInvoices = invoiceRepository.findAll();
        
        for (Invoice invoice : allInvoices) {
            if (invoice.getDueDate() == null) continue;
            if ("PAID".equals(invoice.getStatus())) continue;
            
            LocalDate dueDate = invoice.getDueDate();
            
            // Facture en retard
            if (dueDate.isBefore(today)) {
                long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
                sendOverdueInvoiceAlert(invoice, (int) daysOverdue);
                
                // Mettre à jour le statut
                if (!"OVERDUE".equals(invoice.getStatus())) {
                    invoice.setStatus("OVERDUE");
                    invoiceRepository.save(invoice);
                }
            }
            // Rappel 3 jours avant échéance
            else if (dueDate.equals(today.plusDays(3))) {
                sendInvoiceDueReminder(invoice, 3);
            }
            // Rappel 1 jour avant échéance
            else if (dueDate.equals(today.plusDays(1))) {
                sendInvoiceDueReminder(invoice, 1);
            }
        }
        
        System.out.println("✅ [PROACTIF] Vérification terminée");
    }
    
    /**
     * Génère un rapport quotidien
     * Exécuté tous les jours à 8h00
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReport() {
        System.out.println("📊 [PROACTIF] Génération du rapport quotidien...");
        
        LocalDate today = LocalDate.now();
        
        // Compter les conventions expirant dans 7 jours
        long conventionsExpiringSoon = conventionRepository.findAll().stream()
            .filter(c -> c.getEndDate() != null)
            .filter(c -> {
                long days = ChronoUnit.DAYS.between(today, c.getEndDate());
                return days > 0 && days <= 7;
            })
            .count();
        
        // Compter les factures en retard
        long overdueInvoices = invoiceRepository.findAll().stream()
            .filter(i -> i.getDueDate() != null)
            .filter(i -> !"PAID".equals(i.getStatus()))
            .filter(i -> i.getDueDate().isBefore(today))
            .count();
        
        // Compter les factures à échéance aujourd'hui
        long invoicesDueToday = invoiceRepository.findAll().stream()
            .filter(i -> i.getDueDate() != null)
            .filter(i -> !"PAID".equals(i.getStatus()))
            .filter(i -> i.getDueDate().equals(today))
            .count();
        
        // Envoyer le rapport si nécessaire
        if (conventionsExpiringSoon > 0 || overdueInvoices > 0 || invoicesDueToday > 0) {
            sendDailyReportNotification(conventionsExpiringSoon, overdueInvoices, invoicesDueToday);
        }
        
        System.out.println("✅ [PROACTIF] Rapport envoyé");
    }
    
    /**
     * Envoie un rappel pour une convention
     */
    private void sendConventionReminder(Convention convention, int daysBeforeExpiry) {
        NotificationLog notification = new NotificationLog();
        notification.setType("SYSTEM");
        notification.setChannel("IN_APP");
        notification.setSubject("⚠️ Convention expire dans " + daysBeforeExpiry + " jour(s)");
        notification.setMessage(
            "La convention " + convention.getReference() + 
            " (" + convention.getTitle() + ") expire le " + convention.getEndDate() + 
            ".\n\nIl reste " + daysBeforeExpiry + " jour(s) pour la renouveler."
        );
        notification.setRecipientId(convention.getCreatedBy());
        notification.setStatus("PENDING");
        notification.setSentAt(LocalDateTime.now());
        notification.setConventionId(convention.getId());
        
        notificationService.createAndSendNotification(notification);
        
        System.out.println("📧 [PROACTIF] Rappel envoyé pour convention " + convention.getReference() + 
                          " (" + daysBeforeExpiry + " jours)");
    }
    
    /**
     * Envoie une alerte pour une convention expirée
     */
    private void sendConventionExpiredAlert(Convention convention) {
        NotificationLog notification = new NotificationLog();
        notification.setType("SYSTEM");
        notification.setChannel("IN_APP");
        notification.setSubject("🚨 Convention EXPIRÉE");
        notification.setMessage(
            "ALERTE : La convention " + convention.getReference() + 
            " (" + convention.getTitle() + ") a expiré le " + convention.getEndDate() + 
            ".\n\nAction requise immédiatement !"
        );
        notification.setRecipientId(convention.getCreatedBy());
        notification.setStatus("PENDING");
        notification.setSentAt(LocalDateTime.now());
        notification.setConventionId(convention.getId());
        
        notificationService.createAndSendNotification(notification);
        
        System.out.println("🚨 [PROACTIF] Alerte expiration envoyée pour convention " + convention.getReference());
    }
    
    /**
     * Envoie un rappel pour une facture
     */
    private void sendInvoiceDueReminder(Invoice invoice, int daysBeforeDue) {
        NotificationLog notification = new NotificationLog();
        notification.setType("SYSTEM");
        notification.setChannel("IN_APP");
        notification.setSubject("💰 Facture à échéance dans " + daysBeforeDue + " jour(s)");
        notification.setMessage(
            "La facture " + invoice.getInvoiceNumber() + 
            " (Montant: " + invoice.getAmount() + " DT) arrive à échéance le " + invoice.getDueDate() + 
            ".\n\nIl reste " + daysBeforeDue + " jour(s) pour effectuer le paiement."
        );
        notification.setRecipientId(invoice.getCreatedBy());
        notification.setStatus("PENDING");
        notification.setSentAt(LocalDateTime.now());
        notification.setInvoiceId(invoice.getId());
        
        notificationService.createAndSendNotification(notification);
        
        System.out.println("📧 [PROACTIF] Rappel envoyé pour facture " + invoice.getInvoiceNumber() + 
                          " (" + daysBeforeDue + " jours)");
    }
    
    /**
     * Envoie une alerte pour une facture en retard
     */
    private void sendOverdueInvoiceAlert(Invoice invoice, int daysOverdue) {
        NotificationLog notification = new NotificationLog();
        notification.setType("SYSTEM");
        notification.setChannel("IN_APP");
        notification.setSubject("🚨 Facture EN RETARD de " + daysOverdue + " jour(s)");
        notification.setMessage(
            "ALERTE : La facture " + invoice.getInvoiceNumber() + 
            " (Montant: " + invoice.getAmount() + " DT) est en retard de " + daysOverdue + " jour(s).\n\n" +
            "Échéance dépassée : " + invoice.getDueDate() + 
            "\n\nAction urgente requise !"
        );
        notification.setRecipientId(invoice.getCreatedBy());
        notification.setStatus("PENDING");
        notification.setSentAt(LocalDateTime.now());
        notification.setInvoiceId(invoice.getId());
        
        notificationService.createAndSendNotification(notification);
        
        System.out.println("🚨 [PROACTIF] Alerte retard envoyée pour facture " + invoice.getInvoiceNumber() + 
                          " (" + daysOverdue + " jours)");
    }
    
    /**
     * Envoie le rapport quotidien
     */
    private void sendDailyReportNotification(long conventionsExpiring, long overdueInvoices, long invoicesDueToday) {
        NotificationLog notification = new NotificationLog();
        notification.setType("SYSTEM");
        notification.setChannel("IN_APP");
        notification.setSubject("📊 Rapport Quotidien - " + LocalDate.now());
        notification.setMessage(
            "**Rapport du jour :**\n\n" +
            "⚠️ Conventions expirant dans 7 jours : " + conventionsExpiring + "\n" +
            "🚨 Factures en retard : " + overdueInvoices + "\n" +
            "💰 Factures à échéance aujourd'hui : " + invoicesDueToday + "\n\n" +
            "Consultez le tableau de bord pour plus de détails."
        );
        notification.setRecipientId("admin"); // Envoyer à l'admin ou tous les décideurs
        notification.setStatus("PENDING");
        notification.setSentAt(LocalDateTime.now());
        
        notificationService.createAndSendNotification(notification);
        
        System.out.println("📊 [PROACTIF] Rapport quotidien envoyé");
    }
}
