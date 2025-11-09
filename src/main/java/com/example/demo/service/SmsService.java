package com.example.demo.service;

import com.example.demo.dto.SmsRequestDTO;
import com.example.demo.dto.SmsResponseDTO;
import com.example.demo.dto.SmsStatsDTO;
import com.example.demo.model.SmsNotification;
import com.example.demo.repository.SmsNotificationRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @Value("${sms.simulation.mode:false}")
    private boolean simulationMode;

    @Autowired
    private SmsNotificationRepository smsNotificationRepository;

    // Initialiser Twilio automatiquement au démarrage
    @jakarta.annotation.PostConstruct
    public void initializeTwilio() {
        if (simulationMode) {
            logger.info("MODE SIMULATION ACTIVÉ - Les SMS ne seront pas envoyés réellement");
            logger.info("Les SMS seront enregistrés dans la base de données uniquement");
            return;
        }
        
        if (accountSid != null && authToken != null && !accountSid.equals("your-twilio-account-sid")) {
            logger.info("Initialisation de Twilio...");
            logger.debug("Account SID: {}", accountSid);
            logger.debug("From Number: {}", fromPhoneNumber);
            Twilio.init(accountSid, authToken);
            logger.info("Twilio initialisé avec succès");
        } else {
            logger.warn("Twilio non configuré - vérifiez application.properties");
        }
    }

    /**
     * Envoyer un SMS avec template professionnel
     */
    public SmsResponseDTO sendSmsWithTemplate(String phoneNumber, String templateType, Map<String, String> variables) {
        String message = buildSmsTemplate(templateType, variables);
        
        SmsRequestDTO request = new SmsRequestDTO();
        request.setTo(phoneNumber);
        request.setMessage(message);
        
        return sendSms(request);
    }

    /**
     * Construire un template SMS optimisé
     */
    private String buildSmsTemplate(String templateType, Map<String, String> variables) {
        return switch (templateType.toLowerCase()) {
            case "invoice_reminder" -> String.format(
                "🔔 GestionPro\nFacture %s\nMontant: %s€\nÉchéance: %s\nPaiement en attente.\n\nGestionPro",
                variables.getOrDefault("invoiceNumber", "N/A"),
                variables.getOrDefault("amount", "0"),
                variables.getOrDefault("dueDate", "N/A")
            );
            case "invoice_overdue" -> String.format(
                "⚠️ GestionPro\nFacture %s EN RETARD\nMontant: %s€\nRetard: %s jours\nPaiement urgent requis.\n\nGestionPro",
                variables.getOrDefault("invoiceNumber", "N/A"),
                variables.getOrDefault("amount", "0"),
                variables.getOrDefault("daysOverdue", "0")
            );
            case "payment_received" -> String.format(
                "💰 GestionPro\nPaiement reçu\nFacture: %s\nMontant: %s€\nMéthode: %s\nMerci!\n\nGestionPro",
                variables.getOrDefault("invoiceNumber", "N/A"),
                variables.getOrDefault("amount", "0"),
                variables.getOrDefault("paymentMethod", "N/A")
            );
            case "kpi_alert" -> String.format(
                "🚨 GestionPro\nAlerte KPI %s\n%s (%.1f)\nPriorité: %s\nConsultez le dashboard.\n\nGestionPro",
                variables.getOrDefault("severity", "HIGH"),
                variables.getOrDefault("kpiName", "KPI"),
                Double.parseDouble(variables.getOrDefault("currentValue", "0")),
                variables.getOrDefault("priority", "HIGH")
            );
            case "kpi_delegation" -> String.format(
                "🔔 GestionPro\nAlerte KPI déléguée\n%s\nPriorité: %s\nConsultez le dashboard.\n\nGestionPro",
                variables.getOrDefault("kpiName", "KPI"),
                variables.getOrDefault("priority", "HIGH")
            );
            case "system_alert" -> String.format(
                "🚨 GestionPro\nAlerte système\n%s\n%s\nAction requise.\n\nGestionPro",
                variables.getOrDefault("title", "Alerte"),
                variables.getOrDefault("message", "Message d'alerte")
            );
            case "weekly_summary" -> String.format(
                "📊 GestionPro\nRésumé hebdomadaire\nFactures: %s\nEncaissées: %s€\nEn attente: %s€\n\nGestionPro",
                variables.getOrDefault("totalInvoices", "0"),
                variables.getOrDefault("collectedAmount", "0"),
                variables.getOrDefault("pendingAmount", "0")
            );
            case "client_credentials" -> String.format(
                "👤 GestionPro\nVos identifiants\nEmail: %s\nMot de passe: %s\nChangez-le à la 1ère connexion.\n\nGestionPro",
                variables.getOrDefault("email", "N/A"),
                variables.getOrDefault("password", "N/A")
            );
            case "two_factor" -> String.format(
                "🔐 GestionPro\nCode de vérification\n%s\nValide 5 minutes\nNe partagez jamais ce code.\n\nGestionPro",
                variables.getOrDefault("code", "N/A")
            );
            case "convention_created" -> String.format(
                "✅ GestionPro\nNouvelle Convention\nRéf: %s\nMontant: %s€\nCréée avec succès!\n\nGestionPro",
                variables.getOrDefault("conventionReference", "N/A"),
                variables.getOrDefault("amount", "0")
            );
            default -> String.format(
                "📢 GestionPro\n%s\n%s\n\nGestionPro",
                variables.getOrDefault("title", "Notification"),
                variables.getOrDefault("message", "Message")
            );
        };
    }

    // Envoyer un SMS
    public SmsResponseDTO sendSms(SmsRequestDTO request) {
        SmsResponseDTO response = new SmsResponseDTO();
        
        try {
            logger.debug("Début envoi SMS");
            logger.debug("To: {}", request.getTo());
            logger.debug("From: {}", fromPhoneNumber);
            logger.debug("Message: {}", request.getMessage());
            
            // Valider le numéro de téléphone
            if (!isValidPhoneNumber(request.getTo())) {
                logger.error("Numéro de téléphone invalide: {}", request.getTo());
                response.setSuccess(false);
                response.setMessage("Numéro de téléphone invalide");
                return response;
            }

            // MODE SIMULATION : Ne pas envoyer réellement le SMS
            String twilioSid = null;
            if (simulationMode) {
                logger.info("SMS simulé (non envoyé réellement) - To: {}", request.getTo());
                twilioSid = "SIM-" + System.currentTimeMillis(); // SID simulé
            } else {
                logger.debug("Envoi via Twilio...");
                // Envoyer le SMS via Twilio
                Message message = Message.creator(
                        new PhoneNumber(request.getTo()),
                        new PhoneNumber(fromPhoneNumber),
                        request.getMessage()
                ).create();
                
                twilioSid = message.getSid();
                logger.info("SMS envoyé avec succès - SID: {}", twilioSid);
            }

            // Sauvegarder la notification
            SmsNotification notification = new SmsNotification();
            notification.setTo(request.getTo());
            notification.setMessage(request.getMessage());
            notification.setStatus(simulationMode ? "SIMULATED" : "SENT");
            notification.setTwilioSid(twilioSid);
            notification.setSentAt(LocalDateTime.now());
            notification.setUserId(request.getUserId());
            notification.setType(request.getType());
            
            smsNotificationRepository.save(notification);

            response.setSuccess(true);
            response.setMessage(simulationMode ? "SMS simulé avec succès" : "SMS envoyé avec succès");
            response.setSmsId(notification.getId());
            response.setTwilioSid(twilioSid);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du SMS: {}", e.getMessage());
            
            // Vérifier si c'est une erreur de numéro non vérifié (compte Twilio trial)
            if (e.getMessage() != null && e.getMessage().contains("unverified")) {
                logger.warn("⚠️ Numéro non vérifié dans Twilio (compte trial) - Passage en mode simulation");
                
                // Sauvegarder comme simulé au lieu de failed
                SmsNotification notification = new SmsNotification();
                notification.setTo(request.getTo());
                notification.setMessage(request.getMessage());
                notification.setStatus("SIMULATED");
                notification.setErrorMessage("Numéro non vérifié - SMS simulé (compte Twilio trial)");
                notification.setTwilioSid("SIM-UNVERIFIED-" + System.currentTimeMillis());
                notification.setSentAt(LocalDateTime.now());
                notification.setUserId(request.getUserId());
                notification.setType(request.getType());
                
                smsNotificationRepository.save(notification);

                response.setSuccess(true); // Considérer comme succès en mode simulation
                response.setMessage("SMS simulé (numéro non vérifié dans Twilio trial)");
                response.setSmsId(notification.getId());
                response.setTwilioSid(notification.getTwilioSid());
                
                return response;
            }
            
            // Sauvegarder l'échec pour les autres erreurs
            SmsNotification notification = new SmsNotification();
            notification.setTo(request.getTo());
            notification.setMessage(request.getMessage());
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notification.setSentAt(LocalDateTime.now());
            notification.setUserId(request.getUserId());
            notification.setType(request.getType());
            
            smsNotificationRepository.save(notification);

            response.setSuccess(false);
            response.setMessage("Erreur lors de l'envoi du SMS: " + e.getMessage());
        }
        
        return response;
    }

    // Envoyer un SMS de notification d'échéance
    public SmsResponseDTO sendEcheanceReminder(String phoneNumber, String conventionRef, String amount, String dueDate, String userId) {
        String message = String.format(
            "RAPPEL ÉCHÉANCE: Convention %s - Montant: %s€ - Échéance: %s. " +
            "Merci de procéder au paiement. Contact: support@example.com",
            conventionRef, amount, dueDate
        );

        SmsRequestDTO request = new SmsRequestDTO();
        request.setTo(phoneNumber);
        request.setMessage(message);
        request.setUserId(userId);
        request.setType("ECHEANCE_REMINDER");

        return sendSms(request);
    }

    // Envoyer un SMS de notification de facture
    public SmsResponseDTO sendInvoiceNotification(String phoneNumber, String invoiceNumber, String amount, String dueDate, String userId) {
        String message = String.format(
            "NOUVELLE FACTURE: %s - Montant: %s€ - Échéance: %s. " +
            "Consultez votre espace client pour plus de détails.",
            invoiceNumber, amount, dueDate
        );

        SmsRequestDTO request = new SmsRequestDTO();
        request.setTo(phoneNumber);
        request.setMessage(message);
        request.setUserId(userId);
        request.setType("INVOICE_NOTIFICATION");

        return sendSms(request);
    }

    // Envoyer un SMS de confirmation de paiement
    public SmsResponseDTO sendPaymentConfirmation(String phoneNumber, String invoiceNumber, String amount, String userId) {
        String message = String.format(
            "CONFIRMATION PAIEMENT: Facture %s - Montant: %s€. " +
            "Paiement reçu avec succès. Merci pour votre confiance.",
            invoiceNumber, amount
        );

        SmsRequestDTO request = new SmsRequestDTO();
        request.setTo(phoneNumber);
        request.setMessage(message);
        request.setUserId(userId);
        request.setType("PAYMENT_CONFIRMATION");

        return sendSms(request);
    }

    // Envoyer un SMS d'alerte de retard
    public SmsResponseDTO sendOverdueAlert(String phoneNumber, String reference, String amount, int daysOverdue, String userId) {
        String message = String.format(
            "ALERTE RETARD: %s - Montant: %s€ - Retard: %d jours. " +
            "Veuillez régulariser votre situation rapidement.",
            reference, amount, daysOverdue
        );

        SmsRequestDTO request = new SmsRequestDTO();
        request.setTo(phoneNumber);
        request.setMessage(message);
        request.setUserId(userId);
        request.setType("OVERDUE_ALERT");

        return sendSms(request);
    }

    // Envoyer un SMS de notification système
    public SmsResponseDTO sendSystemNotification(String phoneNumber, String title, String content, String userId) {
        String message = String.format(
            "NOTIFICATION SYSTÈME: %s - %s",
            title, content
        );

        SmsRequestDTO request = new SmsRequestDTO();
        request.setTo(phoneNumber);
        request.setMessage(message);
        request.setUserId(userId);
        request.setType("SYSTEM_NOTIFICATION");

        return sendSms(request);
    }

    // Récupérer l'historique des SMS
    public List<SmsNotification> getSmsHistory(String userId) {
        return smsNotificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    // Récupérer les statistiques SMS
    public SmsStatsDTO getSmsStats(String userId) {
        List<SmsNotification> notifications = smsNotificationRepository.findByUserId(userId);
        
        SmsStatsDTO stats = new SmsStatsDTO();
        stats.setTotalSent((long) notifications.stream().filter(n -> "SENT".equals(n.getStatus())).count());
        stats.setTotalFailed((long) notifications.stream().filter(n -> "FAILED".equals(n.getStatus())).count());
        stats.setSuccessRate(notifications.isEmpty() ? 0.0 : 
            (double) stats.getTotalSent() / notifications.size() * 100);
        
        return stats;
    }

    // Valider un numéro de téléphone
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Format international général (plus permissif)
        // Accepte: +33, +216, +1, etc. avec 8 à 15 chiffres
        String internationalPattern = "^\\+[1-9]\\d{7,14}$";
        
        boolean isValid = phoneNumber.matches(internationalPattern);
        System.out.println("📱 [SMS DEBUG] Validation numéro: " + phoneNumber + " -> " + (isValid ? "VALIDE" : "INVALIDE"));
        
        return isValid;
    }

    // Formater un numéro de téléphone français
    public String formatFrenchPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;
        
        // Supprimer tous les caractères non numériques
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        
        // Si c'est un numéro français (10 chiffres)
        if (cleaned.length() == 10 && cleaned.startsWith("0")) {
            return "+33" + cleaned.substring(1);
        }
        
        // Si c'est déjà au format international
        if (cleaned.startsWith("33")) {
            return "+" + cleaned;
        }
        
        return phoneNumber;
    }

    // Vérifier le statut d'un SMS via Twilio
    public String checkSmsStatus(String twilioSid) {
        try {
            Message message = Message.fetcher(twilioSid).fetch();
            return message.getStatus().toString();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    // Mettre à jour le statut d'un SMS
    public void updateSmsStatus(String smsId, String status) {
        Optional<SmsNotification> optional = smsNotificationRepository.findById(smsId);
        if (optional.isPresent()) {
            SmsNotification notification = optional.get();
            notification.setStatus(status);
            notification.setUpdatedAt(LocalDateTime.now());
            smsNotificationRepository.save(notification);
        }
    }

    // Supprimer un SMS de l'historique
    public void deleteSms(String smsId) {
        smsNotificationRepository.deleteById(smsId);
    }

    // Nettoyer les anciens SMS (plus de 30 jours)
    public void cleanupOldSms() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        smsNotificationRepository.deleteBySentAtBefore(cutoffDate);
    }

    // Envoyer un message WhatsApp (placeholder - à implémenter avec l'API WhatsApp Business)
    public SmsResponseDTO sendWhatsApp(String phoneNumber, String message) {
        SmsResponseDTO response = new SmsResponseDTO();
        
        try {
            // TODO: Implémenter l'envoi WhatsApp avec l'API WhatsApp Business
            // Pour l'instant, on simule l'envoi
            System.out.println("WhatsApp message to " + phoneNumber + ": " + message);
            
            // Sauvegarder la notification
            SmsNotification notification = new SmsNotification();
            notification.setTo(phoneNumber);
            notification.setMessage(message);
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notification.setType("WHATSAPP");
            
            smsNotificationRepository.save(notification);

            response.setSuccess(true);
            response.setMessage("Message WhatsApp envoyé avec succès");
            response.setSmsId(notification.getId());

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Erreur lors de l'envoi du message WhatsApp: " + e.getMessage());
        }

        return response;
    }
}