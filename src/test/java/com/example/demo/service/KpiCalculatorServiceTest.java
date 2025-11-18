package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.service.KpiCalculatorService.KpiResult;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpiCalculatorServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private KpiCalculatorService kpiCalculatorService;

    private List<Invoice> testInvoices;
    private List<Convention> testConventions;

    @BeforeEach
    void setUp() {
        // Setup test invoices
        testInvoices = new ArrayList<>();
        
        Invoice paidInvoice = new Invoice();
        paidInvoice.setId("INV-001");
        paidInvoice.setStatus("PAID");
        paidInvoice.setAmount(BigDecimal.valueOf(1000));
        paidInvoice.setIssueDate(LocalDate.now().minusDays(30));
        paidInvoice.setPaymentDate(LocalDate.now().minusDays(15));
        paidInvoice.setDueDate(LocalDate.now().minusDays(10));
        testInvoices.add(paidInvoice);

        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setId("INV-002");
        overdueInvoice.setStatus("PENDING");
        overdueInvoice.setAmount(BigDecimal.valueOf(2000));
        overdueInvoice.setDueDate(LocalDate.now().minusDays(5));
        testInvoices.add(overdueInvoice);

        Invoice pendingInvoice = new Invoice();
        pendingInvoice.setId("INV-003");
        pendingInvoice.setStatus("PENDING");
        pendingInvoice.setAmount(BigDecimal.valueOf(1500));
        pendingInvoice.setDueDate(LocalDate.now().plusDays(10));
        testInvoices.add(pendingInvoice);

        // Setup test conventions
        testConventions = new ArrayList<>();
        
        Convention activeConvention = new Convention();
        activeConvention.setId("CONV-001");
        activeConvention.setStatus("ACTIVE");
        activeConvention.setGovernorate("Tunis");
        activeConvention.setStructureId("STRUCT-001");
        testConventions.add(activeConvention);

        Convention inactiveConvention = new Convention();
        inactiveConvention.setId("CONV-002");
        inactiveConvention.setStatus("INACTIVE");
        inactiveConvention.setGovernorate("Sfax");
        inactiveConvention.setStructureId("STRUCT-002");
        testConventions.add(inactiveConvention);
    }

    @Test
    void testCalculateGlobalKpis() {
        when(invoiceRepository.findAll()).thenReturn(testInvoices);
        when(conventionRepository.findAll()).thenReturn(testConventions);

        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        assertNotNull(result);
        assertTrue(result.containsKey("TAUX_RETARD"));
        assertTrue(result.containsKey("TAUX_PAIEMENT"));
        assertTrue(result.containsKey("MONTANT_IMPAYE_PERCENT"));
        assertTrue(result.containsKey("DUREE_MOYENNE_PAIEMENT"));
        assertTrue(result.containsKey("TAUX_CONVERSION"));
        
        verify(invoiceRepository, atLeastOnce()).findAll();
        verify(conventionRepository, atLeastOnce()).findAll();
    }

    @Test
    void testCalculateKpisByGouvernorat() {
        when(conventionRepository.findAll()).thenReturn(testConventions);
        when(invoiceRepository.findAll()).thenReturn(testInvoices);

        Map<String, Map<String, KpiResult>> result = kpiCalculatorService.calculateKpisByGouvernorat();

        assertNotNull(result);
        assertTrue(result.containsKey("Tunis") || result.containsKey("Sfax"));
        verify(conventionRepository).findAll();
    }

    @Test
    void testCalculateKpisByStructure() {
        when(conventionRepository.findAll()).thenReturn(testConventions);
        when(invoiceRepository.findAll()).thenReturn(testInvoices);

        Map<String, Map<String, KpiResult>> result = kpiCalculatorService.calculateKpisByStructure();

        assertNotNull(result);
        verify(conventionRepository).findAll();
    }

    @Test
    void testCalculateGlobalKpis_EmptyData() {
        when(invoiceRepository.findAll()).thenReturn(new ArrayList<>());
        when(conventionRepository.findAll()).thenReturn(new ArrayList<>());

        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        assertNotNull(result);
        assertEquals(0.0, result.get("TAUX_RETARD").getValue());
        assertEquals(0.0, result.get("TAUX_PAIEMENT").getValue());
    }

    @Test
    void testCalculateGlobalKpis_AllPaidInvoices() {
        List<Invoice> allPaid = new ArrayList<>();
        Invoice paid1 = new Invoice();
        paid1.setStatus("PAID");
        paid1.setAmount(BigDecimal.valueOf(1000));
        paid1.setIssueDate(LocalDate.now().minusDays(30));
        paid1.setPaymentDate(LocalDate.now().minusDays(20));
        paid1.setDueDate(LocalDate.now().minusDays(10));
        allPaid.add(paid1);

        when(invoiceRepository.findAll()).thenReturn(allPaid);
        when(conventionRepository.findAll()).thenReturn(testConventions);

        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        assertNotNull(result);
        assertEquals(100.0, result.get("TAUX_PAIEMENT").getValue());
        assertEquals(0.0, result.get("TAUX_RETARD").getValue());
    }

    @Test
    void testCalculateGlobalKpis_AllOverdueInvoices() {
        List<Invoice> allOverdue = new ArrayList<>();
        Invoice overdue1 = new Invoice();
        overdue1.setStatus("PENDING");
        overdue1.setAmount(BigDecimal.valueOf(1000));
        overdue1.setDueDate(LocalDate.now().minusDays(10));
        allOverdue.add(overdue1);

        when(invoiceRepository.findAll()).thenReturn(allOverdue);
        when(conventionRepository.findAll()).thenReturn(testConventions);

        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        assertNotNull(result);
        assertEquals(100.0, result.get("TAUX_RETARD").getValue());
        assertEquals(0.0, result.get("TAUX_PAIEMENT").getValue());
    }

    @Test
    void testKpiResult_Creation() {
        KpiResult kpi = new KpiResult(75.5, "%", "Test description");

        assertEquals(75.5, kpi.getValue());
        assertEquals("%", kpi.getUnit());
        assertEquals("Test description", kpi.getDescription());
    }

    @Test
    void testCalculateKpisByGouvernorat_MultipleRegions() {
        Convention conv1 = new Convention();
        conv1.setId("CONV-1");
        conv1.setGovernorate("Tunis");
        conv1.setStructureId("STRUCT-1");

        Convention conv2 = new Convention();
        conv2.setId("CONV-2");
        conv2.setGovernorate("Sfax");
        conv2.setStructureId("STRUCT-2");

        Convention conv3 = new Convention();
        conv3.setId("CONV-3");
        conv3.setGovernorate("Tunis");
        conv3.setStructureId("STRUCT-3");

        when(conventionRepository.findAll()).thenReturn(Arrays.asList(conv1, conv2, conv3));
        when(invoiceRepository.findAll()).thenReturn(testInvoices);

        Map<String, Map<String, KpiResult>> result = kpiCalculatorService.calculateKpisByGouvernorat();

        assertNotNull(result);
        assertTrue(result.size() >= 2);
    }

    @Test
    void testCalculateGlobalKpis_WithNullAmounts() {
        Invoice nullAmountInvoice = new Invoice();
        nullAmountInvoice.setId("INV-NULL");
        nullAmountInvoice.setStatus("PENDING");
        nullAmountInvoice.setAmount(null);
        
        List<Invoice> invoicesWithNull = new ArrayList<>(testInvoices);
        invoicesWithNull.add(nullAmountInvoice);

        when(invoiceRepository.findAll()).thenReturn(invoicesWithNull);
        when(conventionRepository.findAll()).thenReturn(testConventions);

        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        assertNotNull(result);
        assertNotNull(result.get("MONTANT_IMPAYE_PERCENT"));
    }
}
