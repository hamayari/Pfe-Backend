package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.dto.NotificationStatsDTO;
import com.example.demo.model.Notification;
import com.example.demo.model.NotificationPreferences;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.NotificationPreferencesRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealTimeNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private RealTimeNotificationService notificationService;

    private Notification testNotification;
    private NotificationDTO testNotificationDTO;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId("NOTIF-001");
        testNotification.setUserId("user1");
        testNotification.setType("INFO");
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("Test message");
        testNotification.setPriority("medium");
        testNotification.setCategory("general");
        testNotification.setTimestamp(LocalDateTime.now());
        testNotification.setRead(false);
        testNotification.setAcknowledged(false);

        testNotificationDTO = new NotificationDTO();
        testNotificationDTO.setUserId("user1");
        testNotificationDTO.setType("INFO");
        testNotificationDTO.setTitle("Test Notification");
        testNotificationDTO.setMessage("Test message");
        testNotificationDTO.setPriority("medium");
        testNotificationDTO.setCategory("general");
    }

    @Test
    void testCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        NotificationDTO result = notificationService.createNotification(testNotificationDTO);

        assertNotNull(result);
        assertEquals("Test Notification", result.getTitle());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testGetUserNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdOrderByTimestampDesc("user1")).thenReturn(notifications);

        List<NotificationDTO> result = notificationService.getUserNotifications("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Notification", result.get(0).getTitle());
        verify(notificationRepository).findByUserIdOrderByTimestampDesc("user1");
    }

    @Test
    void testMarkAsRead() {
        when(notificationRepository.findById("NOTIF-001")).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAsRead("NOTIF-001");

        assertTrue(testNotification.isRead());
        assertNotNull(testNotification.getReadAt());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void testMarkAsAcknowledged() {
        when(notificationRepository.findById("NOTIF-001")).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAsAcknowledged("NOTIF-001");

        assertTrue(testNotification.isAcknowledged());
        assertNotNull(testNotification.getAcknowledgedAt());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void testDeleteNotification() {
        doNothing().when(notificationRepository).deleteById("NOTIF-001");

        notificationService.deleteNotification("NOTIF-001");

        verify(notificationRepository).deleteById("NOTIF-001");
    }

    @Test
    void testDeleteAllUserNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdOrderByTimestampDesc("user1")).thenReturn(notifications);
        doNothing().when(notificationRepository).deleteAll(notifications);

        notificationService.deleteAllUserNotifications("user1");

        verify(notificationRepository).deleteAll(notifications);
    }

    @Test
    void testGetNotificationStats() {
        Notification unreadNotif = new Notification();
        unreadNotif.setRead(false);
        unreadNotif.setAcknowledged(false);
        unreadNotif.setCategory("alert");
        unreadNotif.setPriority("high");

        List<Notification> notifications = Arrays.asList(testNotification, unreadNotif);
        when(notificationRepository.findByUserIdOrderByTimestampDesc("user1")).thenReturn(notifications);

        NotificationStatsDTO stats = notificationService.getNotificationStats("user1");

        assertNotNull(stats);
        assertEquals(2, stats.getTotal());
        assertEquals(2, stats.getUnread());
        verify(notificationRepository).findByUserIdOrderByTimestampDesc("user1");
    }

    @Test
    void testSendNotification() {
        NotificationPreferences prefs = new NotificationPreferences();
        prefs.setUserId("user1");
        prefs.setEmailEnabled(false);
        prefs.setSmsEnabled(false);

        when(preferencesRepository.findByUserId("user1")).thenReturn(Optional.of(prefs));
        doNothing().when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        notificationService.sendNotification(testNotification);

        verify(messagingTemplate).convertAndSendToUser(eq("user1"), eq("/queue/notifications"), any());
    }

    @Test
    void testSendNotificationWithEmailEnabled() {
        NotificationPreferences prefs = new NotificationPreferences();
        prefs.setUserId("user1");
        prefs.setEmailEnabled(true);
        prefs.setSmsEnabled(false);

        when(preferencesRepository.findByUserId("user1")).thenReturn(Optional.of(prefs));
        doNothing().when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        notificationService.sendNotification(testNotification);

        verify(messagingTemplate).convertAndSendToUser(eq("user1"), eq("/queue/notifications"), any());
        verify(preferencesRepository).findByUserId("user1");
    }
}
