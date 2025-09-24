package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectManagerDashboardService {

    @Autowired
    private ConventionRepository conventionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    @SuppressWarnings("unused")
    private NotificationLogRepository notificationLogRepository;

    // Méthode principale pour obtenir l'aperçu du dashboard
    public ProjectManagerDashboardDTO getDashboardOverview(String projectManagerId, LocalDate startDate, LocalDate endDate) {
        ProjectManagerDashboardDTO dashboard = new ProjectManagerDashboardDTO();
        
        // Calculer les statistiques
        DashboardStats stats = calculateDashboardStats(startDate, endDate);
        dashboard.setStats(stats);
        
        // Obtenir les conventions récentes
        List<ConventionOverviewDTO> recentConventions = getRecentConventionsOverview(5);
        dashboard.setRecentConventions(recentConventions);
        
        // Obtenir les factures en retard
        List<InvoiceTrackingDTO> overdueInvoices = getOverdueInvoicesTracking();
        dashboard.setOverdueInvoices(overdueInvoices);
        
        return dashboard;
    }

    // Méthode pour obtenir les statistiques du dashboard
    public DashboardStats getDashboardStats(LocalDate startDate, LocalDate endDate) {
        return calculateDashboardStats(startDate, endDate);
    }

    // Méthode pour obtenir l'aperçu des conventions
    public List<ConventionOverviewDTO> getConventionsOverview(String status, String structure, String governorate, String commercial, int page, int size) {
        List<Convention> conventions = conventionRepository.findAll();
        
        return conventions.stream()
                .filter(convention -> status == null || convention.getStatus().equals(status))
                .filter(convention -> structure == null || convention.getStructureId().equals(structure))
                .filter(convention -> governorate == null || convention.getGovernorate().equals(governorate))
                .skip(page * size)
                .limit(size)
                .map(this::convertToConventionOverviewDTO)
                .collect(Collectors.toList());
    }

    // Méthode pour obtenir le suivi des factures
    public List<InvoiceTrackingDTO> getInvoicesTracking(String status, String structure, LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findAll();
        
        return invoices.stream()
                .filter(invoice -> status == null || invoice.getStatus().equals(status))
                .filter(invoice -> structure == null || invoice.getConventionId().equals(structure)) // Utiliser conventionId au lieu de structureId
                .filter(invoice -> startDate == null || invoice.getDueDate().isAfter(startDate))
                .filter(invoice -> endDate == null || invoice.getDueDate().isBefore(endDate))
                .map(this::convertToInvoiceTrackingDTO)
                .collect(Collectors.toList());
    }

    // Méthode pour obtenir la carte de chaleur régionale
    public RegionalHeatmapDTO getRegionalHeatmap(LocalDate startDate, LocalDate endDate) {
        RegionalHeatmapDTO heatmap = new RegionalHeatmapDTO();
        heatmap.setRegion("Tunisie");
        heatmap.setGovernorate("Tous");
        
        // Calculer les statistiques par région
        List<Convention> conventions = conventionRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();
        
        heatmap.setTotalConventions(conventions.size());
        heatmap.setActiveConventions((int) conventions.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count());
        heatmap.setExpiredConventions((int) conventions.stream().filter(c -> "EXPIRED".equals(c.getStatus())).count());
        
        heatmap.setTotalInvoices(invoices.size());
        heatmap.setPaidInvoices((int) invoices.stream().filter(i -> "PAID".equals(i.getStatus())).count());
        heatmap.setPendingInvoices((int) invoices.stream().filter(i -> "PENDING".equals(i.getStatus())).count());
        heatmap.setOverdueInvoices((int) invoices.stream().filter(i -> "OVERDUE".equals(i.getStatus())).count());
        
        return heatmap;
    }

    // Méthode pour obtenir la collaboration d'équipe
    public TeamCollaborationDTO getTeamCollaboration(String projectManagerId) {
        TeamCollaborationDTO collaboration = new TeamCollaborationDTO();
        
        // Obtenir les membres de l'équipe
        List<TeamMember> teamMembers = getTeamMembers();
        collaboration.setTeamMembers(teamMembers);
        
        // Obtenir les commentaires internes récents
        List<InternalComment> recentComments = getRecentInternalComments();
        collaboration.setRecentComments(recentComments);
        
        // Obtenir les éléments d'escalade actifs
        List<EscalationItem> activeEscalations = getActiveEscalationItemsForTeam();
        collaboration.setActiveEscalations(activeEscalations);
        
        return collaboration;
    }

    // Méthode pour obtenir l'historique d'escalade
    public EscalationWorkflowDTO getEscalationHistory(String escalationId, String conventionId) {
        EscalationWorkflowDTO escalation = new EscalationWorkflowDTO();
        escalation.setId(escalationId);
        escalation.setTitle("Escalation Example");
        escalation.setDescription("Description de l'escalade");
        escalation.setLevel("HIGH");
        escalation.setStatus("ACTIVE");
        escalation.setAssignedTo("Commercial");
        escalation.setCreatedAt(LocalDateTime.now());
        escalation.setDueDate(LocalDateTime.now().plusDays(7));
        escalation.setConventionId(conventionId);
        escalation.setConventionReference("CONV-001");
        
        return escalation;
    }

    // Méthodes privées utilitaires
    private DashboardStats calculateDashboardStats(LocalDate startDate, LocalDate endDate) {
        DashboardStats stats = new DashboardStats();
        
        List<Convention> conventions = conventionRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();
        
        stats.setTotalConventions(conventions.size());
        stats.setActiveConventions((int) conventions.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count());
        stats.setExpiredConventions((int) conventions.stream().filter(c -> "EXPIRED".equals(c.getStatus())).count());
        
        stats.setTotalInvoices(invoices.size());
        stats.setPaidInvoices((int) invoices.stream().filter(i -> "PAID".equals(i.getStatus())).count());
        stats.setPendingInvoices((int) invoices.stream().filter(i -> "PENDING".equals(i.getStatus())).count());
        stats.setOverdueInvoices((int) invoices.stream().filter(i -> "OVERDUE".equals(i.getStatus())).count());
        
        return stats;
    }

    private List<ConventionOverviewDTO> getRecentConventionsOverview(int limit) {
        List<Convention> conventions = conventionRepository.findAll();
        
        return conventions.stream()
                .limit(limit)
                .map(this::convertToConventionOverviewDTO)
                .collect(Collectors.toList());
    }

    private List<InvoiceTrackingDTO> getOverdueInvoicesTracking() {
        List<Invoice> invoices = invoiceRepository.findAll();
        
        return invoices.stream()
                .filter(invoice -> "OVERDUE".equals(invoice.getStatus()))
                .map(this::convertToInvoiceTrackingDTO)
                .collect(Collectors.toList());
    }

    private ConventionOverviewDTO convertToConventionOverviewDTO(Convention convention) {
        ConventionOverviewDTO dto = new ConventionOverviewDTO();
        dto.setId(convention.getId());
        dto.setReference(convention.getReference());
        dto.setLabel(convention.getReference()); // Utiliser reference au lieu de label
        dto.setStartDate(convention.getStartDate());
        dto.setEndDate(convention.getEndDate());
        dto.setStructureName(convention.getStructureId());
        dto.setGovernorate(convention.getGovernorate());
        dto.setStatus(convention.getStatus());
        dto.setAmount(convention.getAmount());
        dto.setCommercialName("Commercial Name");
        dto.setDaysUntilExpiry(0);
        dto.setOverdue(false);
        return dto;
    }

    private InvoiceTrackingDTO convertToInvoiceTrackingDTO(Invoice invoice) {
        InvoiceTrackingDTO dto = new InvoiceTrackingDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setConventionReference(invoice.getConventionId());
        dto.setDueDate(invoice.getDueDate());
        dto.setAmount(invoice.getAmount());
        dto.setStatus(invoice.getStatus());
        dto.setStructureName(invoice.getConventionId()); // Utiliser conventionId au lieu de structureId
        dto.setCommercialName("Commercial Name");
        dto.setDaysOverdue(0);
        dto.setOverdue("OVERDUE".equals(invoice.getStatus()));
        return dto;
    }

    private List<TeamMember> getTeamMembers() {
        List<User> users = userRepository.findAll();
        
        return users.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> 
                    role.getName().toString().contains("COMMERCIAL")))
                .map(this::convertUserToTeamMember)
                .collect(Collectors.toList());
    }

    private TeamMember convertUserToTeamMember(User user) {
        TeamMember member = new TeamMember();
        member.setId(user.getId());
        member.setUsername(user.getUsername());
        member.setName(user.getName());
        member.setRole("COMMERCIAL");
        member.setStatus("ONLINE");
        member.setAvatar(user.getAvatar());
        member.setLastActive(LocalDateTime.now());
        member.setAssignedConventions(0);
        member.setCompletedTasks(0);
        return member;
    }

    private List<InternalComment> getRecentInternalComments() {
        List<Message> messages = messageRepository.findAll();
        
        return messages.stream()
                .limit(10)
                .map(this::convertMessageToInternalComment)
                .collect(Collectors.toList());
    }

    private InternalComment convertMessageToInternalComment(Message message) {
        InternalComment comment = new InternalComment();
        comment.setId(message.getId());
        comment.setAuthorId(message.getSenderId());
        comment.setAuthorName("Author Name");
        comment.setContent(message.getContent());
        comment.setTimestamp(message.getSentAt());
        comment.setConventionId("CONV-001");
        comment.setConventionReference("CONV-001");
        comment.setUrgent(false);
        return comment;
    }

    private List<EscalationItem> getActiveEscalationItemsForTeam() {
        List<EscalationItem> items = new ArrayList<>();
        
        EscalationItem item = new EscalationItem();
        item.setId("ESC-001");
        item.setTitle("Escalation Example");
        item.setDescription("Description de l'escalade");
        item.setLevel("HIGH");
        item.setAssignedTo("Commercial");
        item.setCreatedAt(LocalDateTime.now());
        item.setDueDate(LocalDateTime.now().plusDays(7));
        item.setStatus("ACTIVE");
        item.setConventionId("CONV-001");
        item.setConventionReference("CONV-001");
        
        items.add(item);
        return items;
    }
}
