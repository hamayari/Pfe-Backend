package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.dto.dashboard.ComplianceRateDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectManagerDashboardServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private InternalCommentRepository internalCommentRepository;

    @InjectMocks
    private ProjectManagerDashboardService dashboardService;

    private Convention testConvention;
    private Invoice testInvoice;
    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testConvention = new Convention();
        testConvention.setId("CONV-001");
        testConvention.setReference("REF-001");
        testConvention.setStatus("ACTIVE");
        testConvention.setStartDate(LocalDate.now());
        testConvention.setEndDate(LocalDate.now().plusMonths(6));
        testConvention.setAmount(BigDecimal.valueOf(10000));
        testConvention.setStructureId("STRUCT-001");
        testConvention.setGovernorate("Tunis");

        testInvoice = new Invoice();
        testInvoice.setId("INV-001");
        testInvoice.setInvoiceNumber("2024-001");
        testInvoice.setAmount(BigDecimal.valueOf(1000));
        testInvoice.setStatus("PENDING");
        testInvoice.setDueDate(LocalDate.now().plusDays(30));
        testInvoice.setConventionId("CONV-001");

        testRole = new Role();
        testRole.setName(com.example.demo.enums.ERole.ROLE_COMMERCIAL);

        testUser = new User();
        testUser.setId("USER-001");
        testUser.setUsername("commercial1");
        testUser.setName("Commercial User");
        testUser.setEmail("commercial@test.com");
        testUser.setRoles(new HashSet<>(Arrays.asList(testRole)));
    }

    @Test
    void testGetDashboardOverview() {
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));

        ProjectManagerDashboardDTO result = dashboardService.getDashboardOverview("PM-001", LocalDate.now(), LocalDate.now().plusMonths(1));

        assertNotNull(result);
        assertNotNull(result.getStats());
        verify(conventionRepository, atLeastOnce()).findAll();
    }

    @Test
    void testGetDashboardStats() {
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));

        DashboardStats result = dashboardService.getDashboardStats(LocalDate.now(), LocalDate.now().plusMonths(1));

        assertNotNull(result);
        assertEquals(1, result.getTotalConventions());
        assertEquals(1, result.getActiveConventions());
        verify(conventionRepository).findAll();
    }

    @Test
    void testGetConventionsOverview() {
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention));

        List<ConventionOverviewDTO> result = dashboardService.getConventionsOverview(null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conventionRepository).findAll();
    }

    @Test
    void testGetInvoicesTracking() {
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));

        List<InvoiceTrackingDTO> result = dashboardService.getInvoicesTracking(null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceRepository).findAll();
    }

    @Test
    void testGetRegionalHeatmap() {
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));

        RegionalHeatmapDTO result = dashboardService.getRegionalHeatmap(LocalDate.now(), LocalDate.now().plusMonths(1));

        assertNotNull(result);
        assertEquals("Tunisie", result.getRegion());
        verify(conventionRepository).findAll();
    }

    @Test
    void testGetTeamCollaboration() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        TeamCollaborationDTO result = dashboardService.getTeamCollaboration("PM-001");

        assertNotNull(result);
        assertNotNull(result.getTeamMembers());
        verify(userRepository).findAll();
    }

    @Test
    void testGetEscalationHistory() {
        EscalationWorkflowDTO result = dashboardService.getEscalationHistory("ESC-001", "CONV-001");

        assertNotNull(result);
        assertEquals("ESC-001", result.getId());
        assertEquals("CONV-001", result.getConventionId());
    }

    @Test
    void testGetCompleteStats() {
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));

        ProjectManagerStatsDTO result = dashboardService.getCompleteStats();

        assertNotNull(result);
        assertTrue(result.getTotalConventions() >= 0);
        verify(conventionRepository).findAll();
    }

    @Test
    void testGetAllComments() {
        com.example.demo.model.InternalComment comment = new com.example.demo.model.InternalComment();
        comment.setId("COMMENT-001");
        comment.setAuthor("user1");
        comment.setContent("Test comment");
        comment.setDate(LocalDateTime.now());

        when(internalCommentRepository.findAllByOrderByDateDesc()).thenReturn(Arrays.asList(comment));

        List<InternalCommentDTO> result = dashboardService.getAllComments();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(internalCommentRepository).findAllByOrderByDateDesc();
    }

    @Test
    void testAddComment() {
        InternalCommentDTO commentDTO = new InternalCommentDTO();
        commentDTO.setContent("New comment");

        com.example.demo.model.InternalComment savedComment = new com.example.demo.model.InternalComment();
        savedComment.setId("COMMENT-002");
        savedComment.setAuthor("user1");
        savedComment.setContent("New comment");
        savedComment.setDate(LocalDateTime.now());

        when(internalCommentRepository.save(any(com.example.demo.model.InternalComment.class))).thenReturn(savedComment);

        InternalCommentDTO result = dashboardService.addComment(commentDTO, "user1");

        assertNotNull(result);
        assertEquals("New comment", result.getContent());
        verify(internalCommentRepository).save(any(com.example.demo.model.InternalComment.class));
    }

    @Test
    void testGetTeamMembersWithDetails() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        List<User> result = dashboardService.getTeamMembersWithDetails();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testGetTeamMembersStats() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));

        List<TeamMemberStatsDTO> result = dashboardService.getTeamMembersStats();

        assertNotNull(result);
        verify(userRepository).findAll();
    }

    @Test
    void testCalculateComplianceRate() {
        testInvoice.setStatus("PAID");
        testInvoice.setPaymentDate(LocalDate.now());
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice));

        ComplianceRateDTO result = dashboardService.calculateComplianceRate();

        assertNotNull(result);
        assertEquals(1, result.getTotalInvoices());
        verify(invoiceRepository).findAll();
    }
}
