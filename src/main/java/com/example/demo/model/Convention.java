package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Data
@Document(collection = "conventions")
public class Convention {
    @Id
    private String id;
    private String reference;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Structure and location
    private String structureId;
    private String zoneGeographiqueId;
    private String governorate;
    
    // Financial information
    private PaymentTerms paymentTerms;
    private BigDecimal amount;
    private String paymentStatus;
    
    // Application details
    private String applicationId;
    private String applicationVersion;
    
    // Tracking
    private String status;
    private List<String> comments = new ArrayList<>();
    private String tag;
    private LocalDate dueDate;
    private String description;
    
    // Audit fields
    private String createdBy;
    private String commercial;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private String lastModifiedBy;
    
    // Manual getters and setters to ensure they exist
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStructureId() {
        return structureId;
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    public String getZoneGeographiqueId() {
        return zoneGeographiqueId;
    }

    public void setZoneGeographiqueId(String zoneGeographiqueId) {
        this.zoneGeographiqueId = zoneGeographiqueId;
    }

    public String getGovernorate() {
        return governorate;
    }

    public void setGovernorate(String governorate) {
        this.governorate = governorate;
    }

    public PaymentTerms getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(PaymentTerms paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCommercial() {
        return commercial;
    }

    public void setCommercial(String commercial) {
        this.commercial = commercial;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
    
    public void addComment(String comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }
    
    public enum ConventionStatus {
        DRAFT,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
    
    public enum PaymentStatus {
        PENDING,
        PARTIAL,
        COMPLETED,
        OVERDUE
    }

    private String client;
    private String type;

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    private List<LocalDate> echeances;
    public List<LocalDate> getEcheances() { return echeances; }
    public void setEcheances(List<LocalDate> echeances) { this.echeances = echeances; }
}
