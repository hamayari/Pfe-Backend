package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class NotificationStatsDTO {
    private long total;
    private long unread;
    private long unacknowledged;
    private Map<String, Long> byCategory;
    private Map<String, Long> byPriority;
    private double readRate;
    private double acknowledgmentRate;
}







































