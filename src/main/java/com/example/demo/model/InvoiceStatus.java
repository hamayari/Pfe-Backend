package com.example.demo.model;

public enum InvoiceStatus {
    PENDING,
    PAID,
    OVERDUE,
    CANCELLED,
    PROOF_PENDING, // Preuve en attente de validation
    PROOF_REJECTED, // Preuve rejetée
    PROOF_VALIDATED, // Preuve validée (statut intermédiaire avant PAID)
    PENDING_VERIFICATION
}
