package com.example.demo.dto;

import lombok.Data;

@Data
public class InvoiceStatsDTO {
    private int total;
    private int paid;
    private int pending;
    private int overdue;
    private double totalAmount;
    private double paidAmount;
    private double pendingAmount;
    private double overdueAmount;
    private double averagePaymentTime;
    private double collectionRate;
}
