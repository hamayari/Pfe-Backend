package com.example.demo.service;

import com.example.demo.dto.SmsRequestDTO;
import com.example.demo.dto.SmsResponseDTO;
import com.example.demo.dto.SmsStatsDTO;
import com.example.demo.model.SmsNotification;
import com.example.demo.repository.SmsNotificationRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @Autowired
    private SmsNotificationRepository smsNotificationRepository;

    // Initialiser Twilio
    public void initializeTwilio() {
        if (accountSid != null && authToken != null) {
            Twilio.init(accountSid, authToken);
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
            System.out.println("📱 [SMS DEBUG] Début envoi SMS");
            System.out.println("📱 [SMS DEBUG] To: " + request.getTo());
            System.out.println("📱 [SMS DEBUG] From: " + fromPhoneNumber);
            System.out.println("📱 [SMS DEBUG] Message: " + request.getMessage());
            System.out.println("📱 [SMS DEBUG] Account SID: " + accountSid);
            System.out.println("📱 [SMS DEBUG] Auth Token: " + (authToken != null ? "PRÉSENT" : "NULL"));
            
            // Valider le numéro de téléphone
            if (!isValidPhoneNumber(request.getTo())) {
                System.err.println("❌ [SMS DEBUG] Numéro de téléphone invalide: " + request.getTo());
                response.setSuccess(false);
                response.setMessage("Numéro de téléphone invalide");
                return response;
            }

            System.out.println("📱 [SMS DEBUG] Envoi via Twilio...");
            // Envoyer le SMS via Twilio
            Message message = Message.creator(
                    new PhoneNumber(request.getTo()),
                    new PhoneNumber(fromPhoneNumber),
                    request.getMessage()
            ).create();
            
            System.out.println("📱 [SMS DEBUG] SMS envoyé avec succès - SID: " + message.getSid());

            // Sauvegarder la notification
            SmsNotification notification = new SmsNotification();
            notification.setTo(request.getTo());
            notification.setMessage(request.getMessage());
            notification.setStatus("SENT");
            notification.setTwilioSid(message.getSid());
            notification.setSentAt(LocalDateTime.now());
            notification.setUserId(request.getUserId());
            notification.setType(request.getType());
            
            smsNotificationRepository.save(notification);

            response.setSuccess(true);
            response.setMessage("SMS envoyé avec succès");
            response.setSmsId(notification.getId());
            response.setTwilioSid(message.getSid());

        } catch (Exception e) {
            System.err.println("❌ [SMS DEBUG] Erreur lors de l'envoi du SMS: " + e.getMessage());
            e.printStackTrace();
            
            // Sauvegarder l'échec
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
        
        // Format international français
        String frenchPattern = "^\\+33[1-9](\\d{8})$";
        // Format international général
        String internationalPattern = "^\\+[1-9]\\d{1,14}$";
        
        return phoneNumber.matches(frenchPattern) || phoneNumber.matches(internationalPattern);
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