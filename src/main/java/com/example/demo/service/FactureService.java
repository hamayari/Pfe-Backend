package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentProofRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream;

@Service
public class FactureService {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private PDFGenerationService pdfGenerationService;
    @Autowired
    private PaymentProofRepository paymentProofRepository;

    public List<Map<String, Object>> getFactures() {
        return invoiceRepository.findAll().stream().map(this::toFactureMap).collect(Collectors.toList());
    }

    private Map<String, Object> toFactureMap(Invoice invoice) {
        Map<String, Object> f = new HashMap<>();
        f.put("id", invoice.getId());
        f.put("reference", invoice.getReference());
        f.put("structure", invoice.getConventionId()); // Adapter si structure séparée
        f.put("montant", invoice.getAmount());
        f.put("echeance", invoice.getDueDate());
        f.put("statut", invoice.getStatus());
        boolean preuveDispo = false;
        String preuveStatus = "AUCUNE";
        try {
            var proofs = paymentProofRepository.findByInvoiceId(invoice.getId());
            preuveDispo = proofs.size() > 0;
            if (preuveDispo) {
                // Prendre le PaymentProof le plus récent
                var latest = proofs.stream().max((a, b) -> a.getUploadedAt().compareTo(b.getUploadedAt())).orElse(null);
                if (latest != null) {
                    preuveStatus = latest.getStatus();
                }
            }
        } catch (Exception e) {
            preuveDispo = false;
            preuveStatus = "AUCUNE";
        }
        f.put("preuveDisponible", preuveDispo);
        f.put("preuveStatus", preuveStatus);
        f.put("recuDisponible", "PAID".equals(invoice.getStatus()));
        return f;
    }

    public Map<String, Object> getPreuve(String id) {
        // À remplacer par la génération dynamique du PDF de preuve
        return Map.of("url", "/api/factures/" + id + "/preuve-pdf");
    }

    public Map<String, Object> getRecu(String id) {
        // À remplacer par la génération dynamique du PDF de reçu
        return Map.of("url", "/api/factures/" + id + "/recu-pdf");
    }

    public byte[] generatePreuvePdf(String id) throws IOException {
        // Pour l'exemple, on utilise la génération de facture PDF existante
        // À adapter si besoin d'un format spécifique pour la preuve
        return pdfGenerationService.generateInvoicePDF(id);
    }

    public byte[] generateRecuPdf(String id) throws IOException {
        // Pour l'exemple, on utilise la génération de facture PDF existante
        // À adapter si besoin d'un format spécifique pour le reçu
        return pdfGenerationService.generateInvoicePDF(id);
    }

    public Map<String, Object> getStats() {
        List<Invoice> factures = invoiceRepository.findAll();
        int total = factures.size();
        int payees = (int) factures.stream().filter(f -> "PAID".equals(f.getStatus())).count();
        double montantTotal = factures.stream().mapToDouble(f -> f.getAmount() != null ? f.getAmount().doubleValue() : 0.0).sum();
        double montantEncaisse = factures.stream().filter(f -> "PAID".equals(f.getStatus())).mapToDouble(f -> f.getAmount() != null ? f.getAmount().doubleValue() : 0.0).sum();
        int retards = (int) factures.stream().filter(f -> "NON PAYÉE".equals(f.getStatus())).count();
        return Map.of(
            "tauxPaiement", total == 0 ? 0 : (double) payees / total,
            "montantTotal", montantTotal,
            "montantEncaisse", montantEncaisse,
            "retards", retards
        );
    }
} 