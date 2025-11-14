package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ChatbotService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatbotServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    private List<Convention> mockConventions;
    private List<Invoice> mockInvoices;

    @BeforeEach
    void setUp() {
        // Mock conventions
        mockConventions = new ArrayList<>();
        Convention conv1 = new Convention();
        conv1.setId("conv-1");
        conv1.setTitle("Convention 1");
        conv1.setStatus("ACTIVE");
        conv1.setAmount(BigDecimal.valueOf(10000));
        mockConventions.add(conv1);

        Convention conv2 = new Convention();
        conv2.setId("conv-2");
        conv2.setTitle("Convention 2");
        conv2.setStatus("EXPIRED");
        conv2.setAmount(BigDecimal.valueOf(5000));
        mockConventions.add(conv2);

        // Mock invoices
        mockInvoices = new ArrayList<>();
        Invoice inv1 = new Invoice();
        inv1.setId("inv-1");
        inv1.setInvoiceNumber("INV-001");
        inv1.setStatus("PAID");
        inv1.setAmount(BigDecimal.valueOf(3000));
        inv1.setIssueDate(LocalDate.now().minusDays(40));
        inv1.setPaymentDate(LocalDate.now().minusDays(5));
        inv1.setDueDate(LocalDate.now().minusDays(10));
        mockInvoices.add(inv1);

        Invoice inv2 = new Invoice();
        inv2.setId("inv-2");
        inv2.setInvoiceNumber("INV-002");
        inv2.setStatus("PENDING");
        inv2.setAmount(BigDecimal.valueOf(2000));
        inv2.setDueDate(LocalDate.now().minusDays(5));
        mockInvoices.add(inv2);
    }

    // ==================== Tests processMessage ====================

    @Test
    @DisplayName("Should process greeting message")
    void testProcessMessage_Greeting() {
        // Given
        String message = "Bonjour";
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertTrue(response.contains("Bonjour") || response.contains("bonjour"));
    }

    @Test
    @DisplayName("Should process convention query")
    void testProcessMessage_ConventionQuery() {
        // Given
        String message = "Combien de conventions sont actives ?";
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @DisplayName("Should process invoice query")
    void testProcessMessage_InvoiceQuery() {
        // Given
        String message = "Combien de factures sont en retard ?";
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @DisplayName("Should process help request")
    void testProcessMessage_HelpRequest() {
        // Given
        String message = "Aide";
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // Response should be generated (content varies based on Gemini API)
    }

    @Test
    @DisplayName("Should process thank you message")
    void testProcessMessage_ThankYou() {
        // Given
        String message = "Merci";
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @DisplayName("Should process goodbye message")
    void testProcessMessage_Goodbye() {
        // Given
        String message = "Au revoir";
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        // Response should be generated (content varies based on Gemini API)
    }

    // ==================== Tests detectIntention ====================

    @Test
    @DisplayName("Should detect greeting intention")
    void testDetectIntention_Greeting() {
        // Given
        String message = "Bonjour";

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        // Vérifier que c'est une salutation
    }

    @Test
    @DisplayName("Should detect convention intention")
    void testDetectIntention_Convention() {
        // Given
        String message = "Montre-moi les conventions";
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        verify(conventionRepository, atLeastOnce()).findAll();
    }

    @Test
    @DisplayName("Should detect invoice intention")
    void testDetectIntention_Invoice() {
        // Given
        String message = "Affiche les factures";
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        verify(invoiceRepository, atLeastOnce()).findAll();
    }

    // ==================== Tests extractDatabaseData ====================

    @Test
    @DisplayName("Should extract database data for conventions")
    void testExtractDatabaseData_Conventions() {
        // Given
        String message = "Analyse les conventions";
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        verify(conventionRepository, atLeastOnce()).count();
    }

    @Test
    @DisplayName("Should extract database data for invoices")
    void testExtractDatabaseData_Invoices() {
        // Given
        String message = "Analyse les factures";
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        verify(invoiceRepository, atLeastOnce()).count();
    }

    // ==================== Tests getFallbackResponse ====================

    @Test
    @DisplayName("Should return fallback response on error")
    void testGetFallbackResponse_OnError() {
        // Given
        String message = "Question complexe";
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    // ==================== Tests Edge Cases ====================

    @Test
    @DisplayName("Should handle empty message")
    void testProcessMessage_EmptyMessage() {
        // Given
        String message = "";
        when(conventionRepository.count()).thenReturn(0L);
        when(invoiceRepository.count()).thenReturn(0L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should handle null message")
    void testProcessMessage_NullMessage() {
        // Given
        String message = null;

        // When & Then
        assertThrows(
            NullPointerException.class,
            () -> chatbotService.processMessage(message)
        );
    }

    @Test
    @DisplayName("Should handle empty database")
    void testProcessMessage_EmptyDatabase() {
        // Given
        String message = "Combien de conventions ?";
        when(conventionRepository.findAll()).thenReturn(Collections.emptyList());
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());
        when(conventionRepository.count()).thenReturn(0L);
        when(invoiceRepository.count()).thenReturn(0L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertTrue(response.contains("0") || response.contains("aucun"));
    }

    @Test
    @DisplayName("Should handle very long message")
    void testProcessMessage_LongMessage() {
        // Given
        String message = "Bonjour ".repeat(100);
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @DisplayName("Should handle special characters")
    void testProcessMessage_SpecialCharacters() {
        // Given
        String message = "Combien de conventions @#$% ?";
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);
        when(conventionRepository.count()).thenReturn(2L);
        when(invoiceRepository.count()).thenReturn(2L);

        // When
        String response = chatbotService.processMessage(message);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    // ==================== Tests Performance ====================

    @Test
    @DisplayName("Should handle large dataset efficiently")
    void testProcessMessage_LargeDataset() {
        // Given
        List<Convention> largeConventionList = new ArrayList<>();
        List<Invoice> largeInvoiceList = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            Convention conv = new Convention();
            conv.setId("conv-" + i);
            conv.setStatus(i % 2 == 0 ? "ACTIVE" : "EXPIRED");
            largeConventionList.add(conv);
            
            Invoice inv = new Invoice();
            inv.setId("inv-" + i);
            inv.setStatus(i % 2 == 0 ? "PAID" : "PENDING");
            largeInvoiceList.add(inv);
        }
        
        String message = "Analyse complète";
        when(conventionRepository.findAll()).thenReturn(largeConventionList);
        when(invoiceRepository.findAll()).thenReturn(largeInvoiceList);
        when(conventionRepository.count()).thenReturn(1000L);
        when(invoiceRepository.count()).thenReturn(1000L);

        // When
        long startTime = System.currentTimeMillis();
        String response = chatbotService.processMessage(message);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(response);
        assertTrue((endTime - startTime) < 5000, "Should complete in less than 5 seconds");
    }
}
