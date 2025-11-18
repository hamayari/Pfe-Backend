package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.User;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.NotificationSettingsRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RealTimeNotificationService realTimeNotificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @InjectMocks
    private NotificationSchedulerService schedulerService;

    private Convention testConvention;
    private Invoice testInvoice;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("USER-001");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhoneNumber("+33612345678");

        testConvention = new Convention();
        testConvention.setId("CONV-001");
        testConvention.setReference("REF-001");
        testConvention.setTitle("Test Convention");
        testConvention.setAmount(BigDecimal.valueOf(10000));
        testConvention.setCreatedBy("USER-001");
        testConvention.setEcheances(Arrays.asList(LocalDate.now().plusDays(7)));

        testInvoice = new Invoice();
        testInvoice.setId("INV-001");
        testInvoice.setInvoiceNumber("2024-001");
        testInvoice.setAmount(BigDecimal.valueOf(1000));
        testInvoice.setDueDate(LocalDate.now().plusDays(7));
        testInvoice.setCreatedBy("USER-001");
        testInvoice.setStatus("PENDING");

        ReflectionTestUtils.setField(schedulerService, "reminderDaysConfig", "7,3,1");
        ReflectionTestUtils.setField(schedulerService, "emailEnabled", true);
        ReflectionTestUtils.setField(schedulerService, "smsEnabled", true);
    }

    @Test
    void testCheckDueDatesAndSendNotifications() {
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
    }

    @Test
    void testCheckDueDatesAndSendNotifications_WithConventions() {
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(Arrays.asList(testConvention));
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(realTimeNotificationService.createNotification(any())).thenReturn(null);

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
        
        verify(realTimeNotificationService, atLeastOnce()).createNotification(any());
    }

    @Test
    void testCheckDueDatesAndSendNotifications_WithInvoices() {
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(realTimeNotificationService.createNotification(any())).thenReturn(null);

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
        
        verify(realTimeNotificationService, atLeastOnce()).createNotification(any());
    }

    @Test
    void testCheckDueDatesAndSendNotifications_WithOverdueInvoices() {
        testInvoice.setDueDate(LocalDate.now().minusDays(5));
        
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(Arrays.asList(testInvoice));
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(realTimeNotificationService.createNotification(any())).thenReturn(null);

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
        
        verify(realTimeNotificationService, atLeastOnce()).createNotification(any());
    }

    @Test
    void testTriggerManualCheck() {
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> schedulerService.triggerManualCheck());
    }

    @Test
    void testCheckDueDatesAndSendNotifications_EmailDisabled() throws Exception {
        ReflectionTestUtils.setField(schedulerService, "emailEnabled", false);
        
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(Arrays.asList(testConvention));
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(realTimeNotificationService.createNotification(any())).thenReturn(null);

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
        
        verify(emailService, never()).sendConventionReminderEmail(anyString(), any());
    }

    @Test
    void testCheckDueDatesAndSendNotifications_SmsDisabled() {
        ReflectionTestUtils.setField(schedulerService, "smsEnabled", false);
        
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(Arrays.asList(testConvention));
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(realTimeNotificationService.createNotification(any())).thenReturn(null);

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
        
        verify(smsService, never()).sendSmsWithTemplate(anyString(), anyString(), any());
    }

    @Test
    void testCheckDueDatesAndSendNotifications_UserNotFound() {
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(Arrays.asList(testConvention));
        when(userRepository.findById("USER-001")).thenReturn(Optional.empty());
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
        
        verify(realTimeNotificationService, never()).createNotification(any());
    }

    @Test
    void testCheckDueDatesAndSendNotifications_Exception() {
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenThrow(new RuntimeException("Database error"));
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
    }

    @Test
    void testCheckDueDatesAndSendNotifications_WithOverdueConventions() {
        when(conventionRepository.findByEcheancesContaining(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDate(any(LocalDate.class)))
            .thenReturn(new ArrayList<>());
        when(invoiceRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(new ArrayList<>());
        when(conventionRepository.findByEcheancesContainingAndStatusNot(any(LocalDate.class), anyString()))
            .thenReturn(Arrays.asList(testConvention));
        when(userRepository.findById("USER-001")).thenReturn(Optional.of(testUser));
        when(notificationSettingsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(realTimeNotificationService.createNotification(any())).thenReturn(null);

        assertDoesNotThrow(() -> schedulerService.checkDueDatesAndSendNotifications());
        
        verify(realTimeNotificationService, atLeastOnce()).createNotification(any());
    }
}
