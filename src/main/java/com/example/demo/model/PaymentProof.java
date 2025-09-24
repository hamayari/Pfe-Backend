package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "payment_proofs")
public class PaymentProof {
    @Id
    private String id;
    private String invoiceId;
    private String clientId;
    private String clientEmail;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private String status; // PENDING, VALIDATED, REJECTED
    private String validationNotes;
    private String validatedBy;
    private LocalDateTime validatedAt;
    private String rejectionReason;
    private LocalDateTime rejectedAt;
    private String rejectedBy;
    
    // Métadonnées du fichier
    private String originalFileName;
    private String mimeType;
    private String checksum;
    
    // Informations de paiement extraites
    private Double paymentAmount;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime paymentDate;
    
    // URI du reçu PDF archivé
    private String receiptUri;
    
    public enum ProofStatus {
        PENDING,
        VALIDATED,
        REJECTED
    }
} 