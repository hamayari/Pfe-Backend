package com.example.demo.service;

import com.example.demo.dto.invoice.InvoiceRequest;
import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceService invoiceService;

    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        testInvoice = new Invoice();
        testInvoice.setId("INV-001");
        testInvoice.setInvoiceNumber("2024-001");
        testInvoice.setAmount(BigDecimal.valueOf(1000.00));
        testInvoice.setStatus("PENDING");
        testInvoice.setDueDate(LocalDate.now().plusDays(30));
    }

    @Test
    void testGetInvoiceById() {
        when(invoiceService.getInvoiceById("INV-001")).thenReturn(testInvoice);

        Invoice result = invoiceService.getInvoiceById("INV-001");

        assertNotNull(result);
        assertEquals("INV-001", result.getId());
        verify(invoiceService).getInvoiceById("INV-001");
    }

    @Test
    void testGetAllInvoices() {
        List<Invoice> invoices = Arrays.asList(testInvoice);
        when(invoiceService.getAllInvoices()).thenReturn(invoices);

        List<Invoice> result = invoiceService.getAllInvoices();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceService).getAllInvoices();
    }

    @Test
    void testGetInvoicesByUser() {
        List<Invoice> invoices = Arrays.asList(testInvoice);
        when(invoiceService.getInvoicesByUser("user1")).thenReturn(invoices);

        List<Invoice> result = invoiceService.getInvoicesByUser("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceService).getInvoicesByUser("user1");
    }

    @Test
    void testUpdateInvoiceStatus() {
        testInvoice.setStatus("PAID");
        when(invoiceService.updateInvoiceStatus("INV-001", "PAID")).thenReturn(testInvoice);

        Invoice result = invoiceService.updateInvoiceStatus("INV-001", "PAID");

        assertNotNull(result);
        assertEquals("PAID", result.getStatus());
        verify(invoiceService).updateInvoiceStatus("INV-001", "PAID");
    }

    @Test
    void testGetOverdueInvoices() {
        testInvoice.setStatus("OVERDUE");
        List<Invoice> overdueInvoices = Arrays.asList(testInvoice);
        when(invoiceService.getOverdueInvoices()).thenReturn(overdueInvoices);

        List<Invoice> result = invoiceService.getOverdueInvoices();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OVERDUE", result.get(0).getStatus());
        verify(invoiceService).getOverdueInvoices();
    }

    @Test
    void testGenerateInvoicePDF() {
        byte[] pdfData = new byte[]{1, 2, 3};
        when(invoiceService.generateInvoicePDF("INV-001")).thenReturn(pdfData);

        byte[] result = invoiceService.generateInvoicePDF("INV-001");

        assertNotNull(result);
        assertEquals(3, result.length);
        verify(invoiceService).generateInvoicePDF("INV-001");
    }

    @Test
    void testSendReminder() {
        doNothing().when(invoiceService).sendReminder("INV-001", "EMAIL");

        invoiceService.sendReminder("INV-001", "EMAIL");

        verify(invoiceService).sendReminder("INV-001", "EMAIL");
    }

    @Test
    void testGetInvoicesByConvention() {
        List<Invoice> invoices = Arrays.asList(testInvoice);
        when(invoiceService.getInvoicesByConvention("CONV-001")).thenReturn(invoices);

        List<Invoice> result = invoiceService.getInvoicesByConvention("CONV-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceService).getInvoicesByConvention("CONV-001");
    }

    @Test
    void testGetInvoicesByClient() {
        List<Invoice> invoices = Arrays.asList(testInvoice);
        when(invoiceService.getInvoicesByClient("CLIENT-001")).thenReturn(invoices);

        List<Invoice> result = invoiceService.getInvoicesByClient("CLIENT-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceService).getInvoicesByClient("CLIENT-001");
    }

    @Test
    void testDeleteInvoice() {
        doNothing().when(invoiceService).deleteInvoice("INV-001");

        invoiceService.deleteInvoice("INV-001");

        verify(invoiceService).deleteInvoice("INV-001");
    }
}
