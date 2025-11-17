package com.example.demo.service;

import com.example.demo.dto.KPIMetricsDTO;
import com.example.demo.dto.report.FinancialReportDTO;
import com.example.demo.dto.report.PerformanceReportDTO;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Tests")
class ReportServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ReportService reportService;

    private Convention testConvention;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        testConvention = new Convention();
        testConvention.setId("1");
        testConvention.setAmount(BigDecimal.valueOf(10000));
        testConvention.setStatus("ACTIVE");

        testInvoice = new Invoice();
        testInvoice.setId("1");
        testInvoice.setAmount(BigDecimal.valueOf(5000));
        testInvoice.setStatus("PAID");
    }

    @Test
    @DisplayName("Should get KPI metrics successfully")
    void testGetKPIMetrics() {
        KPIMetricsDTO result = reportService.getKPIMetrics();

        assertNotNull(result);
        assertEquals(100, result.getTotalConventions());
        assertEquals(80, result.getActiveConventions());
        assertEquals(20, result.getExpiredConventions());
        assertEquals(200, result.getTotalInvoices());
        assertEquals(150, result.getPaidInvoices());
        assertEquals(10, result.getOverdueInvoices());
        assertEquals(50000.0, result.getMonthlyRevenue());
    }

    @Test
    @DisplayName("Should get financial report successfully")
    void testGetFinancialReport() {
        Convention conv2 = new Convention();
        conv2.setAmount(BigDecimal.valueOf(15000));
        
        Invoice inv2 = new Invoice();
        inv2.setAmount(BigDecimal.valueOf(3000));
        inv2.setStatus("PENDING");
        
        Invoice inv3 = new Invoice();
        inv3.setAmount(BigDecimal.valueOf(2000));
        inv3.setStatus("OVERDUE");

        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention, conv2));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice, inv2, inv3));

        FinancialReportDTO result = reportService.getFinancialReport();

        assertNotNull(result);
        assertEquals(25000.0, result.getTotalRevenue());
        assertEquals(5000.0, result.getPaidAmount());
        assertEquals(3000.0, result.getPendingAmount());
        assertEquals(2000.0, result.getOverdueAmount());
        assertTrue(result.getCollectionRate() > 0);
        
        verify(conventionRepository, times(1)).findAll();
        verify(invoiceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle empty data in financial report")
    void testGetFinancialReportWithEmptyData() {
        when(conventionRepository.findAll()).thenReturn(Collections.emptyList());
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());

        FinancialReportDTO result = reportService.getFinancialReport();

        assertNotNull(result);
        assertEquals(0.0, result.getTotalRevenue());
        assertEquals(0.0, result.getPaidAmount());
        assertEquals(0.0, result.getPendingAmount());
        assertEquals(0.0, result.getOverdueAmount());
        assertEquals(0.0, result.getCollectionRate());
    }

    @Test
    @DisplayName("Should get performance report successfully")
    void testGetPerformanceReport() {
        Invoice inv2 = new Invoice();
        inv2.setAmount(BigDecimal.valueOf(3000));
        inv2.setStatus("PENDING");
        
        Invoice inv3 = new Invoice();
        inv3.setAmount(BigDecimal.valueOf(2000));
        inv3.setStatus("OVERDUE");

        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice, inv2, inv3));

        PerformanceReportDTO result = reportService.getPerformanceReport();

        assertNotNull(result);
        assertEquals(1, result.getTotalConventions());
        assertEquals(3, result.getTotalInvoices());
        assertEquals(1, result.getPaidInvoices());
        assertEquals(1, result.getOverdueInvoices());
        assertTrue(result.getPaymentRate() > 0);
        assertTrue(result.getOverdueRate() > 0);
        
        verify(conventionRepository, times(1)).findAll();
        verify(invoiceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle empty data in performance report")
    void testGetPerformanceReportWithEmptyData() {
        when(conventionRepository.findAll()).thenReturn(Collections.emptyList());
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());

        PerformanceReportDTO result = reportService.getPerformanceReport();

        assertNotNull(result);
        assertEquals(0, result.getTotalConventions());
        assertEquals(0, result.getTotalInvoices());
        assertEquals(0, result.getPaidInvoices());
        assertEquals(0, result.getOverdueInvoices());
        assertEquals(0.0, result.getPaymentRate());
        assertEquals(0.0, result.getOverdueRate());
    }

    @Test
    @DisplayName("Should calculate collection rate correctly")
    void testCollectionRateCalculation() {
        Convention conv2 = new Convention();
        conv2.setAmount(BigDecimal.valueOf(20000));
        
        Invoice inv2 = new Invoice();
        inv2.setAmount(BigDecimal.valueOf(15000));
        inv2.setStatus("PAID");

        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention, conv2));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(testInvoice, inv2));

        FinancialReportDTO result = reportService.getFinancialReport();

        assertNotNull(result);
        // Total revenue: 30000, Paid: 20000, Collection rate: 66.67%
        assertTrue(result.getCollectionRate() > 60 && result.getCollectionRate() < 70);
    }

    @Test
    @DisplayName("Should handle different invoice statuses")
    void testDifferentInvoiceStatuses() {
        Invoice paidInvoice = new Invoice();
        paidInvoice.setAmount(BigDecimal.valueOf(1000));
        paidInvoice.setStatus("PAYÉE"); // French status
        
        Invoice pendingInvoice = new Invoice();
        pendingInvoice.setAmount(BigDecimal.valueOf(2000));
        pendingInvoice.setStatus("EN_ATTENTE"); // French status
        
        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setAmount(BigDecimal.valueOf(3000));
        overdueInvoice.setStatus("EN_RETARD"); // French status

        when(conventionRepository.findAll()).thenReturn(Arrays.asList(testConvention));
        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(paidInvoice, pendingInvoice, overdueInvoice));

        FinancialReportDTO result = reportService.getFinancialReport();

        assertNotNull(result);
        assertEquals(1000.0, result.getPaidAmount());
        assertEquals(2000.0, result.getPendingAmount());
        assertEquals(3000.0, result.getOverdueAmount());
    }
}
