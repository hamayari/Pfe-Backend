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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConventionService {

    private static final Logger logger = LoggerFactory.getLogger(ConventionService.class);

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
    private UserRepository userRepository;
    
    @Autowired
    private RealTimeNotificationService realTimeNotificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private AccessControlService accessControlService;
    
    /**
     * Récupère les conventions selon le rôle de l'utilisateur connecté
     * - COMMERCIAL: Uniquement ses propres conventions
     * - CHEF DE PROJET: Toutes les conventions
     * - DÉCIDEUR: Toutes les conventions
     * - ADMIN: Toutes les conventions
     */
    public List<Convention> getConventionsForCurrentUser() {
        System.out.println("========================================");
        System.out.println("📋 [GET CONVENTIONS] Récupération des conventions selon le rôle");
        
        // Log des informations de l'utilisateur
        accessControlService.logCurrentUserInfo();
        
        List<Convention> conventions;
        
        if (accessControlService.canViewAllData()) {
            // Chef de projet, Décideur, Admin: Voir TOUTES les conventions
            System.out.println("✅ Utilisateur autorisé à voir TOUTES les conventions");
            conventions = conventionRepository.findAll();
        } else if (accessControlService.canViewOnlyOwnData()) {
            // Commercial: Voir UNIQUEMENT ses propres conventions
            String currentUsername = accessControlService.getCurrentUsername();
            System.out.println("⚠️  Commercial - Filtrage par createdBy: " + currentUsername);
            conventions = conventionRepository.findByCreatedBy(currentUsername);
        } else {
            // Utilisateur non authentifié ou sans rôle
            System.out.println("❌ Utilisateur non autorisé");
            conventions = new ArrayList<>();
        }
        
        System.out.println("📊 Nombre de conventions retournées: " + conventions.size());
        System.out.println("========================================");
        
        return conventions;
    }
    
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
            logger.info("Génération des échéances: {} paiements, intervalle: {} jours", numberOfPayments, intervalDays);
            
            List<LocalDate> echeances = new ArrayList<>();
            LocalDate current = request.getStartDate().toLocalDate();
            for (int i = 0; i < numberOfPayments; i++) {
                echeances.add(current);
                logger.debug("Échéance {}: {}", (i+1), current);
                current = current.plusDays(intervalDays);
            }
            // S'assurer que la dernière échéance ne dépasse pas la date de fin
            echeances = echeances.stream().filter(d -> !d.isAfter(request.getEndDate().toLocalDate())).collect(Collectors.toList());
            convention.setEcheances(echeances);
            logger.info("{} échéances générées pour la convention {}", echeances.size(), request.getReference());
        } else {
            logger.warn("Impossible de générer les échéances - PaymentTerms: {}, StartDate: {}, EndDate: {}",
                (request.getPaymentTerms() != null ? "présent" : "absent"),
                (request.getStartDate() != null ? "présent" : "absent"),
                (request.getEndDate() != null ? "présent" : "absent"));
        }
        Convention savedConvention = conventionRepository.save(convention);
        logger.info("Convention sauvegardée - ID: {}, Référence: {}, Créée par: {}",
            savedConvention.getId(), savedConvention.getReference(), savedConvention.getCreatedBy());

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
            logger.info("Notification interne envoyée pour la convention {}", savedConvention.getReference());
            
            // 2. Email et SMS réels
            User commercial = userRepository.findById(userId).orElse(null);
            if (commercial == null) {
                // Essayer de trouver par username si l'ID ne fonctionne pas
                commercial = userRepository.findByUsername(userId).orElse(null);
            }
            
            logger.debug("User trouvé: {}, ID: {}, Email: {}, Phone: {}",
                (commercial != null ? commercial.getUsername() : "NULL"),
                userId,
                (commercial != null ? commercial.getEmail() : "NULL"),
                (commercial != null ? commercial.getPhoneNumber() : "NULL"));
            
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
                    logger.info("Email envoyé à {} pour la convention {}", testEmail, savedConvention.getReference());
                } catch (Exception e) {
                    logger.error("Erreur envoi email convention: {}", e.getMessage(), e);
                }
                
                // SMS
                try {
                    String phoneNumber = commercial.getPhoneNumber();
                    if (phoneNumber == null || phoneNumber.isEmpty()) {
                        logger.warn("L'utilisateur {} n'a pas de numéro de téléphone configuré", commercial.getUsername());
                    } else {
                        Map<String, String> smsVariables = new HashMap<>();
                        smsVariables.put("conventionReference", savedConvention.getReference());
                        smsVariables.put("amount", String.valueOf(savedConvention.getAmount()));
                        
                        smsService.sendSmsWithTemplate(phoneNumber, "convention_created", smsVariables);
                        logger.info("SMS envoyé au {} pour la convention {}", phoneNumber, savedConvention.getReference());
                    }
                } catch (Exception e) {
                    logger.error("Erreur envoi SMS convention: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur envoi notification convention: {}", e.getMessage(), e);
        }

        // Debug des PaymentTerms
        logger.debug("PaymentTerms: {}", (request.getPaymentTerms() != null ? "présent" : "absent"));
        if (request.getPaymentTerms() != null) {
            logger.debug("Nombre de paiements: {}, Intervalle: {} jours",
                request.getPaymentTerms().getNumberOfPayments(),
                request.getPaymentTerms().getIntervalDays());
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
        
        System.out.println("🔄 Mise à jour de la convention ID: " + id);
        System.out.println("📋 Nouvelles données - Référence: " + request.getReference());
        System.out.println("📋 Nouvelles données - Titre: " + request.getTitle());
        System.out.println("📋 Nouvelles données - Zone géographique: " + request.getGeographicZone());
        
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
        convention.setClient(request.getClient());
        convention.setType(request.getType());
        convention.setStatus(request.getStatus());
        convention.setTag(request.getTag());
        convention.setUpdatedAt(LocalDate.now());
        
        // Correction : remplir dueDate
        if (request.getDueDate() != null) {
            convention.setDueDate(request.getDueDate());
        } else if (request.getEndDate() != null) {
            convention.setDueDate(request.getEndDate().toLocalDate());
        }

        Convention savedConvention = conventionRepository.save(convention);
        System.out.println("✅ Convention mise à jour avec succès - ID: " + savedConvention.getId());
        System.out.println("✅ Gouvernorat sauvegardé: " + savedConvention.getGovernorate());
        
        return savedConvention;
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

    /**
     * Enrichit les conventions avec le nom complet du commercial qui les a créées
     */
    public List<Convention> enrichConventionsWithCommercialNames(List<Convention> conventions) {
        logger.info("🔍 Enrichissement de {} conventions avec les noms des commerciaux", conventions.size());
        
        for (Convention convention : conventions) {
            if (convention.getCreatedBy() != null && !convention.getCreatedBy().isEmpty()) {
                try {
                    // Chercher l'utilisateur par username (createdBy contient le username)
                    User commercial = userRepository.findByUsername(convention.getCreatedBy()).orElse(null);
                    
                    if (commercial != null) {
                        // Utiliser le nom complet s'il existe, sinon le username
                        String commercialName = commercial.getName() != null && !commercial.getName().isEmpty() 
                            ? commercial.getName() 
                            : commercial.getUsername();
                        
                        convention.setCommercial(commercialName);
                        logger.debug("✅ Convention {}: Commercial = {}", convention.getReference(), commercialName);
                    } else {
                        // Si l'utilisateur n'est pas trouvé, garder le username
                        convention.setCommercial(convention.getCreatedBy());
                        logger.warn("⚠️  Utilisateur non trouvé pour: {}", convention.getCreatedBy());
                    }
                } catch (Exception e) {
                    logger.error("❌ Erreur lors de la récupération du commercial pour {}: {}", 
                        convention.getCreatedBy(), e.getMessage());
                    convention.setCommercial(convention.getCreatedBy());
                }
            } else {
                convention.setCommercial("N/A");
            }
        }
        
        logger.info("✅ Enrichissement terminé");
        return conventions;
    }
}
