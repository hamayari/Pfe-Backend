package com.example.demo.service;

import com.example.demo.dto.convention.ConventionRequest;
import com.example.demo.dto.invoice.InvoiceRequest;
import com.example.demo.model.Convention;
import com.example.demo.service.InvoiceService;

import com.example.demo.repository.ConventionRepository;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.model.PaymentProof;
import com.example.demo.repository.PaymentProofRepository;
import com.example.demo.model.NotificationLog;
import com.example.demo.repository.NotificationLogRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.User;
import java.util.Map;
import java.util.HashMap;

@Service
public class ConventionService {

    @Autowired
    private ConventionRepository conventionRepository;
    
    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentProofRepository paymentProofRepository;
    @Autowired
    private NotificationLogRepository notificationLogRepository;
    @Autowired
    @SuppressWarnings("unused")
    private UserRepository userRepository;
    
    @Autowired
    private RealTimeNotificationService realTimeNotificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    public Convention createConvention(ConventionRequest request, String userId) {
        // Check if convention with same reference already exists
        if (conventionRepository.existsByReference(request.getReference())) {
            throw new IllegalArgumentException("Convention with reference " + request.getReference() + " already exists");
        }

        Convention convention = new Convention();
        convention.setReference(request.getReference());
        convention.setTitle(request.getTitle());
        convention.setDescription(request.getDescription());
        convention.setStartDate(request.getStartDate().toLocalDate());
        convention.setEndDate(request.getEndDate().toLocalDate());
        convention.setStructureId(request.getStructure());
        convention.setZoneGeographiqueId(request.getGeographicZone());
        convention.setAmount(request.getAmount());
        convention.setStatus("ACTIVE"); // Définir un statut par défaut
        convention.setGovernorate(request.getGeographicZone()); // Utiliser la zone géographique comme gouvernorat par défaut
        convention.setPaymentTerms(request.getPaymentTerms());
        convention.setClient(request.getClient());
        convention.setType(request.getType());
        convention.setCreatedBy(userId);
        convention.setCreatedAt(LocalDate.now());
        convention.setUpdatedAt(LocalDate.now());
        // Correction : remplir dueDate
        if (request.getDueDate() != null) {
            convention.setDueDate(request.getDueDate());
        } else if (request.getEndDate() != null) {
            convention.setDueDate(request.getEndDate().toLocalDate());
        }
        convention.setTag(request.getTag() != null ? request.getTag() : "");

        // Génération automatique du calendrier d'échéances
        if (request.getPaymentTerms() != null && request.getStartDate() != null && request.getEndDate() != null) {
            int numberOfPayments = request.getPaymentTerms().getNumberOfPayments();
            int intervalDays = request.getPaymentTerms().getIntervalDays();
            System.out.println("🔄 Génération des échéances: " + numberOfPayments + " paiements, intervalle: " + intervalDays + " jours");
            
            List<LocalDate> echeances = new ArrayList<>();
            LocalDate current = request.getStartDate().toLocalDate();
            for (int i = 0; i < numberOfPayments; i++) {
                echeances.add(current);
                System.out.println("📅 Échéance " + (i+1) + ": " + current);
                current = current.plusDays(intervalDays);
            }
            // S'assurer que la dernière échéance ne dépasse pas la date de fin
            echeances = echeances.stream().filter(d -> !d.isAfter(request.getEndDate().toLocalDate())).collect(Collectors.toList());
            convention.setEcheances(echeances);
            System.out.println("✅ " + echeances.size() + " échéances générées pour la convention " + request.getReference());
        } else {
            System.out.println("⚠️ Impossible de générer les échéances - PaymentTerms: " + (request.getPaymentTerms() != null ? "présent" : "absent") + 
                             ", StartDate: " + (request.getStartDate() != null ? "présent" : "absent") + 
                             ", EndDate: " + (request.getEndDate() != null ? "présent" : "absent"));
        }
        Convention savedConvention = conventionRepository.save(convention);
        System.out.println("💾 Convention sauvegardée en base avec l'ID: " + savedConvention.getId());
        System.out.println("💾 Référence: " + savedConvention.getReference());
        System.out.println("💾 Créée par: " + savedConvention.getCreatedBy());

        // 🔔 NOTIFICATION AUTOMATIQUE - Convention créée
        try {
            // 1. Notification interne (WebSocket)
            NotificationDTO notification = new NotificationDTO();
            notification.setType("success");
            notification.setTitle("✅ Nouvelle Convention Créée");
            notification.setMessage("Convention " + savedConvention.getReference() + " créée avec succès pour " + savedConvention.getTitle());
            notification.setPriority("medium");
            notification.setCategory("convention");
            notification.setUserId(userId);
            notification.setSource("ConventionService");
            
            realTimeNotificationService.createNotification(notification);
            System.out.println("🔔 Notification interne envoyée pour la convention " + savedConvention.getReference());
            
            // 2. Email et SMS réels
            User commercial = userRepository.findById(userId).orElse(null);
            if (commercial == null) {
                // Essayer de trouver par username si l'ID ne fonctionne pas
                commercial = userRepository.findByUsername(userId).orElse(null);
            }
            
            System.out.println("🔍 [DEBUG] User trouvé: " + (commercial != null ? commercial.getUsername() : "NULL"));
            System.out.println("🔍 [DEBUG] User ID: " + userId);
            System.out.println("🔍 [DEBUG] User email: " + (commercial != null ? commercial.getEmail() : "NULL"));
            System.out.println("🔍 [DEBUG] User phone: " + (commercial != null ? commercial.getPhoneNumber() : "NULL"));
            
            if (commercial != null) {
                // Email
                try {
                    Map<String, String> emailVariables = new HashMap<>();
                    emailVariables.put("commercialName", commercial.getName() != null ? commercial.getName() : commercial.getUsername());
                    emailVariables.put("conventionReference", savedConvention.getReference());
                    emailVariables.put("conventionTitle", savedConvention.getTitle());
                    emailVariables.put("amount", String.valueOf(savedConvention.getAmount()));
                    emailVariables.put("dueDate", savedConvention.getDueDate() != null ? savedConvention.getDueDate().toString() : "N/A");
                    
                    // Test avec email de test en cas d'erreur Gmail
                    String testEmail = "hamayari71@gmail.com";
                    emailService.sendConventionCreatedEmail(testEmail, emailVariables);
                    System.out.println("📧 Email envoyé à " + testEmail + " pour la convention " + savedConvention.getReference());
                } catch (Exception e) {
                    System.err.println("❌ Erreur envoi email convention: " + e.getMessage());
                }
                
                // SMS
                try {
                           String phoneNumber = commercial.getPhoneNumber();
                           if (phoneNumber == null || phoneNumber.isEmpty()) {
                               System.out.println("⚠️ [DEBUG] L'utilisateur " + commercial.getUsername() + " n'a pas de numéro de téléphone configuré");
                               System.out.println("📱 [DEBUG] SMS non envoyé - numéro manquant pour l'utilisateur");
                               // Ne pas envoyer de SMS si pas de numéro
                           } else {
                    
                    Map<String, String> smsVariables = new HashMap<>();
                    smsVariables.put("conventionReference", savedConvention.getReference());
                    smsVariables.put("amount", String.valueOf(savedConvention.getAmount()));
                    
                               smsService.sendSmsWithTemplate(phoneNumber, "convention_created", smsVariables);
                               System.out.println("📱 SMS envoyé au " + phoneNumber + " pour la convention " + savedConvention.getReference());
                           }
                       } catch (Exception e) {
                           System.err.println("❌ Erreur envoi SMS convention: " + e.getMessage());
                       }
                   }
               } catch (Exception e) {
                   System.err.println("❌ Erreur envoi notification convention: " + e.getMessage());
               }

        // Debug des PaymentTerms
        System.out.println("PaymentTerms: " + (request.getPaymentTerms() != null ? "présent" : "absent"));
        if (request.getPaymentTerms() != null) {
            System.out.println("Nombre de paiements: " + request.getPaymentTerms().getNumberOfPayments());
            System.out.println("Intervalle: " + request.getPaymentTerms().getIntervalDays() + " jours");
        }

        // Génération automatique des factures basée sur les échéances
        if (request.getPaymentTerms() != null && request.getAmount() != null) {
            int numberOfPayments = request.getPaymentTerms().getNumberOfPayments();
            int intervalDays = request.getPaymentTerms().getIntervalDays();
            BigDecimal montant = request.getAmount();
            BigDecimal montantParEcheance = montant.divide(BigDecimal.valueOf(numberOfPayments), 2, BigDecimal.ROUND_HALF_UP);
            LocalDate dateEcheance = request.getStartDate().toLocalDate();
            
            System.out.println("💰 Génération de " + numberOfPayments + " factures de " + montantParEcheance + "€ chacune");
            
            for (int i = 0; i < numberOfPayments; i++) {
                InvoiceRequest invoiceRequest = new InvoiceRequest();
                invoiceRequest.setConventionId(savedConvention.getId());
                invoiceRequest.setReference(savedConvention.getReference() + "-ECHEANCE-" + (i+1));
                invoiceRequest.setAmount(montantParEcheance);
                invoiceRequest.setDueDate(dateEcheance.plusDays(i * intervalDays).atStartOfDay());
                
                System.out.println("📄 Création facture " + (i+1) + ": " + invoiceRequest.getReference() + " - " + invoiceRequest.getAmount() + "€ - Échéance: " + invoiceRequest.getDueDate());
                
                try {
                    invoiceService.createInvoice(invoiceRequest, userId);
                    System.out.println("✅ Facture " + (i+1) + " créée avec succès");
                } catch (Exception e) {
                    System.out.println("❌ Erreur création facture " + (i+1) + ": " + e.getMessage());
                }
            }
        }
    
        return savedConvention;
    }

