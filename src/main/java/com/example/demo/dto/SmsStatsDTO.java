package com.example.demo.dto;

import lombok.Data;

@Data
public class SmsStatsDTO {
    private long totalSent;
    private long totalDelivered;
    private long totalFailed;
    private double deliveryRate;
    private double failureRate;
    private double successRate;
}
