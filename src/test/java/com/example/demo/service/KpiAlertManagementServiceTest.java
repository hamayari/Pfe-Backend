package com.example.demo.service;

import com.example.demo.model.KpiAlert;
import com.example.demo.model.User;
import com.example.demo.repository.KpiAlertRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpiAlertManagementServiceTest {

    @Mock
    private KpiAlertRepository alertRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private KpiAlertManagementService managementService;

    private KpiAlert mockAlert;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockAlert = new KpiAlert();
        mockAlert.setId("alert1");
        mockAlert.setKpiName("FACTURE_PENDING");
        mockAlert.setCurrentValue(25.0);
        mockAlert.setStatus("ANORMAL");
        mockAlert.setSeverity("HIGH");
        mockAlert.setMessage("Taux de retard élevé");
        mockAlert.setAlertStatus("PENDING_DECISION");
        mockAlert.setRecipients(Arrays.asList("user1"));

        mockUser = new User();
        mockUser.setId("user1");
        mockUser.setUsername("testuser");
    }

    @Test
    void testCreateAlert() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(alertRepository.save(any(KpiAlert.class))).thenReturn(mockAlert);

        KpiAlert result = managementService.createAlert(mockAlert, "user1");

        assertNotNull(result);
        verify(alertRepository).save(any(KpiAlert.class));
        verify(userRepository).findById("user1");
    }

    @Test
    void testSendToProjectManager() {
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));
        when(userRepository.findById("decideur1")).thenReturn(Optional.of(mockUser));
        when(alertRepository.save(any(KpiAlert.class))).thenReturn(mockAlert);

        KpiAlert result = managementService.sendToProjectManager("alert1", "decideur1");

        assertNotNull(result);
        assertEquals("SENT_TO_PM", result.getAlertStatus());
        verify(alertRepository).save(any(KpiAlert.class));
    }

    @Test
    void testMarkAsInProgress() {
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(alertRepository.save(any(KpiAlert.class))).thenReturn(mockAlert);

        KpiAlert result = managementService.markAsInProgress("alert1", "user1", "En cours");

        assertNotNull(result);
        verify(alertRepository).save(any(KpiAlert.class));
    }

    @Test
    void testResolveAlert() {
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(alertRepository.save(any(KpiAlert.class))).thenReturn(mockAlert);

        KpiAlert result = managementService.resolveAlert("alert1", "user1", "Résolu", "Actions prises");

        assertNotNull(result);
        verify(alertRepository).save(any(KpiAlert.class));
    }

    @Test
    void testAcknowledgeAlert() {
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(alertRepository.save(any(KpiAlert.class))).thenReturn(mockAlert);

        KpiAlert result = managementService.acknowledgeAlert("alert1", "user1");

        assertNotNull(result);
        verify(alertRepository).save(any(KpiAlert.class));
    }

    @Test
    void testArchiveAlert() {
        mockAlert.setAlertStatus("RESOLVED");
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(alertRepository.save(any(KpiAlert.class))).thenReturn(mockAlert);

        KpiAlert result = managementService.archiveAlert("alert1", "user1");

        assertNotNull(result);
        verify(alertRepository).save(any(KpiAlert.class));
    }

    @Test
    void testAddComment() {
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(alertRepository.save(any(KpiAlert.class))).thenReturn(mockAlert);

        KpiAlert result = managementService.addComment("alert1", "user1", "Commentaire test");

        assertNotNull(result);
        verify(alertRepository).save(any(KpiAlert.class));
    }

    @Test
    void testGetAllPendingDecisionAlerts() {
        when(alertRepository.findByAlertStatus("PENDING_DECISION")).thenReturn(Arrays.asList(mockAlert));

        List<KpiAlert> result = managementService.getAllPendingDecisionAlerts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(alertRepository).findByAlertStatus("PENDING_DECISION");
    }

    @Test
    void testGetActiveAlerts() {
        when(alertRepository.findByRecipientsContainingAndAlertStatusIn(eq("user1"), anyList()))
            .thenReturn(Arrays.asList(mockAlert));

        List<KpiAlert> result = managementService.getActiveAlerts("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAlertStatistics() {
        when(alertRepository.findByRecipientsContaining("user1")).thenReturn(Arrays.asList(mockAlert));

        var result = managementService.getAlertStatistics("user1");

        assertNotNull(result);
        assertTrue(result.containsKey("total"));
        assertTrue(result.containsKey("active"));
    }

    @Test
    void testAutoArchiveOldResolvedAlerts() {
        mockAlert.setAlertStatus("RESOLVED");
        mockAlert.setResolvedAt(LocalDateTime.now().minusDays(35));
        when(alertRepository.findByAlertStatusAndResolvedAtAfter(eq("RESOLVED"), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(mockAlert));
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));
        when(userRepository.findById("system")).thenReturn(Optional.of(mockUser));
        when(alertRepository.save(any(KpiAlert.class))).thenReturn(mockAlert);

        int result = managementService.autoArchiveOldResolvedAlerts();

        assertTrue(result >= 0);
    }

    @Test
    void testSendToProjectManager_InvalidStatus() {
        mockAlert.setAlertStatus("RESOLVED");
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));

        assertThrows(RuntimeException.class, () -> {
            managementService.sendToProjectManager("alert1", "decideur1");
        });
    }

    @Test
    void testArchiveAlert_NotResolved() {
        mockAlert.setAlertStatus("PENDING_DECISION");
        when(alertRepository.findById("alert1")).thenReturn(Optional.of(mockAlert));

        assertThrows(RuntimeException.class, () -> {
            managementService.archiveAlert("alert1", "user1");
        });
    }
}
