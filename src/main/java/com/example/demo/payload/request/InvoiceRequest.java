package com.example.demo.payload.request;

import java.math.BigDecimal;

public class InvoiceRequest {
    private String reference;
    private Long conventionId;
    private String clientEmail;
    private String description;
    private BigDecimal amount;
    private String dueDate;
    private String notes;

    public InvoiceRequest() {}

    public InvoiceRequest(String reference, Long conventionId, BigDecimal amount) {
        this.reference = reference;
        this.conventionId = conventionId;
        this.amount = amount;
    }

    // Getters and Setters
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Long getConventionId() {
        return conventionId;
    }

    public void setConventionId(Long conventionId) {
        this.conventionId = conventionId;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
















































