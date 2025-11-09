package com.example.demo.controller;

import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur pour afficher les détails d'une facture
 */
@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceViewController {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    /**
     * Obtenir l'aperçu complet d'une facture par son ID
     */
    @GetMapping("/{invoiceId}/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'DECISION_MAKER', 'PROJECT_MANAGER', 'COMMERCIAL')")
    public ResponseEntity<Map<String, Object>> getInvoiceOverview(@PathVariable String invoiceId) {
        try {
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
            
            if (invoiceOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Facture non trouvée");
                return ResponseEntity.status(404).body(error);
            }
            
            Invoice invoice = invoiceOpt.get();
            
            // Construire l'aperçu complet
            Map<String, Object> overview = new HashMap<>();
            overview.put("id", invoice.getId());
            overview.put("reference", invoice.getReference());
            overview.put("invoiceNumber", invoice.getInvoiceNumber());
            overview.put("conventionId", invoice.getConventionId());
            
            // Informations client
            Map<String, Object> client = new HashMap<>();
            client.put("id", invoice.getClientId());
            client.put("email", invoice.getClientEmail());
            overview.put("client", client);
            
            // Montants
            Map<String, Object> amounts = new HashMap<>();
            amounts.put("total", invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0);
            amounts.put("paid", invoice.getPaidAmount() != null ? invoice.getPaidAmount().doubleValue() : 0.0);
            amounts.put("partialPaid", invoice.getPartialPaidAmount() != null ? invoice.getPartialPaidAmount().doubleValue() : 0.0);
            double remaining = (invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0) - 
                             (invoice.getPaidAmount() != null ? invoice.getPaidAmount().doubleValue() : 0.0);
            amounts.put("remaining", remaining);
            overview.put("amounts", amounts);
            
            // Dates
            Map<String, Object> dates = new HashMap<>();
            dates.put("issue", invoice.getIssueDate());
            dates.put("due", invoice.getDueDate());
            dates.put("payment", invoice.getPaymentDate());
            dates.put("created", invoice.getCreatedAt());
            dates.put("updated", invoice.getUpdatedAt());
            dates.put("sentToClient", invoice.getSentToClientAt());
            overview.put("dates", dates);
            
            // Statuts
            overview.put("status", invoice.getStatus());
            overview.put("paymentMethod", invoice.getPaymentMethod());
            overview.put("paymentReference", invoice.getPaymentReference());
            
            // Informations de création/modification
            Map<String, Object> audit = new HashMap<>();
            audit.put("createdBy", invoice.getCreatedBy());
            audit.put("lastModifiedBy", invoice.getLastModifiedBy());
            audit.put("sentBy", invoice.getSentBy());
            audit.put("validatedBy", invoice.getValidatedBy());
            overview.put("audit", audit);
            
            // Commentaires et notes
            overview.put("comments", invoice.getComments());
            overview.put("validationNotes", invoice.getValidationNotes());
            
            // Preuves de paiement
            overview.put("paymentProofs", invoice.getPaymentProofs());
            overview.put("lastProofUploadedAt", invoice.getLastProofUploadedAt());
            overview.put("lastProofUploadedBy", invoice.getLastProofUploadedBy());
            
            // Notifications
            overview.put("sentNotifications", invoice.getSentNotifications());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("invoice", overview);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération facture: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Erreur: " + e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }
}
