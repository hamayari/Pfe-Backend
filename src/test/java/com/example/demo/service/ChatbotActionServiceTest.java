package com.example.demo.service;

import com.example.demo.dto.chatbot.ActionRequest;
import com.example.demo.dto.chatbot.ActionResponse;
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
class ChatbotActionServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ChatbotActionService chatbotActionService;

    @Test
    void testExecuteAction_CreateConvention() {
        Map<String, Object> params = new HashMap<>();
        params.put("title", "Test Convention");
        params.put("reference", "REF001");
        params.put("structureId", "struct1");
        params.put("applicationId", "app1");
        params.put("governorate", "Tunis");
        params.put("amount", "1000");
        params.put("startDate", LocalDate.now().toString());
        params.put("endDate", LocalDate.now().plusMonths(1).toString());

        ActionRequest request = new ActionRequest();
        request.setAction("create_convention");
        request.setParameters(params);

        Convention mockConvention = new Convention();
        mockConvention.setId("conv1");
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);

        ActionResponse response = chatbotActionService.executeAction(request);

        assertTrue(response.isSuccess());
        verify(conventionRepository).save(any(Convention.class));
    }

    @Test
    void testExecuteAction_CreateFacture() {
        Map<String, Object> params = new HashMap<>();
        params.put("invoiceNumber", "FACT001");
        params.put("conventionId", "conv1");
        params.put("amount", "500");
        params.put("issueDate", LocalDate.now().toString());
        params.put("dueDate", LocalDate.now().plusDays(30).toString());

        ActionRequest request = new ActionRequest();
        request.setAction("create_facture");
        request.setParameters(params);

        Invoice mockInvoice = new Invoice();
        mockInvoice.setId("inv1");
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);

        ActionResponse response = chatbotActionService.executeAction(request);

        assertTrue(response.isSuccess());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void testExecuteAction_GetUnpaidInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        Invoice inv1 = new Invoice();
        inv1.setStatus("PENDING");
        invoices.add(inv1);

        when(invoiceRepository.findAll()).thenReturn(invoices);

        ActionRequest request = new ActionRequest();
        request.setAction("get_unpaid_invoices");
        request.setParameters(new HashMap<>());

        ActionResponse response = chatbotActionService.executeAction(request);

        assertTrue(response.isSuccess());
    }

    @Test
    void testExecuteAction_MarkAsPaid() {
        Map<String, Object> params = new HashMap<>();
        params.put("invoiceId", "inv1");

        Invoice mockInvoice = new Invoice();
        mockInvoice.setId("inv1");
        mockInvoice.setStatus("PENDING");

        when(invoiceRepository.findById("inv1")).thenReturn(Optional.of(mockInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);

        ActionRequest request = new ActionRequest();
        request.setAction("mark_as_paid");
        request.setParameters(params);

        ActionResponse response = chatbotActionService.executeAction(request);

        assertTrue(response.isSuccess());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void testExecuteAction_UnknownAction() {
        ActionRequest request = new ActionRequest();
        request.setAction("unknown_action");
        request.setParameters(new HashMap<>());

        ActionResponse response = chatbotActionService.executeAction(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("non reconnue"));
    }
}
