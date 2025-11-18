package com.example.demo.service;

import com.example.demo.model.NotificationPreferences;
import com.example.demo.repository.NotificationPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferencesServiceTest {

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @InjectMocks
    private NotificationPreferencesService preferencesService;

    private NotificationPreferences testPreferences;

    @BeforeEach
    void setUp() {
        testPreferences = new NotificationPreferences("USER-001");
        testPreferences.setEmailEnabled(true);
        testPreferences.setSmsEnabled(false);
        testPreferences.setPushEnabled(true);
        testPreferences.setEmailFrequency("immediate");
        testPreferences.setQuietHoursEnabled(false);
        testPreferences.setQuietHoursStart("22:00");
        testPreferences.setQuietHoursEnd("07:00");
        testPreferences.setQuietHoursDays(new String[]{"saturday", "sunday"});
    }

    @Test
    void testGetUserPreferences_Existing() {
        when(preferencesRepository.findByUserId("USER-001")).thenReturn(Optional.of(testPreferences));

        NotificationPreferences result = preferencesService.getUserPreferences("USER-001");

        assertNotNull(result);
        assertEquals("USER-001", result.getUserId());
        assertTrue(result.isEmailEnabled());
        verify(preferencesRepository).findByUserId("USER-001");
    }

    @Test
    void testGetUserPreferences_CreateDefault() {
        when(preferencesRepository.findByUserId("USER-002")).thenReturn(Optional.empty());
        when(preferencesRepository.save(any(NotificationPreferences.class))).thenReturn(testPreferences);

        NotificationPreferences result = preferencesService.getUserPreferences("USER-002");

        assertNotNull(result);
        verify(preferencesRepository).save(any(NotificationPreferences.class));
    }

    @Test
    void testUpdatePreferences() {
        when(preferencesRepository.save(any(NotificationPreferences.class))).thenReturn(testPreferences);

        NotificationPreferences result = preferencesService.updatePreferences(testPreferences);

        assertNotNull(result);
        assertNotNull(result.getUpdatedAt());
        verify(preferencesRepository).save(testPreferences);
    }

    @Test
    void testShouldSendNotification_EmailEnabled() {
        when(preferencesRepository.findByUserId("USER-001")).thenReturn(Optional.of(testPreferences));

        boolean result = preferencesService.shouldSendNotification("USER-001", "invoice", "email");

        assertTrue(result);
    }

    @Test
    void testShouldSendNotification_SmsDisabled() {
        when(preferencesRepository.findByUserId("USER-001")).thenReturn(Optional.of(testPreferences));

        boolean result = preferencesService.shouldSendNotification("USER-001", "invoice", "sms");

        assertFalse(result);
    }

    @Test
    void testShouldSendNotification_PushEnabled() {
        when(preferencesRepository.findByUserId("USER-001")).thenReturn(Optional.of(testPreferences));

        boolean result = preferencesService.shouldSendNotification("USER-001", "invoice", "push");

        assertTrue(result);
    }

    @Test
    void testShouldSendNotification_InvalidChannel() {
        when(preferencesRepository.findByUserId("USER-001")).thenReturn(Optional.of(testPreferences));

        boolean result = preferencesService.shouldSendNotification("USER-001", "invoice", "invalid");

        assertFalse(result);
    }

    @Test
    void testShouldSendNotification_QuietHoursDisabled() {
        testPreferences.setQuietHoursEnabled(false);
        when(preferencesRepository.findByUserId("USER-001")).thenReturn(Optional.of(testPreferences));

        boolean result = preferencesService.shouldSendNotification("USER-001", "invoice", "email");

        assertTrue(result);
    }

    @Test
    void testClearPreferencesCache() {
        assertDoesNotThrow(() -> preferencesService.clearPreferencesCache());
    }

    @Test
    void testGetUserPreferences_MultipleUsers() {
        NotificationPreferences prefs2 = new NotificationPreferences("USER-002");
        
        when(preferencesRepository.findByUserId("USER-001")).thenReturn(Optional.of(testPreferences));
        when(preferencesRepository.findByUserId("USER-002")).thenReturn(Optional.of(prefs2));

        NotificationPreferences result1 = preferencesService.getUserPreferences("USER-001");
        NotificationPreferences result2 = preferencesService.getUserPreferences("USER-002");

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getUserId(), result2.getUserId());
    }

    @Test
    void testUpdatePreferences_ModifySettings() {
        testPreferences.setEmailEnabled(false);
        testPreferences.setSmsEnabled(true);
        
        when(preferencesRepository.save(any(NotificationPreferences.class))).thenReturn(testPreferences);

        NotificationPreferences result = preferencesService.updatePreferences(testPreferences);

        assertNotNull(result);
        assertFalse(result.isEmailEnabled());
        assertTrue(result.isSmsEnabled());
    }

    @Test
    void testShouldSendNotification_AllChannelsDisabled() {
        testPreferences.setEmailEnabled(false);
        testPreferences.setSmsEnabled(false);
        testPreferences.setPushEnabled(false);
        
        when(preferencesRepository.findByUserId("USER-001")).thenReturn(Optional.of(testPreferences));

        boolean emailResult = preferencesService.shouldSendNotification("USER-001", "invoice", "email");
        boolean smsResult = preferencesService.shouldSendNotification("USER-001", "invoice", "sms");
        boolean pushResult = preferencesService.shouldSendNotification("USER-001", "invoice", "push");

        assertFalse(emailResult);
        assertFalse(smsResult);
        assertFalse(pushResult);
    }
}
