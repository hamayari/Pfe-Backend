package com.example.demo.dto;

import lombok.Data;

@Data
public class DashboardStats {
    private int totalConventions;
    private int activeConventions;
    private int expiredConventions;
    private int totalInvoices;
    private int paidInvoices;
    private int pendingInvoices;
    private int overdueInvoices;
    private double totalRevenue;
    private double pendingRevenue;
    private int teamMembers;
    private int activeEscalations;
    private int resolvedEscalations;
}































