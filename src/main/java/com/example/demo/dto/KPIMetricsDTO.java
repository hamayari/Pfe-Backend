package com.example.demo.dto;

import lombok.Data;

@Data
public class KPIMetricsDTO {
    private int totalConventions;
    private int activeConventions;
    private int expiredConventions;
    private int totalInvoices;
    private int paidInvoices;
    private int overdueInvoices;
    private double collectionRate;
    private double averagePaymentTime;
    private double monthlyRevenue;
    private double pendingAmount;
}







































