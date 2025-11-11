package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.service.KpiCalculatorService.KpiResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

/**
 * Tests unitaires complets pour KpiCalculatorService
 * Teste tous les calculs de KPI selon les bonnes pratiques
 */
@ExtendWith(MockitoExtension.class)
class KpiCalculatorServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private KpiCalculatorService kpiCalculatorService;

    private List<Invoice> mockInvoices;
    private List<Convention> mockConventions;

    @BeforeEach
    void setUp() {
        // Initialisation des factures mock
        mockInvoices = new ArrayList<>();
        
        // Facture payée
        Invoice paidInvoice = new Invoice();
        paidInvoice.setId("inv-1");
        paidInvoice.setStatus("PAID");
        paidInvoice.setAmount(BigDecimal.valueOf(1000));
        paidInvoice.setDueDate(LocalDate.now().minusDays(10));
        paidInvoice.setIssueDate(LocalDate.now().minusDays(40));
        paidInvoice.setPaymentDate(LocalDate.now().minusDays(5));
        mockInvoices.add(paidInvoice);

        // Facture en retard
        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setId("inv-2");
        overdueInvoice.setStatus("PENDING");
        overdueInvoice.setAmount(BigDecimal.valueOf(2000));
        overdueInvoice.setDueDate(LocalDate.now().minusDays(5));
        mockInvoices.add(overdueInvoice);

        // Facture en attente (non en retard)
        Invoice pendingInvoice = new Invoice();
        pendingInvoice.setId("inv-3");
        pendingInvoice.setStatus("PENDING");
        pendingInvoice.setAmount(BigDecimal.valueOf(1500));
        pendingInvoice.setDueDate(LocalDate.now().plusDays(10));
        mockInvoices.add(pendingInvoice);

        // Initialisation des conventions mock
        mockConventions = new ArrayList<>();
        
        Convention activeConv = new Convention();
        activeConv.setId("conv-1");
        activeConv.setStatus("ACTIVE");
        activeConv.setGovernorate("Tunis");
        activeConv.setStructureId("struct-1");
        mockConventions.add(activeConv);

        Convention inactiveConv = new Convention();
        inactiveConv.setId("conv-2");
        inactiveConv.setStatus("INACTIVE");
        inactiveConv.setGovernorate("Sfax");
        inactiveConv.setStructureId("struct-2");
        mockConventions.add(inactiveConv);
    }

    // ==================== Tests calculateGlobalKpis ====================

    @Test
    @DisplayName("Should calculate all global KPIs successfully")
    void testCalculateGlobalKpis_Success() {
        // Given
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue(result.containsKey("TAUX_RETARD"));
        assertTrue(result.containsKey("TAUX_PAIEMENT"));
        assertTrue(result.containsKey("MONTANT_IMPAYE_PERCENT"));
        assertTrue(result.containsKey("DUREE_MOYENNE_PAIEMENT"));
        assertTrue(result.containsKey("TAUX_CONVERSION"));
        
        verify(invoiceRepository, times(1)).findAll();
        verify(conventionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should calculate correct retard rate")
    void testCalculateGlobalKpis_RetardRate() {
        // Given
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        KpiResult retardRate = result.get("TAUX_RETARD");
        assertNotNull(retardRate);
        assertEquals("%", retardRate.getUnit());
        // 1 facture en retard sur 3 = 33.3%
        assertTrue(retardRate.getValue() >= 33.0 && retardRate.getValue() <= 34.0);
    }

    @Test
    @DisplayName("Should calculate correct payment rate")
    void testCalculateGlobalKpis_PaymentRate() {
        // Given
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        KpiResult paymentRate = result.get("TAUX_PAIEMENT");
        assertNotNull(paymentRate);
        assertEquals("%", paymentRate.getUnit());
        // 1 facture payée sur 3 = 33.3%
        assertTrue(paymentRate.getValue() >= 33.0 && paymentRate.getValue() <= 34.0);
    }

    @Test
    @DisplayName("Should calculate correct unpaid amount percentage")
    void testCalculateGlobalKpis_UnpaidAmount() {
        // Given
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        KpiResult unpaidAmount = result.get("MONTANT_IMPAYE_PERCENT");
        assertNotNull(unpaidAmount);
        assertEquals("%", unpaidAmount.getUnit());
        // 3500 impayé sur 4500 total = 77.8%
        assertTrue(unpaidAmount.getValue() >= 77.0 && unpaidAmount.getValue() <= 78.0);
    }

    @Test
    @DisplayName("Should calculate correct average payment time")
    void testCalculateGlobalKpis_AveragePaymentTime() {
        // Given
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        KpiResult avgPaymentTime = result.get("DUREE_MOYENNE_PAIEMENT");
        assertNotNull(avgPaymentTime);
        assertEquals("jours", avgPaymentTime.getUnit());
        // 35 jours entre émission et paiement
        assertTrue(avgPaymentTime.getValue() >= 34.0 && avgPaymentTime.getValue() <= 36.0);
    }

    @Test
    @DisplayName("Should calculate correct conversion rate")
    void testCalculateGlobalKpis_ConversionRate() {
        // Given
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        KpiResult conversionRate = result.get("TAUX_CONVERSION");
        assertNotNull(conversionRate);
        assertEquals("%", conversionRate.getUnit());
        // 1 convention active sur 2 = 50%
        assertEquals(50.0, conversionRate.getValue());
    }

    // ==================== Tests calculateKpisByGouvernorat ====================

    @Test
    @DisplayName("Should calculate KPIs by governorate")
    void testCalculateKpisByGouvernorat_Success() {
        // Given
        Invoice tunisInvoice = new Invoice();
        tunisInvoice.setConventionId("conv-1");
        tunisInvoice.setStatus("PAID");
        tunisInvoice.setAmount(BigDecimal.valueOf(1000));
        
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(Collections.singletonList(tunisInvoice));

        // When
        Map<String, Map<String, KpiResult>> result = kpiCalculatorService.calculateKpisByGouvernorat();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("Tunis"));
        assertTrue(result.containsKey("Sfax"));
        
        Map<String, KpiResult> tunisKpis = result.get("Tunis");
        assertNotNull(tunisKpis);
        assertTrue(tunisKpis.containsKey("TAUX_RETARD"));
        assertTrue(tunisKpis.containsKey("TAUX_PAIEMENT"));
    }

    @Test
    @DisplayName("Should handle empty governorate list")
    void testCalculateKpisByGouvernorat_EmptyList() {
        // Given
        when(conventionRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Map<String, Map<String, KpiResult>> result = kpiCalculatorService.calculateKpisByGouvernorat();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter out conventions without governorate")
    void testCalculateKpisByGouvernorat_NullGovernorate() {
        // Given
        Convention convWithoutGov = new Convention();
        convWithoutGov.setId("conv-3");
        convWithoutGov.setGovernorate(null);
        
        List<Convention> conventions = new ArrayList<>(mockConventions);
        conventions.add(convWithoutGov);
        
        when(conventionRepository.findAll()).thenReturn(conventions);
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Map<String, Map<String, KpiResult>> result = kpiCalculatorService.calculateKpisByGouvernorat();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Only Tunis and Sfax
        assertFalse(result.containsKey(null));
    }

    // ==================== Tests calculateKpisByStructure ====================

    @Test
    @DisplayName("Should calculate KPIs by structure")
    void testCalculateKpisByStructure_Success() {
        // Given
        Invoice structInvoice = new Invoice();
        structInvoice.setConventionId("conv-1");
        structInvoice.setStatus("PAID");
        structInvoice.setAmount(BigDecimal.valueOf(1000));
        
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(Collections.singletonList(structInvoice));

        // When
        Map<String, Map<String, KpiResult>> result = kpiCalculatorService.calculateKpisByStructure();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("struct-1"));
        assertTrue(result.containsKey("struct-2"));
        
        Map<String, KpiResult> struct1Kpis = result.get("struct-1");
        assertNotNull(struct1Kpis);
        assertTrue(struct1Kpis.containsKey("TAUX_RETARD"));
        assertTrue(struct1Kpis.containsKey("TAUX_PAIEMENT"));
    }

    @Test
    @DisplayName("Should handle empty structure list")
    void testCalculateKpisByStructure_EmptyList() {
        // Given
        when(conventionRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Map<String, Map<String, KpiResult>> result = kpiCalculatorService.calculateKpisByStructure();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests Edge Cases ====================

    @Test
    @DisplayName("Should handle empty invoice list")
    void testCalculateGlobalKpis_EmptyInvoices() {
        // Given
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.get("TAUX_RETARD").getValue());
        assertEquals(0.0, result.get("TAUX_PAIEMENT").getValue());
        assertEquals(0.0, result.get("MONTANT_IMPAYE_PERCENT").getValue());
        assertEquals(0.0, result.get("DUREE_MOYENNE_PAIEMENT").getValue());
    }

    @Test
    @DisplayName("Should handle invoices with null amounts")
    void testCalculateGlobalKpis_NullAmounts() {
        // Given
        Invoice nullAmountInvoice = new Invoice();
        nullAmountInvoice.setId("inv-null");
        nullAmountInvoice.setStatus("PENDING");
        nullAmountInvoice.setAmount(null);
        
        List<Invoice> invoices = Collections.singletonList(nullAmountInvoice);
        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        assertNotNull(result);
        KpiResult unpaidAmount = result.get("MONTANT_IMPAYE_PERCENT");
        assertNotNull(unpaidAmount);
        assertEquals(0.0, unpaidAmount.getValue());
    }

    @Test
    @DisplayName("Should handle invoices with null due dates")
    void testCalculateGlobalKpis_NullDueDates() {
        // Given
        Invoice nullDueDateInvoice = new Invoice();
        nullDueDateInvoice.setId("inv-null-date");
        nullDueDateInvoice.setStatus("PENDING");
        nullDueDateInvoice.setAmount(BigDecimal.valueOf(1000));
        nullDueDateInvoice.setDueDate(null);
        
        List<Invoice> invoices = Collections.singletonList(nullDueDateInvoice);
        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        assertNotNull(result);
        KpiResult retardRate = result.get("TAUX_RETARD");
        assertNotNull(retardRate);
        assertEquals(0.0, retardRate.getValue()); // No overdue if no due date
    }

    @Test
    @DisplayName("Should handle paid invoices without payment dates")
    void testCalculateGlobalKpis_PaidWithoutPaymentDate() {
        // Given
        Invoice paidNoDateInvoice = new Invoice();
        paidNoDateInvoice.setId("inv-paid-no-date");
        paidNoDateInvoice.setStatus("PAID");
        paidNoDateInvoice.setAmount(BigDecimal.valueOf(1000));
        paidNoDateInvoice.setIssueDate(LocalDate.now().minusDays(30));
        paidNoDateInvoice.setPaymentDate(null);
        
        List<Invoice> invoices = Collections.singletonList(paidNoDateInvoice);
        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        assertNotNull(result);
        KpiResult avgPaymentTime = result.get("DUREE_MOYENNE_PAIEMENT");
        assertNotNull(avgPaymentTime);
        assertEquals(0.0, avgPaymentTime.getValue()); // No average if no payment date
    }

    @Test
    @DisplayName("Should handle all paid invoices")
    void testCalculateGlobalKpis_AllPaid() {
        // Given
        List<Invoice> allPaidInvoices = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Invoice invoice = new Invoice();
            invoice.setId("inv-" + i);
            invoice.setStatus("PAID");
            invoice.setAmount(BigDecimal.valueOf(1000));
            invoice.setDueDate(LocalDate.now().minusDays(10));
            invoice.setIssueDate(LocalDate.now().minusDays(40));
            invoice.setPaymentDate(LocalDate.now().minusDays(5));
            allPaidInvoices.add(invoice);
        }
        
        when(invoiceRepository.findAll()).thenReturn(allPaidInvoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.get("TAUX_RETARD").getValue());
        assertEquals(100.0, result.get("TAUX_PAIEMENT").getValue());
        assertEquals(0.0, result.get("MONTANT_IMPAYE_PERCENT").getValue());
    }

    @Test
    @DisplayName("Should handle all overdue invoices")
    void testCalculateGlobalKpis_AllOverdue() {
        // Given
        List<Invoice> allOverdueInvoices = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Invoice invoice = new Invoice();
            invoice.setId("inv-" + i);
            invoice.setStatus("PENDING");
            invoice.setAmount(BigDecimal.valueOf(1000));
            invoice.setDueDate(LocalDate.now().minusDays(10));
            allOverdueInvoices.add(invoice);
        }
        
        when(invoiceRepository.findAll()).thenReturn(allOverdueInvoices);
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        // When
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();

        // Then
        assertNotNull(result);
        assertEquals(100.0, result.get("TAUX_RETARD").getValue());
        assertEquals(0.0, result.get("TAUX_PAIEMENT").getValue());
        assertEquals(100.0, result.get("MONTANT_IMPAYE_PERCENT").getValue());
    }

    // ==================== Tests KpiResult Class ====================

    @Test
    @DisplayName("Should create KpiResult with correct values")
    void testKpiResult_Creation() {
        // When
        KpiResult result = new KpiResult(75.5, "%", "Test description");

        // Then
        assertNotNull(result);
        assertEquals(75.5, result.getValue());
        assertEquals("%", result.getUnit());
        assertEquals("Test description", result.getDescription());
    }

    @Test
    @DisplayName("Should handle KpiResult with null values")
    void testKpiResult_NullValues() {
        // When
        KpiResult result = new KpiResult(null, null, null);

        // Then
        assertNotNull(result);
        assertNull(result.getValue());
        assertNull(result.getUnit());
        assertNull(result.getDescription());
    }

    // ==================== Tests Performance ====================

    @Test
    @DisplayName("Should handle large dataset efficiently")
    void testCalculateGlobalKpis_LargeDataset() {
        // Given
        List<Invoice> largeInvoiceList = new ArrayList<>();
        List<Convention> largeConventionList = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            Invoice invoice = new Invoice();
            invoice.setId("inv-" + i);
            invoice.setStatus(i % 2 == 0 ? "PAID" : "PENDING");
            invoice.setAmount(BigDecimal.valueOf(1000 + i));
            invoice.setDueDate(LocalDate.now().minusDays(i % 30));
            largeInvoiceList.add(invoice);
            
            Convention convention = new Convention();
            convention.setId("conv-" + i);
            convention.setStatus(i % 3 == 0 ? "ACTIVE" : "INACTIVE");
            convention.setGovernorate("Gov-" + (i % 10));
            convention.setStructureId("Struct-" + (i % 5));
            largeConventionList.add(convention);
        }
        
        when(invoiceRepository.findAll()).thenReturn(largeInvoiceList);
        when(conventionRepository.findAll()).thenReturn(largeConventionList);

        // When
        long startTime = System.currentTimeMillis();
        Map<String, KpiResult> result = kpiCalculatorService.calculateGlobalKpis();
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue((endTime - startTime) < 5000, "Should complete in less than 5 seconds");
    }
}
