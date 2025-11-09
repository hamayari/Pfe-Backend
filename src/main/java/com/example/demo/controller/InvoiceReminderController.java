package com.example.demo.controller;

import com.example.demo.model.Invoice;
import com.example.demo.model.User;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SmsService;
import com.example.demo.service.RealTimeNotificationService;
import com.example.demo.dto.NotificationDTO;
import com.example.demo.dto.SmsResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceReminderController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RealTimeNotificationService notificationService;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envoyer un rappel multi-canal pour une facture
     */
    @PostMapping("/{invoiceId}/send-reminder")
    public ResponseEntity<Map<String, Object>> sendReminder(
            @PathVariable String invoiceId,
            @RequestBody Map<String, Object> payload) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📤 [REMINDER] ========================================");
            System.out.println("📤 [REMINDER] Envoi de rappel pour facture: " + invoiceId);
            System.out.println("📤 [REMINDER] Payload reçu: " + payload);
            
            // Récupérer la facture
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
            if (!invoiceOpt.isPresent()) {
                System.err.println("❌ [REMINDER] Facture introuvable avec ID: " + invoiceId);
                response.put("success", false);
                response.put("message", "Facture introuvable");
                response.put("emailSent", false);
                response.put("smsSent", false);
                response.put("websocketSent", false);
                return ResponseEntity.status(404).body(response);
            }
            
            Invoice invoice = invoiceOpt.get();
            String invoiceNumber = invoice.getInvoiceNumber();
            BigDecimal amount = invoice.getAmount();
            LocalDate dueDate = invoice.getDueDate();
            
            // Récupérer le commercial (propriétaire de la facture)
            String createdBy = invoice.getCreatedBy();
            
            System.out.println("📋 [REMINDER] Invoice ID: " + invoice.getId());
            System.out.println("📋 [REMINDER] Invoice Number: " + invoiceNumber);
            System.out.println("📋 [REMINDER] Created By: " + createdBy);
            
            if (createdBy == null || createdBy.isEmpty()) {
                System.err.println("❌ [REMINDER] Created By est null ou vide - impossible d'envoyer les notifications");
                response.put("success", false);
                response.put("message", "Créateur manquant pour cette facture");
                response.put("emailSent", false);
                response.put("smsSent", false);
                response.put("websocketSent", false);
                return ResponseEntity.status(400).body(response);
            }
            
            // Chercher l'utilisateur par username OU par ID
            System.out.println("🔍 [REMINDER] Recherche utilisateur avec createdBy: " + createdBy);
            Optional<User> commercialOpt = userRepository.findByUsername(createdBy);
            
            // Si pas trouvé par username, essayer par ID
            if (!commercialOpt.isPresent()) {
                System.out.println("🔍 [REMINDER] Pas trouvé par username, essai par ID...");
                commercialOpt = userRepository.findById(createdBy);
            }
            
            if (!commercialOpt.isPresent()) {
                System.err.println("❌ [REMINDER] Commercial introuvable avec username/ID: " + createdBy);
                response.put("success", false);
                response.put("message", "Commercial introuvable: " + createdBy);
                response.put("emailSent", false);
                response.put("smsSent", false);
                response.put("websocketSent", false);
                return ResponseEntity.status(400).body(response);
            }
            
            User commercial = commercialOpt.get();
            System.out.println("✅ [REMINDER] Commercial trouvé: " + commercial.getUsername() + " (ID: " + commercial.getId() + ")");
            System.out.println("📧 [REMINDER] Email: " + commercial.getEmail());
            System.out.println("📱 [REMINDER] Phone: " + commercial.getPhoneNumber());
            
            boolean emailSent = false;
            boolean smsSent = false;
            boolean websocketSent = false;
            
            // 1. ENVOYER EMAIL
            try {
                String email = commercial.getEmail();
                    
                    if (email != null && !email.isEmpty()) {
                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setTo(email);
                        message.setFrom("noreply@ministere.gov.tn");
                        message.setSubject("Rappel de Paiement - Facture " + (invoiceNumber != null ? invoiceNumber : invoice.getId()));
                        message.setText(String.format(
                            "Bonjour %s,\n\n" +
                            "Nous vous rappelons qu'une facture est en attente de règlement :\n\n" +
                            "Référence : %s\n" +
                            "Montant : %s DT\n" +
                            "Date d'échéance : %s\n\n" +
                            "Merci de procéder au paiement dans les meilleurs délais.\n\n" +
                            "Cordialement,\n" +
                            "Service Financier\n" +
                            "Ministère",
                            commercial.getName() != null ? commercial.getName() : commercial.getUsername(),
                            invoiceNumber != null ? invoiceNumber : invoice.getId(),
                            amount.toString(),
                            dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        ));
                        
                        mailSender.send(message);
                        emailSent = true;
                        System.out.println("✅ [REMINDER] Email envoyé à: " + email);
                    } else {
                        System.err.println("⚠️ [REMINDER] Email vide ou null pour l'utilisateur: " + commercial.getUsername());
                    }
            } catch (Exception e) {
                System.err.println("❌ [REMINDER] Erreur envoi email: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 2. ENVOYER SMS
            try {
                String phoneNumber = commercial.getPhoneNumber();
                    
                    System.out.println("📱 [REMINDER SMS] Commercial ID: " + commercial.getId());
                    System.out.println("📱 [REMINDER SMS] Commercial Username: " + commercial.getUsername());
                    System.out.println("📱 [REMINDER SMS] Phone Number: " + phoneNumber);
                    
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        System.out.println("📱 [REMINDER SMS] Préparation envoi SMS...");
                        
                        Map<String, String> smsVariables = new HashMap<>();
                        smsVariables.put("invoiceNumber", invoiceNumber);
                        smsVariables.put("amount", amount.toString());
                        smsVariables.put("dueDate", dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        
                        SmsResponseDTO smsResponse = smsService.sendSmsWithTemplate(
                            phoneNumber,
                            "invoice_reminder",
                            smsVariables
                        );
                        
                        smsSent = smsResponse.isSuccess();
                        System.out.println("✅ [REMINDER SMS] Résultat envoi - Statut: " + smsSent + " - Message: " + smsResponse.getMessage());
                    } else {
                        System.err.println("⚠️ [REMINDER SMS] Numéro de téléphone vide ou null pour l'utilisateur: " + commercial.getUsername());
                    }
            } catch (Exception e) {
                System.err.println("❌ [REMINDER SMS] Erreur envoi SMS: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 3. ENVOYER NOTIFICATION WEBSOCKET
            try {
                NotificationDTO notification = new NotificationDTO();
                notification.setType("warning");
                notification.setTitle("Rappel de Paiement");
                notification.setMessage(String.format(
                    "Facture %s - Montant: %s € - Échéance: %s",
                    invoiceNumber,
                    amount.toString(),
                    dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ));
                notification.setPriority("high");
                notification.setCategory("invoice");
                notification.setUserId(commercial.getId());
                
                notificationService.createNotification(notification);
                websocketSent = true;
                System.out.println("✅ [REMINDER] Notification WebSocket envoyée");
            } catch (Exception e) {
                System.err.println("❌ [REMINDER] Erreur envoi WebSocket: " + e.getMessage());
            }
            
            // Construire la réponse
            response.put("success", true);
            response.put("message", "Rappel envoyé avec succès");
            response.put("emailSent", emailSent);
            response.put("smsSent", smsSent);
            response.put("websocketSent", websocketSent);
            response.put("invoiceNumber", invoiceNumber);
            
            System.out.println(String.format(
                "✅ [REMINDER] Rappel envoyé - Email: %s, SMS: %s, WebSocket: %s",
                emailSent, smsSent, websocketSent
            ));
            System.out.println("📤 [REMINDER] Réponse à envoyer: " + response);
            System.out.println("📤 [REMINDER] ========================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [REMINDER] Erreur générale: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi du rappel: " + e.getMessage());
            response.put("emailSent", false);
            response.put("smsSent", false);
            response.put("websocketSent", false);
            System.out.println("📤 [REMINDER] Réponse d'erreur: " + response);
            System.out.println("📤 [REMINDER] ========================================");
            return ResponseEntity.status(500).body(response);
        }
    }
}
