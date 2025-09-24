package com.example.demo.service;

import com.example.demo.dto.NotificationSettingsDTO;
import com.example.demo.dto.NotificationHistoryDTO;
import com.example.demo.model.Invoice;
import com.example.demo.model.NotificationLog;
import com.example.demo.repository.NotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import com.example.demo.service.TwilioNotificationService;

@Service
public class NotificationService {
    @Autowired
    private NotificationLogRepository notificationLogRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TwilioNotificationService twilioNotificationService;

    public List<NotificationLog> getNotificationsForUser(String userId) {
        return notificationLogRepository.findByRecipientId(userId);
    }

    public void sendNotificationToUser(NotificationLog notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    public void createAndSendNotification(NotificationLog notification) {
        notificationLogRepository.save(notification);
        sendNotificationToUser(notification);
    }

    public void markAsRead(String id) {
        notificationLogRepository.findById(id).ifPresent(n -> {
            n.setStatus("READ");
            notificationLogRepository.save(n);
            sendNotificationToUser(n);
        });
    }

    public long getUnreadCount(String userId) {
        // Count all notifications not marked as READ
        return notificationLogRepository.countByRecipientIdAndStatusNot(userId, "READ");
    }

    public int markAsReadBulk(String userId, List<String> ids) {
        List<NotificationLog> toUpdate = notificationLogRepository.findByIdInAndRecipientId(ids, userId);
        toUpdate.forEach(n -> n.setStatus("READ"));
        notificationLogRepository.saveAll(toUpdate);
        // Optionally push an aggregate update via WebSocket
        return toUpdate.size();
    }

    // Méthodes legacy pour compatibilité
    public NotificationSettingsDTO getNotificationSettings(String userId) {
        return new NotificationSettingsDTO();
    }
    public NotificationSettingsDTO updateNotificationSettings(String userId, NotificationSettingsDTO settings) {
        return settings;
    }
    public List<NotificationHistoryDTO> getNotificationHistory(String userId) {
        return new ArrayList<>();
    }
    public void sendManualReminder(String userId, String invoiceId, String type) {}
    public void scheduleAutomaticReminders(String userId) {}
    public void testNotification(String userId, String type, String recipient) {}
    public void sendPaymentReminderNotification(Invoice invoice) {}
    public void notifyPaymentValidated(String invoiceId, String receiptUri) {
        // DEPRECATED: Cette méthode ne doit plus être utilisée car validation automatique supprimée
        System.out.println("[DEPRECATED] notifyPaymentValidated appelée - validation automatique supprimée selon cahier des charges");
    }

    /**
     * Notifie le commercial qu'une validation manuelle est requise
     * Selon le cahier des charges : seul le commercial peut valider les paiements
     */
    public void notifyCommercialForManualValidation(String invoiceId, String commercialId, double amount, String paymentMethod) {
        // 1. Notification WebSocket pour le commercial
        NotificationPayload payload = new NotificationPayload();
        payload.setType("VALIDATION_MANUELLE_REQUISE");
        payload.setInvoiceId(invoiceId);
        payload.setMessage("Validation manuelle requise pour la facture " + invoiceId + 
                         " - Montant: " + amount + " EUR - Méthode: " + paymentMethod);
        payload.setLink("/commercial/invoices/" + invoiceId);
        messagingTemplate.convertAndSend("/topic/commercial/" + commercialId, payload);

        // 2. Email au commercial
        String commercialEmail = findCommercialEmailById(commercialId);
        if (commercialEmail != null) {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(commercialEmail);
            mail.setSubject("[Action Requise] Validation manuelle - Facture " + invoiceId);
            mail.setText("Une preuve de paiement a été reçue pour la facture " + invoiceId + 
                       ".\nMontant: " + amount + " EUR" +
                       "\nMéthode: " + paymentMethod + 
                       "\n\nSelon le cahier des charges, vous devez valider manuellement ce paiement." +
                       "\nConnectez-vous à l'application pour traiter cette demande.");
            try {
                mailSender.send(mail);
            } catch (Exception e) {
                System.err.println("Erreur envoi email commercial: " + e.getMessage());
            }
        }
        
        System.out.println("[NOTIFICATION] Commercial " + commercialId + " notifié pour validation manuelle facture " + invoiceId);
    }

    public void notifyPaymentPendingReview(String invoiceId, String proofUri) {
        // 1. Notification WebSocket
        NotificationPayload payload = new NotificationPayload();
        payload.setType("PAIEMENT_ATTENTE");
        payload.setInvoiceId(invoiceId);
        payload.setMessage("La preuve de paiement pour la facture " + invoiceId + " est en attente de vérification.");
        payload.setLink(proofUri);
        messagingTemplate.convertAndSend("/topic/notifications", payload);

        // 2. Email interne (exemple)
        String to = findCommercialEmailByInvoiceId(invoiceId); // À implémenter selon ta logique métier
        if (to != null) {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject("[Paiement en attente] Facture " + invoiceId);
            mail.setText("La preuve de paiement pour la facture " + invoiceId + " est en attente de vérification. Voir la preuve : " + proofUri);
            mailSender.send(mail);
        }
    }

    // À adapter : méthode pour retrouver l'email du commercial à partir de l'id facture
    private String findCommercialEmailByInvoiceId(String invoiceId) {
        // TODO : Intégrer avec InvoiceRepository/UserRepository
        return null;
    }

    private String findCommercialEmailById(String commercialId) {
        // Mock implementation - à remplacer par la vraie logique
        return "commercial@example.com";
    }

    // Payload pour WebSocket
    public static class NotificationPayload {
        private String type;
        private String invoiceId;
        private String message;
        private String link;
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getInvoiceId() { return invoiceId; }
        public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
    }
}
