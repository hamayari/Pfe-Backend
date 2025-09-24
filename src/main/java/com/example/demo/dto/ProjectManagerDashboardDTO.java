package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProjectManagerDashboardDTO {
    private DashboardStats stats;
    private List<ConventionOverviewDTO> recentConventions;
    private List<InvoiceTrackingDTO> overdueInvoices;
    private RegionalHeatmapDTO regionalHeatmap;
    private TeamCollaborationDTO teamCollaboration;
    private EscalationWorkflowDTO escalationWorkflow;
    private List<String> realTimeAlerts;
    private List<String> supervisionKPIs;
    private List<String> exportReports;
    private List<String> teamPerformance;
    private List<String> riskAssessment;
}
