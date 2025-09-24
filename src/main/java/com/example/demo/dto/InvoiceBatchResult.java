package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InvoiceBatchResult {
    private boolean success;
    private int generatedCount;
    private List<String> invoiceIds;
    private List<String> errors;
    private String templateId;
    private LocalDateTime timestamp;
    private String userId;
    private boolean sendImmediately;

    public InvoiceBatchResult() {
        this.timestamp = LocalDateTime.now();
    }

    public InvoiceBatchResult(boolean success, int generatedCount) {
        this();
        this.success = success;
        this.generatedCount = generatedCount;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getGeneratedCount() {
        return generatedCount;
    }

    public void setGeneratedCount(int generatedCount) {
        this.generatedCount = generatedCount;
    }

    public List<String> getInvoiceIds() {
        return invoiceIds;
    }

    public void setInvoiceIds(List<String> invoiceIds) {
        this.invoiceIds = invoiceIds;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSendImmediately() {
        return sendImmediately;
    }

    public void setSendImmediately(boolean sendImmediately) {
        this.sendImmediately = sendImmediately;
    }
}







