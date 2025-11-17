package com.example.demo.scheduler;

import com.example.demo.model.AlertConfiguration;
import com.example.demo.model.Convention;
import com.example.demo.model.Notification;
import com.example.demo.repository.AlertConfigurationRepository;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConventionAlertScheduler Tests")
class ConventionAlertSchedulerTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AlertConfigurationRepository alertConfigurationRepository;

    @InjectMocks
    private ConventionAlertScheduler scheduler;

    private AlertConfiguration defaultConfig;

    @BeforeEach
    void setUp() {
        defaultConfig = AlertConfiguration.getDefaultConfiguration();
        defaultConfig.setActive(true);
        defaultConfig.setAlert30DaysEnabled(true);
        defaultConfig.setAlert15DaysEnabled(true);
        defaultConfig.setAlert7DaysEnabled(true);
        defaultConfig.setAlert1DayEnabled(true);
        defaultConfig.setAlertSameDayEnabled(true);
        defaultConfig.setWebsocketNotificationsEnabled(true);
    }

    @Test
    @DisplayName("Should send expiration alerts successfully")
    void testSendExpirationAlerts() {
        // Arrange
        Convention convention = createConvention("CONV-001", LocalDate.now().plusDays(7));
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.of(defaultConfig));
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(convention));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        scheduler.sendExpirationAlerts();

        // Assert
        verify(conventionRepository, times(1)).findByStatus("ACTIVE");
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle empty convention list")
    void testSendExpirationAlertsWithEmptyList() {
        // Arrange
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.of(defaultConfig));
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Collections.emptyList());

        // Act
        scheduler.sendExpirationAlerts();

        // Assert
        verify(conventionRepository, times(1)).findByStatus("ACTIVE");
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should use default configuration when none found")
    void testSendExpirationAlertsWithDefaultConfig() {
        // Arrange
        Convention convention = createConvention("CONV-001", LocalDate.now().plusDays(7));
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(convention));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        scheduler.sendExpirationAlerts();

        // Assert
        verify(conventionRepository, times(1)).findByStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should skip conventions without end date")
    void testSendExpirationAlertsSkipsConventionsWithoutEndDate() {
        // Arrange
        Convention convention = createConvention("CONV-001", null);
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.of(defaultConfig));
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(convention));

        // Act
        scheduler.sendExpirationAlerts();

        // Assert
        verify(conventionRepository, times(1)).findByStatus("ACTIVE");
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should send alert for convention expiring today")
    void testSendAlertForConventionExpiringToday() {
        // Arrange
        Convention convention = createConvention("CONV-001", LocalDate.now());
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.of(defaultConfig));
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(convention));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        scheduler.sendExpirationAlerts();

        // Assert
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should send alert for convention expiring in 1 day")
    void testSendAlertForConventionExpiringIn1Day() {
        // Arrange
        Convention convention = createConvention("CONV-001", LocalDate.now().plusDays(1));
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.of(defaultConfig));
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(convention));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        scheduler.sendExpirationAlerts();

        // Assert
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle multiple conventions")
    void testSendAlertsForMultipleConventions() {
        // Arrange
        Convention conv1 = createConvention("CONV-001", LocalDate.now().plusDays(7));
        Convention conv2 = createConvention("CONV-002", LocalDate.now().plusDays(15));
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.of(defaultConfig));
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Arrays.asList(conv1, conv2));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        scheduler.sendExpirationAlerts();

        // Assert
        verify(conventionRepository, times(1)).findByStatus("ACTIVE");
        verify(notificationRepository, atLeast(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should force alerts manually")
    void testForceAlerts() {
        // Arrange
        Convention convention = createConvention("CONV-001", LocalDate.now().plusDays(7));
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.of(defaultConfig));
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(convention));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        scheduler.forceAlerts();

        // Assert
        verify(conventionRepository, times(1)).findByStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should handle exception during alert sending")
    void testHandleExceptionDuringAlertSending() {
        // Arrange
        Convention convention = createConvention("CONV-001", LocalDate.now().plusDays(7));
        when(alertConfigurationRepository.findFirstByActiveTrue()).thenReturn(Optional.of(defaultConfig));
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(convention));
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("Database error"));

        // Act - should not throw exception
        scheduler.sendExpirationAlerts();

        // Assert
        verify(conventionRepository, times(1)).findByStatus("ACTIVE");
    }

    private Convention createConvention(String reference, LocalDate endDate) {
        Convention convention = new Convention();
        convention.setId("test-id");
        convention.setReference(reference);
        convention.setTitle("Test Convention");
        convention.setStatus("ACTIVE");
        convention.setEndDate(endDate);
        convention.setCreatedBy("user-123");
        return convention;
    }
}