    public Convention updateConvention(String id, ConventionRequest request) {
        Convention convention = getConventionById(id);
        
        convention.setReference(request.getReference());
        convention.setTitle(request.getTitle());
        convention.setDescription(request.getDescription());
        convention.setStartDate(request.getStartDate().toLocalDate());
        convention.setEndDate(request.getEndDate().toLocalDate());
        convention.setStructureId(request.getStructure());
        convention.setZoneGeographiqueId(request.getGeographicZone());
        convention.setAmount(request.getAmount());
        convention.setGovernorate(request.getGeographicZone()); // Utiliser la zone géographique comme gouvernorat
        convention.setPaymentTerms(request.getPaymentTerms());
        convention.setUpdatedAt(LocalDate.now());
        // Correction : remplir dueDate
        if (request.getDueDate() != null) {
            convention.setDueDate(request.getDueDate());
        } else if (request.getEndDate() != null) {
            convention.setDueDate(request.getEndDate().toLocalDate());
        }

        return conventionRepository.save(convention);
    }

    public Convention getConventionById(String id) {
        return conventionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convention not found with id: " + id));
    }

    public List<Convention> getAllConventions() {
        List<Convention> conventions = conventionRepository.findAll();
        System.out.println("📋 getAllConventions() - " + conventions.size() + " conventions trouvées en base");
        for (Convention c : conventions) {
            System.out.println("  - " + c.getReference() + " (ID: " + c.getId() + ", Créée par: " + c.getCreatedBy() + ")");
        }
        return conventions;
    }

