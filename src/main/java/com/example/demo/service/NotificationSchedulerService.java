package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.User;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.repository.NotificationSettingsRepository;
import com.example.demo.model.NotificationSettings;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service de planification des notifications selon le cahier des charges
 * - Notifications préventives (X jours avant échéance)
 * - Alertes d'échéance dépassée
 * - Confirmation après mise à jour statut
 */
@Service
public class NotificationSchedulerService {

    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RealTimeNotificationService realTimeNotificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;

    @Value("${notification.reminder.days:7,3,1}")
    private String reminderDaysConfig;
    
    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${notification.sms.enabled:true}")
    private boolean smsEnabled;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    private void loadDynamicSettings() {
        try {
            NotificationSettings settings = notificationSettingsRepository.findById("global").orElse(null);
            if (settings != null) {
                // reminder days override
                if (settings.getReminderDays() != null && !settings.getReminderDays().isEmpty()) {
                    this.reminderDaysConfig = String.join(",", settings.getReminderDays().stream().map(String::valueOf).toList());
                }
                this.emailEnabled = settings.isEmailEnabled();
                this.smsEnabled = settings.isSmsEnabled();
                // quiet hours
                if (settings.isQuietHoursEnabled()) {
                    if (isWithinQuietHours(settings)) {
                        System.out.println("⏸️ [SCHEDULER] Quiet hours active - notifications suppressed for this run");
                        // Throwing a runtime here would stop entire run; instead return early
                        throw new RuntimeException("QUIET_HOURS_ACTIVE");
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private boolean isWithinQuietHours(NotificationSettings settings) {
        try {
            String start = settings.getQuietHoursStart();
            String end = settings.getQuietHoursEnd();
            if (start == null || end == null) return false;
            java.time.LocalTime now = java.time.LocalTime.now();
            java.time.LocalTime s = java.time.LocalTime.parse(start);
            java.time.LocalTime e = java.time.LocalTime.parse(end);
            if (s.isBefore(e)) {
                return !now.isBefore(s) && !now.isAfter(e);
            } else { // spans midnight
                return now.isAfter(s) || now.isBefore(e);
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Scheduler principal - s'exécute chaque jour à 9h00
     * Point 6 du cahier des charges : "Chaque jour, un processus planifié analyse les factures/conventions"
     */
    @Scheduled(cron = "0 0 9 * * *") // Tous les jours à 9h00
    public void checkDueDatesAndSendNotifications() {
        System.out.println("🔔 [SCHEDULER] Début de la vérification des échéances - " + LocalDate.now());
        try {
            loadDynamicSettings();
        } catch (RuntimeException ex) {
            if ("QUIET_HOURS_ACTIVE".equals(ex.getMessage())) {
                return; // skip this run silently
            }
            throw ex;
        }
        
        try {
            // 1. Notifications préventives pour les conventions
            checkConventionDueDates();
            
            // 2. Notifications préventives pour les factures
            checkInvoiceDueDates();
            
            // 3. Alertes d'échéance dépassée
            checkOverdueItems();
            
            System.out.println("✅ [SCHEDULER] Vérification des échéances terminée");
            
        } catch (Exception e) {
            System.err.println("❌ [SCHEDULER] Erreur lors de la vérification des échéances: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérification des échéances de conventions
     * Point 3 : Notifications préventives → envoi X jours avant l'échéance
     */
    private void checkConventionDueDates() {
        List<Integer> reminderDays = Arrays.stream(reminderDaysConfig.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        
        LocalDate today = LocalDate.now();
        
        for (Integer days : reminderDays) {
            LocalDate targetDate = today.plusDays(days);
            
            // Chercher les conventions avec échéances à la date cible
            List<Convention> conventions = conventionRepository.findByEcheancesContaining(targetDate);
            
            for (Convention convention : conventions) {
                sendConventionReminder(convention, days, targetDate);
            }
        }
    }

    /**
     * Vérification des échéances de factures
     */
    private void checkInvoiceDueDates() {
        List<Integer> reminderDays = Arrays.stream(reminderDaysConfig.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        
        LocalDate today = LocalDate.now();
        
        for (Integer days : reminderDays) {
            LocalDate targetDate = today.plusDays(days);
            
            // Chercher les factures avec échéance à la date cible
            List<Invoice> invoices = invoiceRepository.findByDueDate(targetDate);
            
            for (Invoice invoice : invoices) {
                sendInvoiceReminder(invoice, days, targetDate);
            }
        }
    }

    /**
     * Vérification des échéances dépassées
     * Point 3 : Alerte d'échéance dépassée → si facture non réglée à temps
     */
    private void checkOverdueItems() {
        LocalDate today = LocalDate.now();
        
        // Factures en retard
        List<Invoice> overdueInvoices = invoiceRepository.findByDueDateBeforeAndStatusNot(today, "PAID");
        
        for (Invoice invoice : overdueInvoices) {
            sendOverdueNotification(invoice);
        }
        
        // Conventions en retard (basé sur les échéances)
        List<Convention> overdueConventions = conventionRepository.findByEcheancesContainingAndStatusNot(today.minusDays(1), "COMPLETED");
        
        for (Convention convention : overdueConventions) {
            sendOverdueConventionNotification(convention);
        }
    }

    /**
     * Envoi de rappel pour convention
     */
    private void sendConventionReminder(Convention convention, int daysBefore, LocalDate dueDate) {
        try {
            User commercial = userRepository.findById(convention.getCreatedBy()).orElse(null);
            if (commercial == null) return;

            // Notification interne (Point 4 : Notification interne frontend)
            NotificationDTO notification = new NotificationDTO();
            notification.setType("warning");
            notification.setTitle("⏰ Rappel Convention");
            notification.setMessage(String.format("Convention %s - Échéance dans %d jour(s) (%s)", 
                convention.getReference(), daysBefore, dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            notification.setPriority(daysBefore == 1 ? "high" : "medium");
            notification.setCategory("convention");
            notification.setUserId(commercial.getId());
            notification.setSource("NotificationScheduler");

            realTimeNotificationService.createNotification(notification);

            // Email (Point 4 : Email SMTP)
            if (emailEnabled && commercial.getEmail() != null) {
                Map<String, String> variables = new HashMap<>();
                variables.put("commercialName", commercial.getName() != null ? commercial.getName() : commercial.getUsername());
                variables.put("conventionReference", convention.getReference());
                variables.put("conventionTitle", convention.getTitle());
                variables.put("dueDate", dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                variables.put("daysBefore", String.valueOf(daysBefore));
                variables.put("amount", String.valueOf(convention.getAmount()));
                
                emailService.sendConventionReminderEmail(commercial.getEmail(), variables);
            }

            // SMS (Point 4 : SMS alerte rapide)
            if (smsEnabled && commercial.getPhoneNumber() != null) {
                Map<String, String> variables = new HashMap<>();
                variables.put("conventionReference", convention.getReference());
                variables.put("dueDate", dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                variables.put("daysBefore", String.valueOf(daysBefore));
                
                smsService.sendSmsWithTemplate(commercial.getPhoneNumber(), "convention_reminder", variables);
            }

            System.out.println("🔔 Rappel convention envoyé: " + convention.getReference() + " (J-" + daysBefore + ")");

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi rappel convention " + convention.getReference() + ": " + e.getMessage());
        }
    }

    /**
     * Envoi de rappel pour facture
     */
    private void sendInvoiceReminder(Invoice invoice, int daysBefore, LocalDate dueDate) {
        try {
            User commercial = userRepository.findById(invoice.getCreatedBy()).orElse(null);
            if (commercial == null) return;

            // Notification interne
            NotificationDTO notification = new NotificationDTO();
            notification.setType("warning");
            notification.setTitle("💰 Rappel Facture");
            notification.setMessage(String.format("Facture %s - Échéance dans %d jour(s) (%s) - Montant: %s€", 
                invoice.getInvoiceNumber(), daysBefore, dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), invoice.getAmount()));
            notification.setPriority(daysBefore == 1 ? "high" : "medium");
            notification.setCategory("invoice");
            notification.setUserId(commercial.getId());
            notification.setSource("NotificationScheduler");

            realTimeNotificationService.createNotification(notification);

            // Email
            if (emailEnabled && commercial.getEmail() != null) {
                Map<String, String> variables = new HashMap<>();
                variables.put("commercialName", commercial.getName() != null ? commercial.getName() : commercial.getUsername());
                variables.put("invoiceNumber", invoice.getInvoiceNumber());
                variables.put("dueDate", dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                variables.put("daysBefore", String.valueOf(daysBefore));
                variables.put("amount", String.valueOf(invoice.getAmount()));
                variables.put("clientName", invoice.getClientEmail() != null ? invoice.getClientEmail() : "Client");
                
                emailService.sendInvoiceReminderEmail(commercial.getEmail(), variables);
            }

            // SMS
            if (smsEnabled && commercial.getPhoneNumber() != null) {
                Map<String, String> variables = new HashMap<>();
                variables.put("invoiceNumber", invoice.getInvoiceNumber());
                variables.put("dueDate", dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                variables.put("daysBefore", String.valueOf(daysBefore));
                variables.put("amount", String.valueOf(invoice.getAmount()));
                
                smsService.sendSmsWithTemplate(commercial.getPhoneNumber(), "invoice_reminder", variables);
            }

            System.out.println("🔔 Rappel facture envoyé: " + invoice.getInvoiceNumber() + " (J-" + daysBefore + ")");

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi rappel facture " + invoice.getInvoiceNumber() + ": " + e.getMessage());
        }
    }

    /**
     * Envoi d'alerte pour facture en retard
     */
    private void sendOverdueNotification(Invoice invoice) {
        try {
            User commercial = userRepository.findById(invoice.getCreatedBy()).orElse(null);
            if (commercial == null) return;

            // Notification interne
            NotificationDTO notification = new NotificationDTO();
            notification.setType("error");
            notification.setTitle("🚨 Facture en Retard");
            notification.setMessage(String.format("Facture %s - ÉCHÉANCE DÉPASSÉE depuis %d jour(s) - Montant: %s€", 
                invoice.getInvoiceNumber(), 
                LocalDate.now().toEpochDay() - invoice.getDueDate().toEpochDay(),
                invoice.getAmount()));
            notification.setPriority("high");
            notification.setCategory("invoice");
            notification.setUserId(commercial.getId());
            notification.setSource("NotificationScheduler");

            realTimeNotificationService.createNotification(notification);

            // Email d'urgence
            if (emailEnabled && commercial.getEmail() != null) {
                Map<String, String> variables = new HashMap<>();
                variables.put("commercialName", commercial.getName() != null ? commercial.getName() : commercial.getUsername());
                variables.put("invoiceNumber", invoice.getInvoiceNumber());
                variables.put("dueDate", invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                variables.put("amount", String.valueOf(invoice.getAmount()));
                variables.put("clientName", invoice.getClientEmail() != null ? invoice.getClientEmail() : "Client");
                variables.put("daysOverdue", String.valueOf(LocalDate.now().toEpochDay() - invoice.getDueDate().toEpochDay()));
                
                emailService.sendOverdueInvoiceEmail(commercial.getEmail(), variables);
            }

            // SMS d'urgence
            if (smsEnabled && commercial.getPhoneNumber() != null) {
                Map<String, String> variables = new HashMap<>();
                variables.put("invoiceNumber", invoice.getInvoiceNumber());
                variables.put("amount", String.valueOf(invoice.getAmount()));
                variables.put("daysOverdue", String.valueOf(LocalDate.now().toEpochDay() - invoice.getDueDate().toEpochDay()));
                
                smsService.sendSmsWithTemplate(commercial.getPhoneNumber(), "overdue", variables);
            }

            System.out.println("🚨 Alerte facture en retard: " + invoice.getInvoiceNumber());

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi alerte facture en retard " + invoice.getInvoiceNumber() + ": " + e.getMessage());
        }
    }

    /**
     * Envoi d'alerte pour convention en retard
     */
    private void sendOverdueConventionNotification(Convention convention) {
        try {
            User commercial = userRepository.findById(convention.getCreatedBy()).orElse(null);
            if (commercial == null) return;

            // Notification interne
            NotificationDTO notification = new NotificationDTO();
            notification.setType("error");
            notification.setTitle("🚨 Convention en Retard");
            notification.setMessage(String.format("Convention %s - ÉCHÉANCE DÉPASSÉE - Montant: %s€", 
                convention.getReference(), convention.getAmount()));
            notification.setPriority("high");
            notification.setCategory("convention");
            notification.setUserId(commercial.getId());
            notification.setSource("NotificationScheduler");

            realTimeNotificationService.createNotification(notification);

            System.out.println("🚨 Alerte convention en retard: " + convention.getReference());

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi alerte convention en retard " + convention.getReference() + ": " + e.getMessage());
        }
    }

    /**
     * Test manuel du scheduler (pour les tests)
     */
    public void triggerManualCheck() {
        System.out.println("🧪 [TEST MANUEL] Déclenchement manuel du scheduler");
        checkDueDatesAndSendNotifications();
    }
}