package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project-manager/dashboard")
@PreAuthorize("hasRole('PROJECT_MANAGER')")
public class ProjectManagerDashboardController {

    @GetMapping("/contracts-overview")
    public String getContractsOverview() {
        return "Vue d'ensemble des contrats";
    }

    @GetMapping("/invoices-tracking")
    public String getInvoicesTracking() {
        return "Suivi des factures";
    }

    @GetMapping("/team-communication")
    public String getTeamCommunication() {
        return "Communication d'équipe";
    }
}
