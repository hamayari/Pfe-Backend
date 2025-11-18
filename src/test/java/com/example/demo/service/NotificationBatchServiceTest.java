package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationBatchServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RealTimeNotificationService realTimeNotificationService;

    @InjectMocks
    private NotificationBatchService batchService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId("NOTIF-001");
        testNotification.setUserId("USER-001");
        testNotification.setStatus("PENDING");
        testNotification.setTimestamp(LocalDateTime.now());
        testNotification.setRetryCount(0);
    }

    @Test
    void testProcessPendingNotifications_EmptyList() {
        Page<Notification> emptyPage = new PageImpl<>(new ArrayList<>());
        when(notificationRepository.findByStatusOrderByTimestampAsc(anyString(), any(PageRequest.class)))
            .thenReturn(emptyPage);

        CompletableFuture<Void> result = batchService.processPendingNotifications();

        assertNotNull(result);
        verify(notificationRepository).findByStatusOrderByTimestampAsc(anyString(), any(PageRequest.class));
    }

    @Test
    void testProcessPendingNotifications_WithNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> page = new PageImpl<>(notifications);
        
        when(notificationRepository.findByStatusOrderByTimestampAsc(anyString(), any(PageRequest.class)))
            .thenReturn(page);
        doNothing().when(realTimeNotificationService).sendNotification(any(Notification.class));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        CompletableFuture<Void> result = batchService.processPendingNotifications();

        assertNotNull(result);
        verify(realTimeNotificationService, atLeastOnce()).sendNotification(any(Notification.class));
    }

    @Test
    void testProcessPendingNotifications_WithError() {
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> page = new PageImpl<>(notifications);
        
        when(notificationRepository.findByStatusOrderByTimestampAsc(anyString(), any(PageRequest.class)))
            .thenReturn(page);
        doThrow(new RuntimeException("Send error")).when(realTimeNotificationService)
            .sendNotification(any(Notification.class));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        CompletableFuture<Void> result = batchService.processPendingNotifications();

        assertNotNull(result);
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void testProcessPendingNotifications_MaxRetries() {
        testNotification.setRetryCount(3);
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> page = new PageImpl<>(notifications);
        
        when(notificationRepository.findByStatusOrderByTimestampAsc(anyString(), any(PageRequest.class)))
            .thenReturn(page);
        doThrow(new RuntimeException("Send error")).when(realTimeNotificationService)
            .sendNotification(any(Notification.class));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        CompletableFuture<Void> result = batchService.processPendingNotifications();

        assertNotNull(result);
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void testCleanupBatchStatus() {
        assertDoesNotThrow(() -> batchService.cleanupBatchStatus());
    }

    @Test
    void testProcessPendingNotifications_MultiplePages() {
        List<Notification> page1 = Arrays.asList(testNotification);
        List<Notification> page2 = new ArrayList<>();
        
        Page<Notification> firstPage = new PageImpl<>(page1, PageRequest.of(0, 100), 101);
        Page<Notification> secondPage = new PageImpl<>(page2, PageRequest.of(1, 100), 101);
        
        when(notificationRepository.findByStatusOrderByTimestampAsc(anyString(), any(PageRequest.class)))
            .thenReturn(firstPage)
            .thenReturn(secondPage);
        doNothing().when(realTimeNotificationService).sendNotification(any(Notification.class));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        CompletableFuture<Void> result = batchService.processPendingNotifications();

        assertNotNull(result);
    }

    @Test
    void testProcessPendingNotifications_MultipleUsers() {
        Notification notif1 = new Notification();
        notif1.setId("NOTIF-001");
        notif1.setUserId("USER-001");
        notif1.setStatus("PENDING");
        
        Notification notif2 = new Notification();
        notif2.setId("NOTIF-002");
        notif2.setUserId("USER-002");
        notif2.setStatus("PENDING");
        
        List<Notification> notifications = Arrays.asList(notif1, notif2);
        Page<Notification> page = new PageImpl<>(notifications);
        
        when(notificationRepository.findByStatusOrderByTimestampAsc(anyString(), any(PageRequest.class)))
            .thenReturn(page);
        doNothing().when(realTimeNotificationService).sendNotification(any(Notification.class));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notif1);

        CompletableFuture<Void> result = batchService.processPendingNotifications();

        assertNotNull(result);
        verify(realTimeNotificationService, times(2)).sendNotification(any(Notification.class));
    }
}
