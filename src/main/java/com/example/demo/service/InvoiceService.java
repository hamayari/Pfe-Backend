package com.example.demo.service;

import com.example.demo.dto.invoice.InvoiceRequest;
import com.example.demo.model.Invoice;


import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(InvoiceRequest request, String userId);
    Invoice getInvoiceById(String id);
    List<Invoice> getAllInvoices();
    List<Invoice> getInvoicesByUser(String username);  // ✅ AJOUTÉ
    Invoice updateInvoiceStatus(String id, String status);
    Invoice updateInvoiceStatusWithAudit(String invoiceId, String status, String commercialId, String commercialName);
    List<Invoice> getInvoicesByConvention(String conventionId);
    List<Invoice> getOverdueInvoices();
    byte[] generateInvoicePDF(String invoiceId);
    void sendReminder(String invoiceId, String type);
    List<String> getInvoiceReminders(String invoiceId);
    List<Invoice> getInvoicesByClient(String clientId);
    void save(Invoice invoice);
    void deleteInvoice(String invoiceId);
    int deleteAllInvoices();
}
