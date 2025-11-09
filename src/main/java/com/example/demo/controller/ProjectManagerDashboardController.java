package com.example.demo.controller;

import com.example.demo.dto.InternalCommentDTO;
import com.example.demo.dto.ProjectManagerStatsDTO;
import com.example.demo.dto.TeamMemberStatsDTO;
import com.example.demo.dto.dashboard.ComplianceRateDTO;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.User;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.service.ProjectManagerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pm-dashboard")
@PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectManagerDashboardController {

    @Autowired
    private ProjectManagerDashboardService dashboardService;

    @Autowired
    private ConventionRepository conventionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    /**
     * Obtenir les statistiques complètes du dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<ProjectManagerStatsDTO> getCompleteStats() {
        ProjectManagerStatsDTO stats = dashboardService.getCompleteStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtenir toutes les conventions
     */
    @GetMapping("/conventions")
    public ResponseEntity<List<Convention>> getAllConventions() {
        List<Convention> conventions = conventionRepository.findAll();
        return ResponseEntity.ok(conventions);
    }

    /**
     * Obtenir toutes les factures
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        return ResponseEntity.ok(invoices);
    }

    /**
     * Obtenir les factures en retard
     */
    @GetMapping("/invoices/overdue")
    public ResponseEntity<List<Invoice>> getOverdueInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<Invoice> overdueInvoices = invoices.stream()
            .filter(i -> "OVERDUE".equals(i.getStatus()))
            .toList();
        return ResponseEntity.ok(overdueInvoices);
    }

    /**
     * Obtenir les membres de l'équipe commerciale
     */
    @GetMapping("/team")
    public ResponseEntity<List<User>> getTeamMembers() {
        List<User> teamMembers = dashboardService.getTeamMembersWithDetails();
        return ResponseEntity.ok(teamMembers);
    }

    /**
     * Obtenir les statistiques détaillées de chaque membre de l'équipe
     */
    @GetMapping("/team/stats")
    public ResponseEntity<List<TeamMemberStatsDTO>> getTeamMembersStats() {
        List<TeamMemberStatsDTO> teamStats = dashboardService.getTeamMembersStats();
        return ResponseEntity.ok(teamStats);
    }

    /**
     * Obtenir tous les commentaires internes
     */
    @GetMapping("/comments")
    public ResponseEntity<List<InternalCommentDTO>> getAllComments() {
        List<InternalCommentDTO> comments = dashboardService.getAllComments();
        return ResponseEntity.ok(comments);
    }

    /**
     * Ajouter un commentaire interne
     */
    @PostMapping("/comments")
    public ResponseEntity<InternalCommentDTO> addComment(
            @RequestBody InternalCommentDTO commentDTO,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        InternalCommentDTO savedComment = dashboardService.addComment(commentDTO, currentUsername);
        return ResponseEntity.ok(savedComment);
    }

    /**
     * Obtenir les conventions par statut
     */
    @GetMapping("/conventions/status/{status}")
    public ResponseEntity<List<Convention>> getConventionsByStatus(@PathVariable String status) {
        List<Convention> conventions = conventionRepository.findAll();
        List<Convention> filtered = conventions.stream()
            .filter(c -> status.equalsIgnoreCase(c.getStatus()))
            .toList();
        return ResponseEntity.ok(filtered);
    }

    /**
     * Obtenir les factures par statut
     */
    @GetMapping("/invoices/status/{status}")
    public ResponseEntity<List<Invoice>> getInvoicesByStatus(@PathVariable String status) {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<Invoice> filtered = invoices.stream()
            .filter(i -> status.equalsIgnoreCase(i.getStatus()))
            .toList();
        return ResponseEntity.ok(filtered);
    }

    /**
     * Obtenir le taux de conformité des paiements
     */
    @GetMapping("/compliance-rate")
    public ResponseEntity<ComplianceRateDTO> getComplianceRate() {
        ComplianceRateDTO complianceRate = dashboardService.calculateComplianceRate();
        return ResponseEntity.ok(complianceRate);
    }
}
