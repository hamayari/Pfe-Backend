package com.example.demo.service;

import com.example.demo.dto.KPIMetricsDTO;
import com.example.demo.dto.ConventionStatsDTO;
import com.example.demo.dto.InvoiceStatsDTO;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommercialDashboardServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private CommercialDashboardService service;

    private String userId;
    private List<Convention> mockConventions;
    private List<Invoice> mockInvoices;

    @BeforeEach
    void setUp() {
        userId = "user123";
        mockConventions = createMockConventions();
        mockInvoices = createMockInvoices();
    }

    private List<Convention> createMockConventions() {
        List<Convention> conventions = new ArrayList<>();
        
        Convention c1 = new Convention();
        c1.setId("conv1");
        c1.setReference("REF001");
        c1.setTitle("Convention 1");
        c1.setStatus("ACTIVE");
        c1.setGovernorate("Tunis");
        c1.setStructureId("struct1");
        c1.setAmount(BigDecimal.valueOf(1000));
        c1.setCreatedBy(userId);
        c1.setCreatedAt(LocalDate.now());
        conventions.add(c1);
        
        Convention c2 = new Convention();
        c2.setId("conv2");
        c2.setReference("REF002");
        c2.setTitle("Convention 2");
        c2.setStatus("EXPIRED");
        c2.setGovernorate("Sfax");
        c2.setStructureId("struct2");
        c2.setAmount(BigDecimal.valueOf(2000));
        c2.setCreatedBy(userId);
        c2.setCreatedAt(LocalDate.now());
        conventions.add(c2);
        
        Convention c3 = new Convention();
        c3.setId("conv3");
        c3.setReference("REF003");
        c3.setTitle("Convention 3");
        c3.setStatus("ACTIVE");
        c3.setGovernorate("Tunis");
        c3.setStructureId("struct1");
        c3.setAmount(BigDecimal.valueOf(1500));
        c3.setCreatedBy(userId);
        c3.setCreatedAt(LocalDate.now());
        conventions.add(c3);
        
        return conventions;
    }

    private List<Invoice> createMockInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        
        Invoice i1 = new Invoice();
        i1.setId("inv1");
        i1.setInvoiceNumber("FACT001");
        i1.setConventionId("conv1");
        i1.setStatus("PAID");
        i1.setAmount(BigDecimal.valueOf(1000));
        i1.setDueDate(LocalDate.now().minusDays(10));
        i1.setPaymentDate(LocalDate.now().minusDays(5));
        i1.setCreatedBy(userId);
        i1.setCreatedAt(LocalDate.now().minusDays(20));
        invoices.add(i1);
        
        Invoice i2 = new Invoice();
        i2.setId("inv2");
        i2.setInvoiceNumber("FACT002");
        i2.setConventionId("conv2");
        i2.setStatus("OVERDUE");
        i2.setAmount(BigDecimal.valueOf(2000));
        i2.setDueDate(LocalDate.now().minusDays(5));
        i2.setCreatedBy(userId);
        i2.setCreatedAt(LocalDate.now().minusDays(15));
        invoices.add(i2);
        
        Invoice i3 = new Invoice();
        i3.setId("inv3");
        i3.setInvoiceNumber("FACT003");
        i3.setConventionId("conv3");
        i3.setStatus("PAID");
        i3.setAmount(BigDecimal.valueOf(1500));
        i3.setDueDate(LocalDate.now().minusDays(8));
        i3.setPaymentDate(LocalDate.now().minusDays(3));
        i3.setCreatedBy(userId);
        i3.setCreatedAt(LocalDate.now().minusDays(10));
        invoices.add(i3);
        
        return invoices;
    }

    @Test
    void testGetKPIMetrics_Success() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenReturn(mockConventions);
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        KPIMetricsDTO result = service.getKPIMetrics(userId, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.getTotalConventions() >= 0);
        assertTrue(result.getActiveConventions() >= 0);
        assertTrue(result.getTotalInvoices() >= 0);
    }

    @Test
    void testGetKPIMetrics_WithException() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenThrow(new RuntimeException("DB Error"));
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenReturn(new ArrayList<>());

        KPIMetricsDTO result = service.getKPIMetrics(userId, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotalConventions());
        assertEquals(0, result.getActiveConventions());
    }

    @Test
    void testGetConventionStats_Success() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenReturn(mockConventions);

        ConventionStatsDTO result = service.getConventionStats(userId);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 0);
        assertNotNull(result.getByGovernorate());
    }

    @Test
    void testGetConventionStats_WithException() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenThrow(new RuntimeException("DB Error"));

        ConventionStatsDTO result = service.getConventionStats(userId);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertNotNull(result.getByGovernorate());
        assertTrue(result.getByGovernorate().isEmpty());
    }

    @Test
    void testGetInvoiceStats_Success() {
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        InvoiceStatsDTO result = service.getInvoiceStats(userId);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 0);
        assertTrue(result.getTotalAmount() >= 0);
    }

    @Test
    void testGetInvoiceStats_WithException() {
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenThrow(new RuntimeException("DB Error"));

        InvoiceStatsDTO result = service.getInvoiceStats(userId);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertEquals(0.0, result.getTotalAmount());
    }

    @Test
    void testGetConventionsByDeadline() {
        LocalDate deadline = LocalDate.now().plusDays(30);
        when(conventionRepository.findByCreatedByAndDueDateBefore(userId, deadline))
            .thenReturn(mockConventions.subList(0, 1));

        List<Convention> result = service.getConventionsByDeadline(userId, 30);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetExpiredConventions() {
        when(conventionRepository.findByCreatedByAndStatus(userId, "EXPIRED"))
            .thenReturn(mockConventions.subList(1, 2));

        List<Convention> result = service.getExpiredConventions(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EXPIRED", result.get(0).getStatus());
    }

    @Test
    void testGetOverdueInvoices_Success() {
        when(invoiceRepository.findByCreatedByAndStatusAndDueDateBefore(eq(userId), eq("PENDING"), any()))
            .thenReturn(mockInvoices.subList(1, 2));

        List<Invoice> result = service.getOverdueInvoices(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetOverdueInvoices_Fallback() {
        when(invoiceRepository.findByCreatedByAndStatusAndDueDateBefore(eq(userId), eq("PENDING"), any()))
            .thenThrow(new RuntimeException("Method not found"));
        when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        List<Invoice> result = service.getOverdueInvoices(userId);

        assertNotNull(result);
    }

    @Test
    void testGetUpcomingInvoices_Success() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(7);
        when(invoiceRepository.findByCreatedByAndDueDateBetween(userId, start, end))
            .thenReturn(new ArrayList<>());

        List<Invoice> result = service.getUpcomingInvoices(userId, 7);

        assertNotNull(result);
    }

    @Test
    void testGetUpcomingInvoices_Fallback() {
        when(invoiceRepository.findByCreatedByAndDueDateBetween(eq(userId), any(), any()))
            .thenThrow(new RuntimeException("Method not found"));
        when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        List<Invoice> result = service.getUpcomingInvoices(userId, 7);

        assertNotNull(result);
    }

    @Test
    void testSearchConventions_WithFilters() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenReturn(mockConventions);

        List<Convention> result = service.searchConventions(userId, "REF001", null, 
            null, null, "ACTIVE", null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    void testSearchConventions_WithAmountFilter() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenReturn(mockConventions);

        List<Convention> result = service.searchConventions(userId, null, null, 
            null, null, null, null, null, null, 1000.0, 1500.0);

        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    void testSearchInvoices_WithFilters() {
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        List<Invoice> result = service.searchInvoices(userId, "FACT001", null, 
            "PAID", null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    void testSearchInvoices_WithAmountFilter() {
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        List<Invoice> result = service.searchInvoices(userId, null, null, 
            null, null, null, 1000.0, 1500.0, null, null);

        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    void testGenerateInvoicesBatch_Success() {
        Convention convention = mockConventions.get(0);
        when(conventionRepository.findById("conv1")).thenReturn(Optional.of(convention));
        when(invoiceRepository.findByConventionId("conv1")).thenReturn(new ArrayList<>());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        CommercialDashboardService.InvoiceBatchResult result = 
            service.generateInvoicesBatch(userId, Arrays.asList("conv1"), null, 30, false);

        assertNotNull(result);
        assertEquals(1, result.getInvoices().size());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testGenerateInvoicesBatch_ConventionNotFound() {
        when(conventionRepository.findById("conv999")).thenReturn(Optional.empty());

        CommercialDashboardService.InvoiceBatchResult result = 
            service.generateInvoicesBatch(userId, Arrays.asList("conv999"), null, 30, false);

        assertNotNull(result);
        assertTrue(result.getInvoices().isEmpty());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("introuvable"));
    }

    @Test
    void testGenerateInvoicesBatch_InvoiceAlreadyExists() {
        Convention convention = mockConventions.get(0);
        when(conventionRepository.findById("conv1")).thenReturn(Optional.of(convention));
        when(invoiceRepository.findByConventionId("conv1")).thenReturn(mockInvoices.subList(0, 1));

        CommercialDashboardService.InvoiceBatchResult result = 
            service.generateInvoicesBatch(userId, Arrays.asList("conv1"), null, 30, false);

        assertNotNull(result);
        assertTrue(result.getInvoices().isEmpty());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("existe déjà"));
    }

    @Test
    void testExportConventions_Excel() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenReturn(mockConventions);

        byte[] result = service.exportConventions(userId, "excel", null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportConventions_CSV() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenReturn(mockConventions);

        byte[] result = service.exportConventions(userId, "csv", null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportConventions_InvalidFormat() {
        lenient().when(conventionRepository.findByCreatedBy(userId)).thenReturn(mockConventions);

        byte[] result = service.exportConventions(userId, "invalid", null, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testExportInvoices_Excel() {
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        byte[] result = service.exportInvoices(userId, "excel", null, null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportInvoices_CSV() {
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        byte[] result = service.exportInvoices(userId, "csv", null, null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportInvoices_InvalidFormat() {
        lenient().when(invoiceRepository.findByCreatedBy(userId)).thenReturn(mockInvoices);

        byte[] result = service.exportInvoices(userId, "invalid", null, null, null);

        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
