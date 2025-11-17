package com.example.demo.scheduler;

import com.example.demo.model.KpiAlert;
import com.example.demo.service.InvoiceAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertScheduler Tests")
class AlertSchedulerTest {

    @Mock
    private InvoiceAlertService invoiceAlertService;

    @InjectMocks
    private AlertScheduler alertScheduler;

    @BeforeEach
    void setUp() {
        // Setup is handled by @Mock and @InjectMocks
    }

    @Test
    @DisplayName("Should generate overdue invoice alerts successfully")
    void testGenerateOverdueInvoiceAlerts() {
        // Arrange
        KpiAlert alert1 = new KpiAlert();
        KpiAlert alert2 = new KpiAlert();
        List<KpiAlert> mockAlerts = Arrays.asList(alert1, alert2);
        when(invoiceAlertService.checkPendingInvoices()).thenReturn(mockAlerts);

        // Act
        alertScheduler.generateOverdueInvoiceAlerts();

        // Assert
        verify(invoiceAlertService, times(1)).checkPendingInvoices();
    }

    @Test
    @DisplayName("Should handle empty alert list")
    void testGenerateOverdueInvoiceAlertsWithEmptyList() {
        // Arrange
        when(invoiceAlertService.checkPendingInvoices()).thenReturn(Collections.emptyList());

        // Act
        alertScheduler.generateOverdueInvoiceAlerts();

        // Assert
        verify(invoiceAlertService, times(1)).checkPendingInvoices();
    }

    @Test
    @DisplayName("Should handle exception during alert generation")
    void testGenerateOverdueInvoiceAlertsWithException() {
        // Arrange
        when(invoiceAlertService.checkPendingInvoices())
                .thenThrow(new RuntimeException("Database error"));

        // Act - should not throw exception
        alertScheduler.generateOverdueInvoiceAlerts();

        // Assert
        verify(invoiceAlertService, times(1)).checkPendingInvoices();
    }

    @Test
    @DisplayName("Should force generation manually")
    void testForceGeneration() {
        // Arrange
        KpiAlert alert = new KpiAlert();
        List<KpiAlert> mockAlerts = Collections.singletonList(alert);
        when(invoiceAlertService.checkPendingInvoices()).thenReturn(mockAlerts);

        // Act
        alertScheduler.forceGeneration();

        // Assert
        verify(invoiceAlertService, times(1)).checkPendingInvoices();
    }

    @Test
    @DisplayName("Should handle multiple alerts generation")
    void testGenerateMultipleAlerts() {
        // Arrange
        KpiAlert alert1 = new KpiAlert();
        KpiAlert alert2 = new KpiAlert();
        KpiAlert alert3 = new KpiAlert();
        List<KpiAlert> mockAlerts = Arrays.asList(alert1, alert2, alert3);
        when(invoiceAlertService.checkPendingInvoices()).thenReturn(mockAlerts);

        // Act
        alertScheduler.generateOverdueInvoiceAlerts();

        // Assert
        verify(invoiceAlertService, times(1)).checkPendingInvoices();
    }

    @Test
    @DisplayName("Should call service exactly once per execution")
    void testServiceCalledOnce() {
        // Arrange
        when(invoiceAlertService.checkPendingInvoices()).thenReturn(Collections.emptyList());

        // Act
        alertScheduler.generateOverdueInvoiceAlerts();

        // Assert
        verify(invoiceAlertService, times(1)).checkPendingInvoices();
        verifyNoMoreInteractions(invoiceAlertService);
    }
}
