package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.dto.dashboard.ComplianceRateDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    @SuppressWarnings("unused")
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private InternalCommentRepository internalCommentRepository;

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

    // ===== NOUVELLES MÉTHODES POUR STATISTIQUES COMPLÈTES =====
    
    public ProjectManagerStatsDTO getCompleteStats() {
        ProjectManagerStatsDTO stats = new ProjectManagerStatsDTO();
        
        List<Convention> conventions = conventionRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();
        
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysFromNow = now.plusDays(30);
        
        // Conventions
        stats.setTotalConventions((int) conventions.stream()
            .filter(c -> "ACTIVE".equals(c.getStatus()) || "PENDING".equals(c.getStatus()))
            .count());
        stats.setExpiredConventions((int) conventions.stream()
            .filter(c -> "EXPIRED".equals(c.getStatus()))
            .count());
        stats.setActiveConventions((int) conventions.stream()
            .filter(c -> "ACTIVE".equals(c.getStatus()))
            .count());
        
        // Échéances proches (30 jours)
        stats.setUpcomingDeadlines((int) conventions.stream()
            .filter(c -> c.getEndDate() != null)
            .filter(c -> {
                LocalDate endDate = c.getEndDate();
                return !endDate.isBefore(now) && !endDate.isAfter(thirtyDaysFromNow);
            })
            .count());
        
        // Factures
        stats.setTotalInvoices(invoices.size());
        stats.setTotalInvoicesAmount(invoices.stream()
            .mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0)
            .sum());
        stats.setOverdueInvoices((int) invoices.stream()
            .filter(i -> "OVERDUE".equals(i.getStatus()))
            .count());
        stats.setPaidInvoices((int) invoices.stream()
            .filter(i -> "PAID".equals(i.getStatus()))
            .count());
        stats.setPendingInvoices((int) invoices.stream()
            .filter(i -> "PENDING".equals(i.getStatus()))
            .count());
        
        // Pourcentage de retard
        if (stats.getTotalInvoices() > 0) {
            stats.setOverduePercentage((double) stats.getOverdueInvoices() / stats.getTotalInvoices() * 100);
        } else {
            stats.setOverduePercentage(0.0);
        }
        
        // Performance d'équipe (basée sur le taux de factures payées)
        if (stats.getTotalInvoices() > 0) {
            stats.setTeamPerformance((double) stats.getPaidInvoices() / stats.getTotalInvoices() * 100);
        } else {
            stats.setTeamPerformance(85.0); // Valeur par défaut
        }
        
        // Taux de régularisation (factures payées après avoir été en retard)
        // Pour simplifier, on utilise un calcul basé sur les factures payées
        stats.setRegularizationRate(72.0); // Valeur par défaut, à calculer avec historique
        
        // Alertes (à implémenter avec un système d'alertes)
        stats.setPendingAlerts(0);
        
        return stats;
    }
    
    // ===== GESTION DES COMMENTAIRES INTERNES =====
    
    public List<InternalCommentDTO> getAllComments() {
        List<com.example.demo.model.InternalComment> comments = internalCommentRepository.findAllByOrderByDateDesc();
        return comments.stream()
            .map(this::convertToCommentDTO)
            .collect(Collectors.toList());
    }
    
    public InternalCommentDTO addComment(InternalCommentDTO commentDTO, String currentUsername) {
        com.example.demo.model.InternalComment comment = new com.example.demo.model.InternalComment();
        comment.setAuthor(currentUsername);
        comment.setContent(commentDTO.getContent());
        comment.setDate(LocalDateTime.now());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setCreatedBy(currentUsername);
        
        // Si un commercial est mentionné
        if (commentDTO.getMentionedCommercialId() != null) {
            comment.setMentionedCommercialId(commentDTO.getMentionedCommercialId());
            Optional<User> commercial = userRepository.findById(commentDTO.getMentionedCommercialId());
            commercial.ifPresent(user -> comment.setMentionedCommercialName(user.getName()));
        }
        
        com.example.demo.model.InternalComment saved = internalCommentRepository.save(comment);
        return convertToCommentDTO(saved);
    }
    
    private InternalCommentDTO convertToCommentDTO(com.example.demo.model.InternalComment comment) {
        InternalCommentDTO dto = new InternalCommentDTO();
        dto.setId(comment.getId());
        dto.setAuthor(comment.getAuthor());
        dto.setContent(comment.getContent());
        dto.setDate(comment.getDate());
        dto.setMentionedCommercialId(comment.getMentionedCommercialId());
        dto.setMentionedCommercialName(comment.getMentionedCommercialName());
        return dto;
    }
    
    // ===== MÉTHODES POUR LES MEMBRES DE L'ÉQUIPE =====
    
    public List<User> getTeamMembersWithDetails() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> role.getName().toString().contains("COMMERCIAL")))
            .collect(Collectors.toList());
    }
    
    public List<TeamMemberStatsDTO> getTeamMembersStats() {
        List<User> commercials = userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> role.getName().toString().contains("COMMERCIAL")))
            .collect(Collectors.toList());
        
        List<Convention> allConventions = conventionRepository.findAll();
        List<Invoice> allInvoices = invoiceRepository.findAll();
        
        return commercials.stream()
            .map(commercial -> calculateCommercialStats(commercial, allConventions, allInvoices))
            .collect(Collectors.toList());
    }
    
    private TeamMemberStatsDTO calculateCommercialStats(User commercial, List<Convention> allConventions, List<Invoice> allInvoices) {
        TeamMemberStatsDTO stats = new TeamMemberStatsDTO();
        
        stats.setId(commercial.getId());
        stats.setUsername(commercial.getUsername());
        stats.setName(commercial.getName() != null ? commercial.getName() : commercial.getUsername());
        stats.setEmail(commercial.getEmail());
        stats.setRole("COMMERCIAL");
        stats.setAvatar(commercial.getAvatar());
        stats.setStatus("online");
        stats.setLastActivity(commercial.getLastLoginAt() != null ? 
            LocalDateTime.ofInstant(commercial.getLastLoginAt(), java.time.ZoneId.systemDefault()) : 
            LocalDateTime.now());
        
        List<Convention> commercialConventions = allConventions.stream()
            .filter(c -> commercial.getId().equals(c.getCreatedBy()) || commercial.getUsername().equals(c.getCreatedBy()))
            .collect(Collectors.toList());
        
        stats.setAssignedConventions(commercialConventions.size());
        stats.setActiveConventions((int) commercialConventions.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count());
        stats.setExpiredConventions((int) commercialConventions.stream().filter(c -> "EXPIRED".equals(c.getStatus())).count());
        
        Set<String> conventionIds = commercialConventions.stream().map(Convention::getId).collect(Collectors.toSet());
        
        List<Invoice> commercialInvoices = allInvoices.stream()
            .filter(i -> conventionIds.contains(i.getConventionId()))
            .collect(Collectors.toList());
        
        stats.setTotalInvoices(commercialInvoices.size());
        stats.setOverdueInvoices((int) commercialInvoices.stream().filter(i -> "OVERDUE".equals(i.getStatus())).count());
        stats.setPaidInvoices((int) commercialInvoices.stream().filter(i -> "PAID".equals(i.getStatus())).count());
        stats.setPendingInvoices((int) commercialInvoices.stream().filter(i -> "PENDING".equals(i.getStatus())).count());
        
        if (stats.getTotalInvoices() > 0) {
            stats.setPaymentRate((double) stats.getPaidInvoices() / stats.getTotalInvoices() * 100);
        } else {
            stats.setPaymentRate(0.0);
        }
        
        stats.setPerformanceScore(calculatePerformanceScore(stats));
        stats.setCurrentTask(null);
        
        return stats;
    }
    
    private double calculatePerformanceScore(TeamMemberStatsDTO stats) {
        double score = 0.0;
        score += stats.getPaymentRate() * 0.4;
        score += (stats.getActiveConventions() > 0 ? 30.0 : 0.0);
        score += (stats.getOverdueInvoices() == 0 ? 30.0 : Math.max(0, 30.0 - stats.getOverdueInvoices() * 5));
        return Math.min(100.0, score);
    }
    
    /**
     * Calcule le taux de conformité des paiements
     * Conformité = factures payées à temps / total factures
     */
    public ComplianceRateDTO calculateComplianceRate() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        
        ComplianceRateDTO dto = new ComplianceRateDTO();
        dto.setTotalInvoices(allInvoices.size());
        
        int paidOnTime = 0;
        int paidLate = 0;
        int unpaid = 0;
        int overdue = 0;
        long totalDelayDays = 0;
        int paidInvoicesCount = 0;
        
        LocalDate today = LocalDate.now();
        
        for (Invoice invoice : allInvoices) {
            if ("PAID".equals(invoice.getStatus())) {
                paidInvoicesCount++;
                
                // Vérifier si payé à temps
                if (invoice.getPaymentDate() != null && invoice.getDueDate() != null) {
                    if (invoice.getPaymentDate().isAfter(invoice.getDueDate())) {
                        // Payé en retard
                        paidLate++;
                        long delay = ChronoUnit.DAYS.between(invoice.getDueDate(), invoice.getPaymentDate());
                        totalDelayDays += delay;
                    } else {
                        // Payé à temps
                        paidOnTime++;
                    }
                } else {
                    // Pas de date de paiement, considérer comme à temps
                    paidOnTime++;
                }
            } else if ("OVERDUE".equals(invoice.getStatus())) {
                overdue++;
                unpaid++;
            } else if ("PENDING".equals(invoice.getStatus())) {
                // Vérifier si en retard
                if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(today)) {
                    overdue++;
                }
                unpaid++;
            } else {
                unpaid++;
            }
        }
        
        dto.setPaidOnTime(paidOnTime);
        dto.setPaidLate(paidLate);
        dto.setUnpaid(unpaid);
        dto.setOverdue(overdue);
        
        // Calculer le délai moyen
        if (paidLate > 0) {
            dto.setAverageDelayDays((double) totalDelayDays / paidLate);
        } else {
            dto.setAverageDelayDays(0.0);
        }
        
        // Calculer les taux
        dto.calculateRates();
        
        return dto;
    }
}
