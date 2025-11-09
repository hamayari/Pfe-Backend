package com.example.demo.service;

import com.example.demo.dto.chatbot.ActionRequest;
import com.example.demo.dto.chatbot.ActionResponse;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.NotificationLog;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Service
public class ChatbotActionService {

    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Exécute une action demandée par le chatbot
     */
    public ActionResponse executeAction(ActionRequest request) {
        System.out.println("🤖 [ACTION] Exécution de l'action: " + request.getAction());
        
        try {
            switch (request.getAction()) {
                case "create_convention":
                    return createConvention(request.getParameters());
                    
                case "create_facture":
                    return createFacture(request.getParameters());
                    
                case "send_reminder":
                    return sendReminder(request.getParameters());
                    
                case "send_notification":
                    return sendNotification(request.getParameters());
                    
                case "get_unpaid_invoices":
                    return getUnpaidInvoices(request.getParameters());
                    
                case "mark_as_paid":
                    return markAsPaid(request.getParameters());
                    
                default:
                    return new ActionResponse(false, "Action non reconnue: " + request.getAction());
            }
        } catch (Exception e) {
            System.err.println("❌ [ACTION] Erreur: " + e.getMessage());
            e.printStackTrace();
            return new ActionResponse(false, "Erreur lors de l'exécution: " + e.getMessage());
        }
    }
    
    /**
     * Crée une nouvelle convention
     */
    private ActionResponse createConvention(Map<String, Object> params) {
        try {
            Convention convention = new Convention();
            convention.setTitle((String) params.get("title"));
            convention.setReference((String) params.get("reference"));
            convention.setStructureId((String) params.get("structureId"));
            convention.setApplicationId((String) params.get("applicationId"));
            convention.setGovernorate((String) params.get("governorate"));
            convention.setAmount(new java.math.BigDecimal(params.get("amount").toString()));
            convention.setStartDate(LocalDate.parse((String) params.get("startDate")));
            convention.setEndDate(LocalDate.parse((String) params.get("endDate")));
            convention.setStatus("DRAFT");
            convention.setCreatedAt(LocalDate.now());
            convention.setCreatedBy((String) params.getOrDefault("createdBy", "chatbot"));
            
            Convention saved = conventionRepository.save(convention);
            
            System.out.println("✅ [ACTION] Convention créée: " + saved.getId());
            return new ActionResponse(true, "Convention créée avec succès !", saved);
            
        } catch (Exception e) {
            return new ActionResponse(false, "Erreur lors de la création de la convention: " + e.getMessage());
        }
    }
    
    /**
     * Crée une nouvelle facture
     */
    private ActionResponse createFacture(Map<String, Object> params) {
        try {
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber((String) params.get("invoiceNumber"));
            invoice.setConventionId((String) params.get("conventionId"));
            invoice.setAmount(new java.math.BigDecimal(params.get("amount").toString()));
            invoice.setIssueDate(LocalDate.parse((String) params.get("issueDate")));
            invoice.setDueDate(LocalDate.parse((String) params.get("dueDate")));
            invoice.setStatus("PENDING");
            invoice.setCreatedAt(LocalDate.now());
            invoice.setCreatedBy((String) params.getOrDefault("createdBy", "chatbot"));
            
            Invoice saved = invoiceRepository.save(invoice);
            
            System.out.println("✅ [ACTION] Facture créée: " + saved.getId());
            return new ActionResponse(true, "Facture créée avec succès !", saved);
            
        } catch (Exception e) {
            return new ActionResponse(false, "Erreur lors de la création de la facture: " + e.getMessage());
        }
    }
    
