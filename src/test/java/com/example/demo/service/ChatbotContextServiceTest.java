package com.example.demo.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotContextServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ChatbotContextService chatbotContextService;

    private List<Convention> mockConventions;
    private List<Invoice> mockInvoices;

    @BeforeEach
    void setUp() {
        mockConventions = new ArrayList<>();
        Convention conv1 = new Convention();
        conv1.setId("conv1");
        conv1.setStatus("ACTIVE");
        conv1.setAmount(BigDecimal.valueOf(1000));
        conv1.setGovernorate("Tunis");
        mockConventions.add(conv1);

        mockInvoices = new ArrayList<>();
        Invoice inv1 = new Invoice();
        inv1.setId("inv1");
        inv1.setStatus("PAID");
        inv1.setAmount(BigDecimal.valueOf(500));
        inv1.setDueDate(LocalDate.now().plusDays(30));
        mockInvoices.add(inv1);

        Invoice inv2 = new Invoice();
        inv2.setId("inv2");
        inv2.setStatus("OVERDUE");
        inv2.setAmount(BigDecimal.valueOf(300));
        inv2.setDueDate(LocalDate.now().minusDays(5));
        mockInvoices.add(inv2);
    }

    @Test
    void testPrepareContext_InvoiceQuestion() {
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);

        Map<String, Object> context = chatbotContextService.prepareContext("Combien de factures en retard?");

        assertNotNull(context);
        assertTrue(context.containsKey("facturesEnRetard"));
        assertTrue(context.containsKey("totalFactures"));
    }

    @Test
    void testPrepareContext_ConventionQuestion() {
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        Map<String, Object> context = chatbotContextService.prepareContext("Combien de conventions actives?");

        assertNotNull(context);
        assertTrue(context.containsKey("activeConventions"));
        assertTrue(context.containsKey("totalConventions"));
    }

    @Test
    void testPrepareContext_GeographicQuestion() {
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        Map<String, Object> context = chatbotContextService.prepareContext("Répartition par gouvernorat");

        assertNotNull(context);
        assertTrue(context.containsKey("conventions_par_gouvernorat"));
    }

    @Test
    void testPrepareContext_GlobalStats() {
        Map<String, Object> context = chatbotContextService.prepareContext("Statistiques générales");

        assertNotNull(context);
        assertTrue(context.containsKey("date_analyse"));
        assertTrue(context.containsKey("annee_courante"));
    }
}
