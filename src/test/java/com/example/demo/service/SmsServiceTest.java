package com.example.demo.service;

import com.example.demo.dto.SmsRequestDTO;
import com.example.demo.dto.SmsResponseDTO;
import com.example.demo.dto.SmsStatsDTO;
import com.example.demo.model.SmsNotification;
import com.example.demo.repository.SmsNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private SmsNotificationRepository smsNotificationRepository;

    @InjectMocks
    private SmsService smsService;

    private SmsRequestDTO mockRequest;
    private SmsNotification mockNotification;

    @BeforeEach
    void setUp() {
        // Activer le mode simulation pour les tests
        ReflectionTestUtils.setField(smsService, "simulationMode", true);
        ReflectionTestUtils.setField(smsService, "fromPhoneNumber", "+33612345678");

        mockRequest = new SmsRequestDTO();
        mockRequest.setTo("+33612345678");
        mockRequest.setMessage("Test SMS");
        mockRequest.setUserId("user1");
        mockRequest.setType("TEST");

        mockNotification = new SmsNotification();
        mockNotification.setId("sms1");
        mockNotification.setTo("+33612345678");
        mockNotification.setMessage("Test SMS");
        mockNotification.setStatus("SENT");
        mockNotification.setSentAt(LocalDateTime.now());
    }

    @Test
    void testSendSms_Success() {
        when(smsNotificationRepository.save(any(SmsNotification.class))).thenAnswer(invocation -> {
            SmsNotification notification = invocation.getArgument(0);
            notification.setId("sms1");
            return notification;
        });

        SmsResponseDTO result = smsService.sendSms(mockRequest);

        assertTrue(result.isSuccess());
        assertNotNull(result.getSmsId());
        verify(smsNotificationRepository).save(any(SmsNotification.class));
    }

    @Test
    void testSendSms_InvalidPhoneNumber() {
        mockRequest.setTo("invalid");

        SmsResponseDTO result = smsService.sendSms(mockRequest);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("invalide"));
    }

    @Test
    void testSendSmsWithTemplate_InvoiceReminder() {
        Map<String, String> variables = new HashMap<>();
        variables.put("invoiceNumber", "INV-001");
        variables.put("amount", "1000");
        variables.put("dueDate", "2024-12-31");

        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendSmsWithTemplate("+33612345678", "invoice_reminder", variables);

        assertTrue(result.isSuccess());
        verify(smsNotificationRepository).save(any(SmsNotification.class));
    }

    @Test
    void testSendSmsWithTemplate_InvoiceOverdue() {
        Map<String, String> variables = new HashMap<>();
        variables.put("invoiceNumber", "INV-001");
        variables.put("amount", "1000");
        variables.put("daysOverdue", "5");

        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendSmsWithTemplate("+33612345678", "invoice_overdue", variables);

        assertTrue(result.isSuccess());
    }

    @Test
    void testSendSmsWithTemplate_PaymentReceived() {
        Map<String, String> variables = new HashMap<>();
        variables.put("invoiceNumber", "INV-001");
        variables.put("amount", "1000");
        variables.put("paymentMethod", "Carte bancaire");

        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendSmsWithTemplate("+33612345678", "payment_received", variables);

        assertTrue(result.isSuccess());
    }

    @Test
    void testSendSmsWithTemplate_KpiAlert() {
        Map<String, String> variables = new HashMap<>();
        variables.put("severity", "HIGH");
        variables.put("kpiName", "TAUX_RETARD");
        variables.put("currentValue", "25.5");
        variables.put("priority", "HIGH");

        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendSmsWithTemplate("+33612345678", "kpi_alert", variables);

        assertTrue(result.isSuccess());
    }

    @Test
    void testSendEcheanceReminder() {
        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendEcheanceReminder("+33612345678", "CONV-001", "5000", "2024-12-31", "user1");

        assertTrue(result.isSuccess());
        verify(smsNotificationRepository).save(any(SmsNotification.class));
    }

    @Test
    void testSendInvoiceNotification() {
        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendInvoiceNotification("+33612345678", "INV-001", "1000", "2024-12-31", "user1");

        assertTrue(result.isSuccess());
        verify(smsNotificationRepository).save(any(SmsNotification.class));
    }

    @Test
    void testSendPaymentConfirmation() {
        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendPaymentConfirmation("+33612345678", "INV-001", "1000", "user1");

        assertTrue(result.isSuccess());
        verify(smsNotificationRepository).save(any(SmsNotification.class));
    }

    @Test
    void testSendOverdueAlert() {
        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendOverdueAlert("+33612345678", "INV-001", "1000", 5, "user1");

        assertTrue(result.isSuccess());
        verify(smsNotificationRepository).save(any(SmsNotification.class));
    }

    @Test
    void testSendSystemNotification() {
        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendSystemNotification("+33612345678", "Test", "Message de test", "user1");

        assertTrue(result.isSuccess());
        verify(smsNotificationRepository).save(any(SmsNotification.class));
    }

    @Test
    void testGetSmsHistory() {
        when(smsNotificationRepository.findByUserIdOrderBySentAtDesc("user1")).thenReturn(Arrays.asList(mockNotification));

        List<SmsNotification> result = smsService.getSmsHistory("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(smsNotificationRepository).findByUserIdOrderBySentAtDesc("user1");
    }

    @Test
    void testGetSmsStats() {
        when(smsNotificationRepository.findByUserId("user1")).thenReturn(Arrays.asList(mockNotification));

        SmsStatsDTO result = smsService.getSmsStats("user1");

        assertNotNull(result);
        assertTrue(result.getTotalSent() >= 0);
        verify(smsNotificationRepository).findByUserId("user1");
    }

    @Test
    void testFormatFrenchPhoneNumber() {
        String result1 = smsService.formatFrenchPhoneNumber("0612345678");
        assertEquals("+33612345678", result1);

        String result2 = smsService.formatFrenchPhoneNumber("33612345678");
        assertEquals("+33612345678", result2);

        String result3 = smsService.formatFrenchPhoneNumber("+33612345678");
        assertEquals("+33612345678", result3);
    }

    @Test
    void testSendWhatsApp() {
        when(smsNotificationRepository.save(any(SmsNotification.class))).thenReturn(mockNotification);

        SmsResponseDTO result = smsService.sendWhatsApp("+33612345678", "Test WhatsApp");

        assertTrue(result.isSuccess());
        verify(smsNotificationRepository).save(any(SmsNotification.class));
    }

    @Test
    void testSendSms_NullPhoneNumber() {
        mockRequest.setTo(null);

        SmsResponseDTO result = smsService.sendSms(mockRequest);

        assertFalse(result.isSuccess());
    }

    @Test
    void testSendSms_EmptyPhoneNumber() {
        mockRequest.setTo("");

        SmsResponseDTO result = smsService.sendSms(mockRequest);

        assertFalse(result.isSuccess());
    }

    @Test
    void testGetSmsStats_NoSms() {
        when(smsNotificationRepository.findByUserId("user1")).thenReturn(Arrays.asList());

        SmsStatsDTO result = smsService.getSmsStats("user1");

        assertNotNull(result);
        assertEquals(0L, result.getTotalSent());
        assertEquals(0.0, result.getSuccessRate());
    }
}
