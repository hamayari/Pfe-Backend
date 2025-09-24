package com.example.demo.service.impl;

import com.example.demo.dto.invoice.InvoiceRequest;
import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.service.InvoiceService;
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

    @Override
    public Invoice createInvoice(InvoiceRequest request, String userId) {
        Invoice invoice = new Invoice();
        invoice.setConventionId(request.getConventionId());
        invoice.setReference(request.getReference());
        invoice.setAmount(request.getAmount());
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(request.getDueDate() != null ? request.getDueDate().toLocalDate() : LocalDate.now());
        invoice.setStatus("PENDING");
        invoice.setCreatedBy(userId);
        invoice.setCreatedAt(LocalDate.now());
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
    public Invoice updateInvoiceStatus(String id, String status) {
        Invoice invoice = getInvoiceById(id);
        if (invoice != null) {
            invoice.setStatus(status);
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
