package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    private String reportType;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> metrics;
    private List<String> dimensions;
    private List<String> filters;
    private String format; // PDF, EXCEL, CSV, etc.
    private String timeZone;
    private boolean includeCharts;
    private boolean includeDataTables;
    private boolean includeExecutiveSummary;
    private boolean includeMethodology;
    private Map<String, Object> customParameters;
    private String recipientEmail;
    private boolean scheduleRecurring;
    private String frequency; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private String deliveryMethod; // EMAIL, DOWNLOAD, BOTH
}
