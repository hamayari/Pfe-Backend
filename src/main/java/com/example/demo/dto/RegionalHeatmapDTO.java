package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RegionalHeatmapDTO {
    private String region;
    private String governorate;
    private int totalConventions;
    private int activeConventions;
    private int expiredConventions;
    private int totalInvoices;
    private int paidInvoices;
    private int pendingInvoices;
    private int overdueInvoices;
    private double totalRevenue;
    private double pendingRevenue;
    private Map<String, Integer> conventionsByStatus;
    private Map<String, Integer> invoicesByStatus;
}































