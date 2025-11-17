package com.example.demo.scheduler;

import com.example.demo.model.KpiAlert;
import com.example.demo.service.KpiEvaluatorService;
import com.example.demo.service.KpiNotificationService;
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
@DisplayName("KpiAnalysisScheduler Tests")
class KpiAnalysisSchedulerTest {

    @Mock
    private KpiEvaluatorService evaluatorService;

    @Mock
    private KpiNotificationService notificationService;

    @InjectMocks
    private KpiAnalysisScheduler scheduler;

    @BeforeEach
    void setUp() {
        // Setup is handled by @Mock and @InjectMocks
    }

    @Test
    @DisplayName("Should perform daily KPI analysis successfully")
    void testDailyKpiAnalysis() {
        // Arrange
        KpiAlert alert1 = createKpiAlert("MEDIUM");
        KpiAlert alert2 = createKpiAlert("HIGH");
        List<KpiAlert> alerts = Arrays.asList(alert1, alert2);
        when(evaluatorService.analyzeAllKpis()).thenReturn(alerts);

        // Act
        scheduler.dailyKpiAnalysis();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
        verify(notificationService, times(1)).sendAlertNotifications(alerts);
    }

    @Test
    @DisplayName("Should handle empty alerts in daily analysis")
    void testDailyKpiAnalysisWithEmptyAlerts() {
        // Arrange
        when(evaluatorService.analyzeAllKpis()).thenReturn(Collections.emptyList());

        // Act
        scheduler.dailyKpiAnalysis();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
        verify(notificationService, never()).sendAlertNotifications(any());
    }

    @Test
    @DisplayName("Should handle exception during daily analysis")
    void testDailyKpiAnalysisWithException() {
        // Arrange
        when(evaluatorService.analyzeAllKpis()).thenThrow(new RuntimeException("Analysis error"));

        // Act - should not throw exception
        scheduler.dailyKpiAnalysis();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
    }

    @Test
    @DisplayName("Should perform weekly KPI analysis successfully")
    void testWeeklyKpiAnalysis() {
        // Arrange
        KpiAlert alert = createKpiAlert("HIGH");
        List<KpiAlert> alerts = Collections.singletonList(alert);
        when(evaluatorService.analyzeAllKpis()).thenReturn(alerts);

        // Act
        scheduler.weeklyKpiAnalysis();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
        verify(notificationService, times(1)).sendWeeklyReport(alerts);
    }

    @Test
    @DisplayName("Should handle exception during weekly analysis")
    void testWeeklyKpiAnalysisWithException() {
        // Arrange
        when(evaluatorService.analyzeAllKpis()).thenThrow(new RuntimeException("Analysis error"));

        // Act - should not throw exception
        scheduler.weeklyKpiAnalysis();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
    }

    @Test
    @DisplayName("Should perform monthly KPI analysis successfully")
    void testMonthlyKpiAnalysis() {
        // Arrange
        KpiAlert alert = createKpiAlert("CRITICAL");
        List<KpiAlert> alerts = Collections.singletonList(alert);
        when(evaluatorService.analyzeAllKpis()).thenReturn(alerts);

        // Act
        scheduler.monthlyKpiAnalysis();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
        verify(notificationService, times(1)).sendMonthlyReport(alerts);
    }

    @Test
    @DisplayName("Should handle exception during monthly analysis")
    void testMonthlyKpiAnalysisWithException() {
        // Arrange
        when(evaluatorService.analyzeAllKpis()).thenThrow(new RuntimeException("Analysis error"));

        // Act - should not throw exception
        scheduler.monthlyKpiAnalysis();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
    }

    @Test
    @DisplayName("Should perform periodic KPI check successfully")
    void testPeriodicKpiCheck() {
        // Arrange
        KpiAlert criticalAlert = createKpiAlert("CRITICAL");
        KpiAlert highAlert = createKpiAlert("HIGH");
        KpiAlert mediumAlert = createKpiAlert("MEDIUM");
        List<KpiAlert> alerts = Arrays.asList(criticalAlert, highAlert, mediumAlert);
        when(evaluatorService.analyzeAllKpis()).thenReturn(alerts);

        // Act
        scheduler.periodicKpiCheck();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
        verify(notificationService, times(1)).sendUrgentAlerts(anyList());
    }

    @Test
    @DisplayName("Should not send urgent alerts when no critical alerts")
    void testPeriodicKpiCheckWithNoCriticalAlerts() {
        // Arrange
        KpiAlert lowAlert = createKpiAlert("LOW");
        List<KpiAlert> alerts = Collections.singletonList(lowAlert);
        when(evaluatorService.analyzeAllKpis()).thenReturn(alerts);

        // Act
        scheduler.periodicKpiCheck();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
        verify(notificationService, never()).sendUrgentAlerts(any());
    }

    @Test
    @DisplayName("Should handle exception during periodic check")
    void testPeriodicKpiCheckWithException() {
        // Arrange
        when(evaluatorService.analyzeAllKpis()).thenThrow(new RuntimeException("Check error"));

        // Act - should not throw exception
        scheduler.periodicKpiCheck();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
    }

    @Test
    @DisplayName("Should send urgent alerts only for high and critical severity")
    void testPeriodicKpiCheckFiltersCorrectly() {
        // Arrange
        KpiAlert criticalAlert = createKpiAlert("CRITICAL");
        KpiAlert highAlert = createKpiAlert("HIGH");
        KpiAlert mediumAlert = createKpiAlert("MEDIUM");
        KpiAlert lowAlert = createKpiAlert("LOW");
        List<KpiAlert> alerts = Arrays.asList(criticalAlert, highAlert, mediumAlert, lowAlert);
        when(evaluatorService.analyzeAllKpis()).thenReturn(alerts);

        // Act
        scheduler.periodicKpiCheck();

        // Assert
        verify(evaluatorService, times(1)).analyzeAllKpis();
        verify(notificationService, times(1)).sendUrgentAlerts(argThat(list -> 
            list.size() == 2 && 
            list.stream().allMatch(alert -> 
                "HIGH".equals(alert.getSeverity()) || "CRITICAL".equals(alert.getSeverity())
            )
        ));
    }

    private KpiAlert createKpiAlert(String severity) {
        KpiAlert alert = new KpiAlert();
        alert.setSeverity(severity);
        alert.setKpiName("Test KPI");
        alert.setMessage("Test alert message");
        return alert;
    }
}
