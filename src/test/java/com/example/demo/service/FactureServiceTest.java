package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentProofRepository;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactureServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PDFGenerationService pdfGenerationService;

    @Mock
    private PaymentProofRepository paymentProofRepository;

    @InjectMocks
    private FactureService factureService;

    private Invoice mockInvoice;

    @BeforeEach
    void setUp() {
        mockInvoice = new Invoice();
        mockInvoice.setId("inv1");
        mockInvoice.setReference("REF001");
        mockInvoice.setAmount(BigDecimal.valueOf(1000));
        mockInvoice.setStatus("PAID");
        mockInvoice.setDueDate(LocalDate.now().plusDays(30));
        mockInvoice.setConventionId("conv1");
    }

    @Test
    void testGetFactures() {
        List<Invoice> invoices = Arrays.asList(mockInvoice);
        when(invoiceRepository.findAll()).thenReturn(invoices);
        when(paymentProofRepository.findByInvoiceId(anyString())).thenReturn(new ArrayList<>());

        List<Map<String, Object>> result = factureService.getFactures();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceRepository).findAll();
    }

    @Test
    void testGetPreuve() {
        Map<String, Object> result = factureService.getPreuve("inv1");

        assertNotNull(result);
        assertTrue(result.containsKey("url"));
    }

    @Test
    void testGetRecu() {
        Map<String, Object> result = factureService.getRecu("inv1");

        assertNotNull(result);
        assertTrue(result.containsKey("url"));
    }

    @Test
    void testGeneratePreuvePdf() throws Exception {
        byte[] mockPdf = "PDF content".getBytes();
        when(pdfGenerationService.generateInvoicePDF("inv1")).thenReturn(mockPdf);

        byte[] result = factureService.generatePreuvePdf("inv1");

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(pdfGenerationService).generateInvoicePDF("inv1");
    }

    @Test
    void testGenerateRecuPdf() throws Exception {
        byte[] mockPdf = "PDF content".getBytes();
        when(pdfGenerationService.generateInvoicePDF("inv1")).thenReturn(mockPdf);

        byte[] result = factureService.generateRecuPdf("inv1");

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(pdfGenerationService).generateInvoicePDF("inv1");
    }

    @Test
    void testGetStats() {
        List<Invoice> invoices = new ArrayList<>();
        Invoice inv1 = new Invoice();
        inv1.setStatus("PAID");
        inv1.setAmount(BigDecimal.valueOf(1000));
        invoices.add(inv1);

        Invoice inv2 = new Invoice();
        inv2.setStatus("NON PAYÉE");
        inv2.setAmount(BigDecimal.valueOf(500));
        invoices.add(inv2);

        when(invoiceRepository.findAll()).thenReturn(invoices);

        Map<String, Object> stats = factureService.getStats();

        assertNotNull(stats);
        assertTrue(stats.containsKey("tauxPaiement"));
        assertTrue(stats.containsKey("montantTotal"));
        assertTrue(stats.containsKey("montantEncaisse"));
        assertTrue(stats.containsKey("retards"));
    }
}
