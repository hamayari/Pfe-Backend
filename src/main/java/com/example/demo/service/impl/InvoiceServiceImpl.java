package com.example.demo.service.impl;

import com.example.demo.dto.invoice.InvoiceRequest;
import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.service.InvoiceService;
import com.example.demo.service.InvoiceNumberGenerator;
import com.example.demo.service.PDFGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PDFGenerationService pdfGenerationService;
    
    @Autowired
    private InvoiceNumberGenerator invoiceNumberGenerator;
    
    @Autowired
    private com.example.demo.service.AccessControlService accessControlService;

    /**
     * Récupère les factures selon le rôle de l'utilisateur connecté
     * - COMMERCIAL: Uniquement ses propres factures
     * - CHEF DE PROJET: Toutes les factures
     * - DÉCIDEUR: Toutes les factures
     * - ADMIN: Toutes les factures
     */
    public List<Invoice> getInvoicesForCurrentUser() {
        System.out.println("========================================");
        System.out.println("💰 [GET INVOICES] Récupération des factures selon le rôle");
        
        // Log des informations de l'utilisateur
        accessControlService.logCurrentUserInfo();
        
        List<Invoice> invoices;
        
        if (accessControlService.canViewAllData()) {
            // Chef de projet, Décideur, Admin: Voir TOUTES les factures
            System.out.println("✅ Utilisateur autorisé à voir TOUTES les factures");
            invoices = invoiceRepository.findAll();
        } else if (accessControlService.canViewOnlyOwnData()) {
            // Commercial: Voir UNIQUEMENT ses propres factures
            String currentUsername = accessControlService.getCurrentUsername();
            System.out.println("⚠️  Commercial - Filtrage par createdBy: " + currentUsername);
            invoices = invoiceRepository.findByCreatedBy(currentUsername);
        } else {
            // Utilisateur non authentifié ou sans rôle
            System.out.println("❌ Utilisateur non autorisé");
            invoices = new java.util.ArrayList<>();
        }
        
        System.out.println("📊 Nombre de factures retournées: " + invoices.size());
        System.out.println("========================================");
        
        return invoices;
    }

    @Override
    public Invoice createInvoice(InvoiceRequest request, String userId) {
        Invoice invoice = new Invoice();
        
        // Générer automatiquement le numéro de facture
        String invoiceNumber = invoiceNumberGenerator.generateInvoiceNumber();
        invoice.setInvoiceNumber(invoiceNumber);
        
        invoice.setConventionId(request.getConventionId());
        invoice.setReference(request.getReference());
        invoice.setAmount(request.getAmount());
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(request.getDueDate() != null ? request.getDueDate().toLocalDate() : LocalDate.now());
        invoice.setStatus("PENDING");
        invoice.setCreatedBy(userId);
        invoice.setCreatedAt(LocalDate.now());
        
        System.out.println("✅ Facture créée avec le numéro: " + invoiceNumber);
        
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoiceById(String id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    
    @Override
    public List<Invoice> getInvoicesByUser(String username) {
        System.out.println("📋 getInvoicesByUser - Filtrage par createdBy: " + username);
        List<Invoice> invoices = invoiceRepository.findByCreatedBy(username);
        System.out.println("✅ " + invoices.size() + " factures trouvées pour " + username);
        return invoices;
    }

    @Override
    public Invoice updateInvoiceStatus(String id, String status) {
        Invoice invoice = getInvoiceById(id);
        if (invoice != null) {
            invoice.setStatus(status);
            invoice.setUpdatedAt(LocalDate.now());
            
            // Si le statut est PAID, mettre à jour la date de paiement
            if ("PAID".equals(status)) {
                invoice.setPaymentDate(LocalDate.now());
            }
            
            return invoiceRepository.save(invoice);
        }
        return null;
    }

    @Override
    public Invoice updateInvoiceStatusWithAudit(String invoiceId, String status, String commercialId, String commercialName) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice != null) {
            invoice.setStatus(status);
            invoice.setLastModifiedBy(commercialId);
            invoice.setUpdatedAt(LocalDate.now());
            
            // Si le statut est PAID, mettre à jour la date de paiement
            if ("PAID".equals(status)) {
                invoice.setPaymentDate(LocalDate.now());
                invoice.setValidatedBy(commercialName);
                invoice.setValidatedAt(java.time.LocalDateTime.now());
            }
            
            return invoiceRepository.save(invoice);
        }
        return null;
    }

    @Override
    public List<Invoice> getInvoicesByConvention(String conventionId) {
        return invoiceRepository.findByConventionId(conventionId);
    }

    @Override
    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findByDueDateBeforeAndStatus(LocalDate.now(), "PENDING");
    }

    @Override
    public byte[] generateInvoicePDF(String invoiceId) {
        try {
            System.out.println("📄 Génération PDF pour la facture ID: " + invoiceId);
            
            // Vérifier que la facture existe
            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            if (invoice == null) {
                System.err.println("❌ Facture non trouvée avec l'ID: " + invoiceId);
                return new byte[0];
            }
            
            System.out.println("✅ Facture trouvée: " + invoice.getReference() + " - Montant: " + invoice.getAmount());
            
            byte[] pdfBytes = pdfGenerationService.generateInvoicePDF(invoiceId);
            System.out.println("✅ PDF généré - Taille: " + pdfBytes.length + " bytes");
            
            return pdfBytes;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du PDF pour la facture " + invoiceId + ": " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void sendReminder(String invoiceId, String type) {
        // Implémentation temporaire
    }

    @Override
    public List<String> getInvoiceReminders(String invoiceId) {
        // Implémentation temporaire
        return List.of();
    }

    @Override
    public List<Invoice> getInvoicesByClient(String clientId) {
        return invoiceRepository.findByClientId(clientId);
    }

    @Override
    public void save(Invoice invoice) {
        invoiceRepository.save(invoice);
    }

    @Override
    public void deleteInvoice(String invoiceId) {
        invoiceRepository.deleteById(invoiceId);
    }

    @Override
    public int deleteAllInvoices() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        int count = allInvoices.size();
        invoiceRepository.deleteAll();
        return count;
    }
}
