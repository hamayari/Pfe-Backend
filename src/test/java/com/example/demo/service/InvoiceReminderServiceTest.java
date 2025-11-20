package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.model.User;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
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
class InvoiceReminderServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InAppNotificationService inAppNotificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private InvoiceReminderService service;

    private User mockCommercial;
    private Invoice mockInvoice;

    @BeforeEach
    void setUp() {
        mockCommercial = createMockUser();
        mockInvoice = createMockInvoice();
    }

    @Test
    void testSendDailyReminders_Success() {
        // Given
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.singletonList(mockInvoice));
        when(invoiceRepository.findByStatus("OVERDUE"))
            .thenReturn(Collections.emptyList());
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.of(mockCommercial));

        // When
        service.sendDailyReminders();

        // Then
        verify(invoiceRepository, atLeastOnce()).findByStatus("PENDING");
        verify(invoiceRepository, atLeastOnce()).findByStatus("OVERDUE");
    }

    @Test
    void testSendTestReminders_Success() {
        // Given
        when(invoiceRepository.findByStatus(anyString()))
            .thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = service.sendTestReminders();

        // Then
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertNotNull(result.get("message"));
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void testUpcomingDueReminders_7Days() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setDueDate(LocalDate.now().plusDays(7));
        
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.singletonList(invoice));
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.of(mockCommercial));

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, atLeastOnce()).createNotification(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void testUpcomingDueReminders_3Days() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setDueDate(LocalDate.now().plusDays(3));
        
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.singletonList(invoice));
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.of(mockCommercial));

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, atLeastOnce()).createNotification(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void testUpcomingDueReminders_1Day() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setDueDate(LocalDate.now().plusDays(1));
        
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.singletonList(invoice));
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.of(mockCommercial));

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, atLeastOnce()).createNotification(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void testOverdueReminders_Critical() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setStatus("OVERDUE");
        invoice.setDueDate(LocalDate.now().minusDays(35));
        
        when(invoiceRepository.findByStatus("OVERDUE"))
            .thenReturn(Collections.singletonList(invoice));
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.emptyList());
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.of(mockCommercial));

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, atLeastOnce()).createNotification(
            anyString(), anyString(), contains("URGENT"), anyString(), eq("urgent"), anyString()
        );
    }

    @Test
    void testOverdueReminders_High() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setStatus("OVERDUE");
        invoice.setDueDate(LocalDate.now().minusDays(20));
        
        when(invoiceRepository.findByStatus("OVERDUE"))
            .thenReturn(Collections.singletonList(invoice));
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.emptyList());
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.of(mockCommercial));

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, atLeastOnce()).createNotification(
            anyString(), anyString(), anyString(), anyString(), eq("high"), anyString()
        );
    }

    @Test
    void testOverdueReminders_Medium() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setStatus("OVERDUE");
        invoice.setDueDate(LocalDate.now().minusDays(10));
        
        when(invoiceRepository.findByStatus("OVERDUE"))
            .thenReturn(Collections.singletonList(invoice));
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.emptyList());
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.of(mockCommercial));

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, atLeastOnce()).createNotification(
            anyString(), anyString(), anyString(), anyString(), eq("medium"), anyString()
        );
    }

    @Test
    void testPendingInvoiceReminders() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setIssueDate(LocalDate.now().minusDays(35));
        
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.singletonList(invoice));
        when(invoiceRepository.findByStatus("OVERDUE"))
            .thenReturn(Collections.emptyList());
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.of(mockCommercial));

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, atLeastOnce()).createNotification(
            anyString(), eq("invoice_pending"), anyString(), anyString(), eq("medium"), anyString()
        );
    }

    @Test
    void testReminderWithoutCommercial() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setCreatedBy(null);
        
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.singletonList(invoice));
        when(invoiceRepository.findByStatus("OVERDUE"))
            .thenReturn(Collections.emptyList());

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, never()).createNotification(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void testReminderWithNonExistentCommercial() {
        // Given
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.singletonList(mockInvoice));
        when(invoiceRepository.findByStatus("OVERDUE"))
            .thenReturn(Collections.emptyList());
        when(userRepository.findById(anyString()))
            .thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString()))
            .thenReturn(Optional.empty());

        // When
        service.sendDailyReminders();

        // Then
        verify(inAppNotificationService, never()).createNotification(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void testReminderWithNullDueDate() {
        // Given
        Invoice invoice = createMockInvoice();
        invoice.setDueDate(null);
        
        when(invoiceRepository.findByStatus("PENDING"))
            .thenReturn(Collections.singletonList(invoice));
        when(invoiceRepository.findByStatus("OVERDUE"))
            .thenReturn(Collections.emptyList());

        // When
        service.sendDailyReminders();

        // Then - Should not crash, just skip the invoice
        verify(inAppNotificationService, never()).createNotification(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        );
    }

    // Helper methods
    private User createMockUser() {
        User user = new User();
        user.setId("user123");
        user.setUsername("commercial1");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPhoneNumber("+21612345678");
        return user;
    }

    private Invoice createMockInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId("inv123");
        invoice.setReference("INV-2024-001");
        invoice.setAmount(BigDecimal.valueOf(1500.00));
        invoice.setStatus("PENDING");
        invoice.setDueDate(LocalDate.now().plusDays(7));
        invoice.setIssueDate(LocalDate.now().minusDays(10));
        invoice.setClientEmail("client@example.com");
        invoice.setCreatedBy("user123");
        return invoice;
    }
}
