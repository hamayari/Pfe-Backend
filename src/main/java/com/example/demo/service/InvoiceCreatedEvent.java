package com.example.demo.service;

import com.example.demo.model.Invoice;
import org.springframework.context.ApplicationEvent;

public class InvoiceCreatedEvent extends ApplicationEvent {
    private final Invoice invoice;

    public InvoiceCreatedEvent(Object source, Invoice invoice) {
        super(source);
        this.invoice = invoice;
    }

    public Invoice getInvoice() {
        return invoice;
    }
} 