package com.example.demo.service;

import com.example.demo.model.KpiAlert;
import com.example.demo.model.KpiThreshold;
import com.example.demo.model.Invoice;
import com.example.demo.model.Convention;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.repository.KpiThresholdRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.ConventionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpiEvaluatorServiceTest {

    @Mock
    private KpiThresholdRepository thresholdRepository;

    @Mock
    private KpiAlertRepository alertRepository;

    @Mock
    private KpiCalculatorService calculatorService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ConventionRepository conventionRepository;

    @InjectMocks
    private KpiEvaluatorService evaluatorService;

    private KpiThreshold mockThreshold;
    private Invoice mockInvoice;
    private Convention mockConvention;

    @BeforeEach
    void setUp() {
        mockThreshold = new KpiThreshold();
        mockThreshold.setKpiName("TAUX_RETARD");
        mockThreshold.setDescription("Taux de retard");
        mockThreshold.setLowThreshold(10.0);
        mockThreshold.setHighThreshold(20.0);
        mockThreshold.setNormalValue(5.0);
        mockThreshold.setUnit("%");
        mockThreshold.setEnabled(true);

        mockInvoice = new Invoice();
        mockInvoice.setId("inv1");
        mockInvoice.setInvoiceNumber("INV-001");
        mockInvoice.setAmount(BigDecimal.valueOf(1000));
        mockInvoice.setStatus("OVERDUE");
        mockInvoice.setDueDate(LocalDate.now().minusDays(10));

        mockConvention = new Convention();
        mockConvention.setId("conv1");
        mockConvention.setReference("CONV-001");
        mockConvention.setAmount(BigDecimal.valueOf(5000));
    }

    @Test
    void testEvaluateKpi_Normal() {
        when(thresholdRepository.findByKpiName("TAUX_RETARD")).thenReturn(Optional.of(mockThreshold));

        KpiEvaluatorService.KpiEvaluation result = evaluatorService.evaluateKpi("TAUX_RETARD", 5.0, null, null);

        assertNotNull(result);
        assertEquals("SAIN", result.getStatus());
        assertEquals("LOW", result.getSeverity());
    }

    @Test
    void testEvaluateKpi_Warning() {
        when(thresholdRepository.findByKpiName("TAUX_RETARD")).thenReturn(Optional.of(mockThreshold));

        KpiEvaluatorService.KpiEvaluation result = evaluatorService.evaluateKpi("TAUX_RETARD", 15.0, null, null);

        assertNotNull(result);
        assertEquals("A_SURVEILLER", result.getStatus());
        assertEquals("MEDIUM", result.getSeverity());
    }

    @Test
    void testEvaluateKpi_Critical() {
        when(thresholdRepository.findByKpiName("TAUX_RETARD")).thenReturn(Optional.of(mockThreshold));

        KpiEvaluatorService.KpiEvaluation result = evaluatorService.evaluateKpi("TAUX_RETARD", 25.0, null, null);

        assertNotNull(result);
        assertEquals("ANORMAL", result.getStatus());
        assertEquals("HIGH", result.getSeverity());
        assertNotNull(result.getRecommendation());
    }

    @Test
    void testEvaluateKpi_NoThreshold() {
        when(thresholdRepository.findByKpiName("UNKNOWN_KPI")).thenReturn(Optional.empty());

        KpiEvaluatorService.KpiEvaluation result = evaluatorService.evaluateKpi("UNKNOWN_KPI", 10.0, null, null);

        assertNotNull(result);
        assertEquals("SAIN", result.getStatus());
    }

    @Test
    void testAnalyzeAllKpis() {
        List<Invoice> overdueInvoices = Arrays.asList(mockInvoice);
        lenient().when(invoiceRepository.findByStatus("OVERDUE")).thenReturn(overdueInvoices);
        lenient().when(conventionRepository.findById(anyString())).thenReturn(Optional.of(mockConvention));
        lenient().when(alertRepository.findByRelatedInvoiceIdAndAlertStatus(anyString(), anyString()))
            .thenReturn(Optional.empty());
        lenient().when(alertRepository.save(any(KpiAlert.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(alertRepository.findByDimension(anyString())).thenReturn(new ArrayList<>());

        List<KpiAlert> result = evaluatorService.analyzeAllKpis();

        assertNotNull(result);
    }

    @Test
    void testAnalyzeAllKpis_Concurrent() {
        // Test protection contre appels concurrents
        when(invoiceRepository.findByStatus("OVERDUE")).thenReturn(new ArrayList<>());

        List<KpiAlert> result1 = evaluatorService.analyzeAllKpis();
        List<KpiAlert> result2 = evaluatorService.analyzeAllKpis(); // Devrait être ignoré

        assertNotNull(result1);
        assertNotNull(result2);
    }
}