    public List<Convention> getAllConventionsByUser(String userId) {
        return conventionRepository.findByCreatedBy(userId);
    }

    public void deleteConvention(String id) {
        // Suppression en cascade : supprimer d'abord les factures associées et tout ce qui en dépend
        List<Invoice> invoices = invoiceRepository.findByConventionId(id);
        if (invoices != null && !invoices.isEmpty()) {
            for (Invoice invoice : invoices) {
                // Supprimer les preuves de paiement liées à la facture
                List<PaymentProof> proofs = paymentProofRepository.findByInvoiceId(invoice.getId());
                if (proofs != null && !proofs.isEmpty()) {
                    for (PaymentProof proof : proofs) {
                        paymentProofRepository.delete(proof);
                        // Audit log removed - service not available
                    }
                }
                // Supprimer les notifications liées à la facture
                List<NotificationLog> notifs = notificationLogRepository.findByInvoiceId(invoice.getId());
                if (notifs != null && !notifs.isEmpty()) {
                    for (NotificationLog notif : notifs) {
                        notificationLogRepository.delete(notif);
                        // Audit log removed - service not available
                    }
                }
                // Audit log removed - service not available
                invoiceRepository.delete(invoice);
            }
        }
        Convention convention = getConventionById(id);
        // Audit log removed - service not available
        conventionRepository.delete(convention);
    }

    public List<Convention> getConventionsByStatus(String status) {
        return conventionRepository.findByStatus(status);
    }

    public List<Convention> getConventionsByCommercial(String commercialId) {
        return conventionRepository.findByCommercial(commercialId);
    }

    public List<Convention> getConventionsByStructure(String structure) {
        return conventionRepository.findByStructureId(structure);
    }

    public List<Convention> getConventionsByGeographicZone(String zone) {
        return conventionRepository.findByZoneGeographiqueId(zone);
    }

    public List<Convention> searchConventions(String status, String governorate, String structureId, String dateDebut, String dateFin, List<String> tags) {
        // Recherche multi-critères simple (peut être optimisée avec Criteria si besoin)
        List<Convention> all = conventionRepository.findAll();
        return all.stream()
            .filter(c -> status == null || status.isEmpty() || status.equalsIgnoreCase(c.getStatus()))
            .filter(c -> governorate == null || governorate.isEmpty() || governorate.equalsIgnoreCase(c.getGovernorate()))
            .filter(c -> structureId == null || structureId.isEmpty() || structureId.equalsIgnoreCase(c.getStructureId()))
            .filter(c -> {
                if (dateDebut == null || dateDebut.isEmpty()) return true;
                try { return !c.getStartDate().isBefore(LocalDate.parse(dateDebut)); } catch (Exception e) { return true; }
            })
            .filter(c -> {
                if (dateFin == null || dateFin.isEmpty()) return true;
                try { return !c.getEndDate().isAfter(LocalDate.parse(dateFin)); } catch (Exception e) { return true; }
            })
            .filter(c -> tags == null || tags.isEmpty() || (c.getTag() != null && c.getTag().equals(tags.get(0))))
            .toList();
    }

    public Convention addTag(String id, String tag) {
        Convention c = getConventionById(id);
        c.setTag(tag);
        c.setUpdatedAt(LocalDate.now());
        conventionRepository.save(c);
        return c;
    }

    public Convention removeTag(String id, String tag) {
        Convention c = getConventionById(id);
        if (c.getTag() != null && c.getTag().equals(tag)) {
            c.setTag(null);
            c.setUpdatedAt(LocalDate.now());
            conventionRepository.save(c);
        }
        return c;
    }

    @Autowired
    private PDFGenerationService pdfGenerationService;

    public byte[] generateConventionPDF(String id) {
        try {
            return pdfGenerationService.generateConventionPDF(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
