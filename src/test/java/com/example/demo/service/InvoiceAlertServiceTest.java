package com.example.demo.service;

import com.example.demo.enums.ERole;
import com.example.demo.model.Invoice;
import com.example.demo.model.KpiAlert;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceAlertServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KpiAlertManagementService alertManagementService;

    @Mock
    private KpiAlertRepository kpiAlertRepository;

    @InjectMocks
    private InvoiceAlertService invoiceAlertService;

    private Invoice testInvoice;
    private User testDecisionMaker;
    private KpiAlert testAlert;

    @BeforeEach
    void setUp() {
        testInvoice = new Invoice();
        testInvoice.setId("INV-001");
        testInvoice.setReference("REF-2024-001");
        testInvoice.setInvoiceNumber("2024-001");
        testInvoice.setAmount(BigDecimal.valueOf(25000));
        testInvoice.setStatus("PENDING");
        testInvoice.setDueDate(LocalDate.now().plusDays(30));
        testInvoice.setIssueDate(LocalDate.now());
        testInvoice.setCreatedAt(LocalDate.now().minusDays(10));
        testInvoice.setClientId("CLIENT-001");
        testInvoice.setClientEmail("client@test.com");

        Role role = new Role();
        role.setName(ERole.ROLE_DECISION_MAKER);

        testDecisionMaker = new User();
        testDecisionMaker.setId("DM-001");
        testDecisionMaker.setUsername("decisionmaker");
        testDecisionMaker.setRoles(new HashSet<>(Arrays.asList(role)));

        testAlert = new KpiAlert();
        testAlert.setId("ALERT-001");
        testAlert.setKpiName("FACTURE_PENDING");
        testAlert.setRelatedInvoiceId("INV-001");
    }

    @Test
    void testCheckPendingInvoices_Success() {
        when(invoiceRepository.findByStatus("PENDING")).thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER)).thenReturn(Arrays.asList(testDecisionMaker));
        when(kpiAlertRepository.findByRelatedInvoiceId("INV-001")).thenReturn(new ArrayList<>());
        when(alertManagementService.createAlert(any(KpiAlert.class), eq("system"))).thenReturn(testAlert);

        List<KpiAlert> result = invoiceAlertService.checkPendingInvoices();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceRepository).findByStatus("PENDING");
        verify(alertManagementService).createAlert(any(KpiAlert.class), eq("system"));
    }

    @Test
    void testCheckPendingInvoices_NoInvoices() {
        when(invoiceRepository.findByStatus("PENDING")).thenReturn(new ArrayList<>());

        List<KpiAlert> result = invoiceAlertService.checkPendingInvoices();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(invoiceRepository).findByStatus("PENDING");
        verify(alertManagementService, never()).createAlert(any(), any());
    }

    @Test
    void testCheckOverdueInvoices_Success() {
        testInvoice.setStatus("OVERDUE");
        testInvoice.setDueDate(LocalDate.now().minusDays(15));

        when(invoiceRepository.findByStatus("OVERDUE")).thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER)).thenReturn(Arrays.asList(testDecisionMaker));
        when(kpiAlertRepository.findByRelatedInvoiceId("INV-001")).thenReturn(new ArrayList<>());
        when(alertManagementService.createAlert(any(KpiAlert.class), eq("system"))).thenReturn(testAlert);

        List<KpiAlert> result = invoiceAlertService.checkOverdueInvoices();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceRepository).findByStatus("OVERDUE");
    }

    @Test
    void testCheckPendingInvoices_UpdateExistingAlert() {
        when(invoiceRepository.findByStatus("PENDING")).thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER)).thenReturn(Arrays.asList(testDecisionMaker));
        
        testAlert.setAlertStatus("PENDING_DECISION");
        when(kpiAlertRepository.findByRelatedInvoiceId("INV-001")).thenReturn(Arrays.asList(testAlert));
        when(kpiAlertRepository.save(any(KpiAlert.class))).thenReturn(testAlert);

        List<KpiAlert> result = invoiceAlertService.checkPendingInvoices();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(kpiAlertRepository).save(any(KpiAlert.class));
    }

    @Test
    void testCheckPendingInvoices_HighPriorityInvoice() {
        testInvoice.setAmount(BigDecimal.valueOf(60000));
        testInvoice.setCreatedAt(LocalDate.now().minusDays(35));

        when(invoiceRepository.findByStatus("PENDING")).thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER)).thenReturn(Arrays.asList(testDecisionMaker));
        when(kpiAlertRepository.findByRelatedInvoiceId("INV-001")).thenReturn(new ArrayList<>());
        when(alertManagementService.createAlert(any(KpiAlert.class), eq("system"))).thenReturn(testAlert);

        List<KpiAlert> result = invoiceAlertService.checkPendingInvoices();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(alertManagementService).createAlert(any(KpiAlert.class), eq("system"));
    }

    @Test
    void testCountOverdueInvoices() {
        when(invoiceRepository.findByStatus("OVERDUE")).thenReturn(Arrays.asList(testInvoice, testInvoice));

        long count = invoiceAlertService.countOverdueInvoices();

        assertEquals(2, count);
        verify(invoiceRepository).findByStatus("OVERDUE");
    }

    @Test
    void testCheckPendingInvoices_RemoveDuplicates() {
        KpiAlert duplicate1 = new KpiAlert();
        duplicate1.setId("ALERT-001");
        duplicate1.setAlertStatus("PENDING_DECISION");
        
        KpiAlert duplicate2 = new KpiAlert();
        duplicate2.setId("ALERT-002");
        duplicate2.setAlertStatus("PENDING_DECISION");

        when(invoiceRepository.findByStatus("PENDING")).thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER)).thenReturn(Arrays.asList(testDecisionMaker));
        when(kpiAlertRepository.findByRelatedInvoiceId("INV-001")).thenReturn(Arrays.asList(duplicate1, duplicate2));
        when(kpiAlertRepository.save(any(KpiAlert.class))).thenReturn(duplicate1);

        List<KpiAlert> result = invoiceAlertService.checkPendingInvoices();

        assertNotNull(result);
        verify(kpiAlertRepository).delete(duplicate2);
    }

    @Test
    void testCheckOverdueInvoices_CriticalSeverity() {
        testInvoice.setStatus("OVERDUE");
        testInvoice.setDueDate(LocalDate.now().minusDays(65));

        when(invoiceRepository.findByStatus("OVERDUE")).thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER)).thenReturn(Arrays.asList(testDecisionMaker));
        when(kpiAlertRepository.findByRelatedInvoiceId("INV-001")).thenReturn(new ArrayList<>());
        when(alertManagementService.createAlert(any(KpiAlert.class), eq("system"))).thenReturn(testAlert);

        List<KpiAlert> result = invoiceAlertService.checkOverdueInvoices();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testCheckPendingInvoices_ExceptionHandling() {
        when(invoiceRepository.findByStatus("PENDING")).thenThrow(new RuntimeException("Database error"));

        List<KpiAlert> result = invoiceAlertService.checkPendingInvoices();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCheckOverdueInvoices_NoDecisionMakers() {
        when(invoiceRepository.findByStatus("OVERDUE")).thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER)).thenReturn(new ArrayList<>());

        List<KpiAlert> result = invoiceAlertService.checkOverdueInvoices();

        assertNotNull(result);
        verify(invoiceRepository).findByStatus("OVERDUE");
    }
}