    /**
     * Envoie un rappel avec notification réelle
     */
    private ActionResponse sendReminder(Map<String, Object> params) {
        try {
            String conventionId = (String) params.get("conventionId");
            Object daysObj = params.get("daysBeforeExpiry");
            int daysBeforeExpiry = daysObj instanceof Integer ? (Integer) daysObj : 
                                   Integer.parseInt(daysObj.toString());
            
            // Vérifier que la convention existe
            var conventionOpt = conventionRepository.findById(conventionId);
            if (conventionOpt.isEmpty()) {
                return new ActionResponse(false, "Convention non trouvée: " + conventionId);
            }
            
            Convention convention = conventionOpt.get();
            
            // Créer une notification
            NotificationLog notification = new NotificationLog();
            notification.setType("SYSTEM");
            notification.setChannel("IN_APP");
            notification.setSubject("Rappel Convention - " + convention.getReference());
            notification.setMessage("La convention " + convention.getReference() + 
                                  " expire dans " + daysBeforeExpiry + " jours (le " + 
                                  convention.getEndDate() + ")");
            notification.setRecipientId(convention.getCreatedBy());
            notification.setStatus("PENDING");
            notification.setSentAt(LocalDateTime.now());
            notification.setConventionId(conventionId);
            
            // Envoyer la notification
            notificationService.createAndSendNotification(notification);
            
            System.out.println("📧 [ACTION] Rappel envoyé pour la convention " + conventionId + 
                              " (" + daysBeforeExpiry + " jours avant échéance)");
            
            return new ActionResponse(true, 
                "✅ Rappel envoyé avec succès ! Une notification a été créée pour " + 
                daysBeforeExpiry + " jours avant l'échéance (" + convention.getEndDate() + ").",
                notification);
                
        } catch (Exception e) {
            System.err.println("❌ [ACTION] Erreur lors de l'envoi du rappel: " + e.getMessage());
            return new ActionResponse(false, "Erreur lors de l'envoi du rappel: " + e.getMessage());
        }
    }
    
    /**
     * Envoie une notification personnalisée
     */
    private ActionResponse sendNotification(Map<String, Object> params) {
        try {
            String recipientId = (String) params.get("recipientId");
            String subject = (String) params.get("subject");
            String message = (String) params.get("message");
            String type = (String) params.getOrDefault("type", "SYSTEM");
            
            // Créer la notification
            NotificationLog notification = new NotificationLog();
            notification.setType(type);
            notification.setChannel("IN_APP");
            notification.setSubject(subject);
            notification.setMessage(message);
            notification.setRecipientId(recipientId);
            notification.setStatus("PENDING");
            notification.setSentAt(LocalDateTime.now());
            
            // Envoyer
            notificationService.createAndSendNotification(notification);
            
            System.out.println("📧 [ACTION] Notification envoyée à " + recipientId);
            
            return new ActionResponse(true, 
                "✅ Notification envoyée avec succès !",
                notification);
                
        } catch (Exception e) {
            System.err.println("❌ [ACTION] Erreur lors de l'envoi de la notification: " + e.getMessage());
            return new ActionResponse(false, "Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
    }
    
    /**
     * Récupère les factures non payées
     */
    private ActionResponse getUnpaidInvoices(Map<String, Object> params) {
        // Récupérer les factures avec statut PENDING ou OVERDUE
        List<Invoice> unpaidInvoices = invoiceRepository.findAll().stream()
            .filter(f -> "PENDING".equals(f.getStatus()) || "OVERDUE".equals(f.getStatus()))
            .toList();
        
        System.out.println("📋 [ACTION] " + unpaidInvoices.size() + " factures non payées trouvées");
        
        return new ActionResponse(true, 
            unpaidInvoices.size() + " facture(s) non payée(s) trouvée(s).", 
            unpaidInvoices);
    }
    
    /**
     * Marque une facture comme payée
     */
    private ActionResponse markAsPaid(Map<String, Object> params) {
        String invoiceId = (String) params.get("invoiceId");
        
        var invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            return new ActionResponse(false, "Facture non trouvée: " + invoiceId);
        }
        
        Invoice invoice = invoiceOpt.get();
        invoice.setStatus("PAID");
        invoice.setPaymentDate(LocalDate.now());
        invoiceRepository.save(invoice);
        
        System.out.println("✅ [ACTION] Facture marquée comme payée: " + invoiceId);
        return new ActionResponse(true, "Facture marquée comme payée avec succès !", invoice);
    }
}
