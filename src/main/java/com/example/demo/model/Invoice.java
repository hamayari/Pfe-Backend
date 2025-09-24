package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "invoices")
public class Invoice {
    @Id
    private String id;
    private String conventionId;
    private String reference;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private String status;
    private List<NotificationLog> sentNotifications;
    private String createdBy;
    private String invoiceNumber;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private String comments;
    
    // Payment tracking
    private String paymentReference;
    private String paymentMethod;
    private BigDecimal paidAmount;
    
    // Ajout pour paiement partiel
    private BigDecimal partialPaidAmount;

    // Audit fields
    private String lastModifiedBy;
    
    // Nouveaux champs pour le système de confirmation de paiement automatisé
    private String clientEmail;
    private String clientId;
    private LocalDateTime sentToClientAt;
    private String sentBy;
    private List<PaymentProof> paymentProofs;
    private LocalDateTime lastProofUploadedAt;
    private String lastProofUploadedBy;
    private String validationNotes;
    private String validatedBy;
    private LocalDateTime validatedAt;
    private String rejectionReason;
    private LocalDateTime rejectedAt;
    private String rejectedBy;
    
    // Configuration pour validation automatique
    private Boolean autoValidationEnabled;
    private String autoValidationRules; // JSON string pour les règles
    
    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) && status == "PENDING";
    }
    
    public boolean isPaid() {
        return status == "PAID";
    }
    
    public void markAsPaid() {
        this.status = "PAID";
        this.paymentDate = LocalDate.now();
    }
    
    public boolean hasProofPending() {
        return "PROOF_PENDING".equals(status);
    }
    
    public boolean hasProofUploaded() {
        return paymentProofs != null && !paymentProofs.isEmpty();
    }
    
    public PaymentProof getLatestProof() {
        if (paymentProofs == null || paymentProofs.isEmpty()) {
            return null;
        }
        return paymentProofs.stream()
                .max((p1, p2) -> p1.getUploadedAt().compareTo(p2.getUploadedAt()))
                .orElse(null);
    }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setStatus(InvoiceStatus status) { this.status = status != null ? status.name() : null; }

    public BigDecimal getPartialPaidAmount() {
        return partialPaidAmount;
    }
    public void setPartialPaidAmount(BigDecimal partialPaidAmount) {
        this.partialPaidAmount = partialPaidAmount;
    }
}
