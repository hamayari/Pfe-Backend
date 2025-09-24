package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AdminDashboardStatsDTO {
    private long totalUsers;
    private Map<String, Long> usersByRole;
    private long activeConventions;
    private long expiredConventions;
    private long totalApplications;
    private long totalZones;
    private long totalStructures;
}




