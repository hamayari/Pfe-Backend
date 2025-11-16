package com.example.demo.service;

import com.example.demo.dto.NotificationHistoryDTO;
import com.example.demo.dto.NotificationSettingsDTO;
import com.example.demo.model.Invoice;
import com.example.demo.model.NotificationLog;
import com.example.demo.model.User;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.NotificationLogRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour NotificationService
 * Couverture: 80%+
 * Bonnes pratiques: AAA (Arrange-Act-Assert), Mockito, AssertJ
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService - Tests Unitaires")
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationLog testNotification;
    private User testUser;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        // Arrange - Données de test
        testNotification = new NotificationLog();
        testNotification.setId("notif123");
        testNotification.setRecipientId("user123");
        testNotification.setMessage("Test notification");
        testNotification.setStatus("UNREAD");
        testNotification.setType("INFO");

        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testInvoice = new Invoice();
        testInvoice.setId("invoice123");
        testInvoice.setInvoiceNumber("INV-2024-001");
        testInvoice.setCreatedBy("user123");
    }

    // ==================== GET NOTIFICATIONS FOR USER ====================

    @Test
    @DisplayName("getNotificationsForUser - Devrait retourner les notifications de l'utilisateur")
    void getNotificationsForUser_ShouldReturnUserNotifications() {
        // Arrange
        List<NotificationLog> notifications = Arrays.asList(testNotification, new NotificationLog());
        when(notificationLogRepository.findByRecipientId("user123")).thenReturn(notifications);

        // Act
        List<NotificationLog> result = notificationService.getNotificationsForUser("user123");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(testNotification);
        verify(notificationLogRepository).findByRecipientId("user123");
    }

    @Test
    @DisplayName("getNotificationsForUser - Devrait retourner liste vide si aucune notification")
    void getNotificationsForUser_ShouldReturnEmptyList_WhenNoNotifications() {
        // Arrange
        when(notificationLogRepository.findByRecipientId("user123")).thenReturn(Collections.emptyList());

        // Act
        List<NotificationLog> result = notificationService.getNotificationsForUser("user123");

        // Assert
        assertThat(result).isEmpty();
        verify(notificationLogRepository).findByRecipientId("user123");
    }

    // ==================== SEND NOTIFICATION TO USER ====================

    @Test
    @DisplayName("sendNotificationToUser - Devrait envoyer la notification via WebSocket")
    void sendNotificationToUser_ShouldSendViaWebSocket() {
        // Arrange
        doNothing().when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));

        // Act
        notificationService.sendNotificationToUser(testNotification);

        // Assert
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), eq(testNotification));
    }

    // ==================== CREATE AND SEND NOTIFICATION ====================

    @Test
    @DisplayName("createAndSendNotification - Devrait créer et envoyer la notification")
    void createAndSendNotification_ShouldCreateAndSend() {
        // Arrange
        when(notificationLogRepository.save(testNotification)).thenReturn(testNotification);
        doNothing().when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));

        // Act
        notificationService.createAndSendNotification(testNotification);

        // Assert
        verify(notificationLogRepository).save(testNotification);
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), eq(testNotification));
    }

    // ==================== MARK AS READ ====================

    @Test
    @DisplayName("markAsRead - Devrait marquer la notification comme lue")
    void markAsRead_ShouldMarkNotificationAsRead() {
        // Arrange
        when(notificationLogRepository.findById("notif123")).thenReturn(Optional.of(testNotification));
        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(testNotification);
        doNothing().when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));

        // Act
        notificationService.markAsRead("notif123");

        // Assert
        verify(notificationLogRepository).findById("notif123");
        verify(notificationLogRepository).save(any(NotificationLog.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(NotificationLog.class));
    }

    @Test
    @DisplayName("markAsRead - Ne devrait rien faire si notification inexistante")
    void markAsRead_ShouldDoNothing_WhenNotificationNotFound() {
        // Arrange
        when(notificationLogRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act
        notificationService.markAsRead("invalid");

        // Assert
        verify(notificationLogRepository).findById("invalid");
        verify(notificationLogRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }

    // ==================== GET UNREAD COUNT ====================

    @Test
    @DisplayName("getUnreadCount - Devrait retourner le nombre de notifications non lues")
    void getUnreadCount_ShouldReturnUnreadCount() {
        // Arrange
        when(notificationLogRepository.countByRecipientIdAndStatusNot("user123", "READ")).thenReturn(5L);

        // Act
        long result = notificationService.getUnreadCount("user123");

        // Assert
        assertThat(result).isEqualTo(5L);
        verify(notificationLogRepository).countByRecipientIdAndStatusNot("user123", "READ");
    }

    @Test
    @DisplayName("getUnreadCount - Devrait retourner 0 si aucune notification non lue")
    void getUnreadCount_ShouldReturnZero_WhenNoUnreadNotifications() {
        // Arrange
        when(notificationLogRepository.countByRecipientIdAndStatusNot("user123", "READ")).thenReturn(0L);

        // Act
        long result = notificationService.getUnreadCount("user123");

        // Assert
        assertThat(result).isZero();
        verify(notificationLogRepository).countByRecipientIdAndStatusNot("user123", "READ");
    }

    // ==================== MARK AS READ BULK ====================

    @Test
    @DisplayName("markAsReadBulk - Devrait marquer plusieurs notifications comme lues")
    void markAsReadBulk_ShouldMarkMultipleNotificationsAsRead() {
        // Arrange
        List<String> ids = Arrays.asList("notif1", "notif2", "notif3");
        NotificationLog notif1 = new NotificationLog();
        notif1.setId("notif1");
        NotificationLog notif2 = new NotificationLog();
        notif2.setId("notif2");
        NotificationLog notif3 = new NotificationLog();
        notif3.setId("notif3");
        
        List<NotificationLog> notifications = Arrays.asList(notif1, notif2, notif3);
        when(notificationLogRepository.findByIdInAndRecipientId(ids, "user123")).thenReturn(notifications);
        when(notificationLogRepository.saveAll(anyList())).thenReturn(notifications);

        // Act
        int result = notificationService.markAsReadBulk("user123", ids);

        // Assert
        assertThat(result).isEqualTo(3);
        verify(notificationLogRepository).findByIdInAndRecipientId(ids, "user123");
        verify(notificationLogRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("markAsReadBulk - Devrait retourner 0 si aucune notification trouvée")
    void markAsReadBulk_ShouldReturnZero_WhenNoNotificationsFound() {
        // Arrange
        List<String> ids = Arrays.asList("invalid1", "invalid2");
        when(notificationLogRepository.findByIdInAndRecipientId(ids, "user123"))
                .thenReturn(Collections.emptyList());
        when(notificationLogRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        int result = notificationService.markAsReadBulk("user123", ids);

        // Assert
        assertThat(result).isZero();
        verify(notificationLogRepository).saveAll(anyList());
    }

    // ==================== LEGACY METHODS ====================

    @Test
    @DisplayName("getNotificationSettings - Devrait retourner un DTO vide")
    void getNotificationSettings_ShouldReturnEmptyDTO() {
        // Act
        NotificationSettingsDTO result = notificationService.getNotificationSettings("user123");

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("updateNotificationSettings - Devrait retourner le DTO fourni")
    void updateNotificationSettings_ShouldReturnProvidedDTO() {
        // Arrange
        NotificationSettingsDTO settings = new NotificationSettingsDTO();

        // Act
        NotificationSettingsDTO result = notificationService.updateNotificationSettings("user123", settings);

        // Assert
        assertThat(result).isEqualTo(settings);
    }

    @Test
    @DisplayName("getNotificationHistory - Devrait retourner une liste vide")
    void getNotificationHistory_ShouldReturnEmptyList() {
        // Act
        List<NotificationHistoryDTO> result = notificationService.getNotificationHistory("user123");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("sendManualReminder - Devrait s'exécuter sans erreur")
    void sendManualReminder_ShouldExecuteWithoutError() {
        // Act & Assert
        assertThatCode(() -> notificationService.sendManualReminder("user123", "invoice123", "EMAIL"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("scheduleAutomaticReminders - Devrait s'exécuter sans erreur")
    void scheduleAutomaticReminders_ShouldExecuteWithoutError() {
        // Act & Assert
        assertThatCode(() -> notificationService.scheduleAutomaticReminders("user123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("testNotification - Devrait s'exécuter sans erreur")
    void testNotification_ShouldExecuteWithoutError() {
        // Act & Assert
        assertThatCode(() -> notificationService.testNotification("user123", "EMAIL", "test@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendPaymentReminderNotification - Devrait s'exécuter sans erreur")
    void sendPaymentReminderNotification_ShouldExecuteWithoutError() {
        // Act & Assert
        assertThatCode(() -> notificationService.sendPaymentReminderNotification(testInvoice))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("notifyPaymentValidated - Devrait s'exécuter sans erreur (deprecated)")
    void notifyPaymentValidated_ShouldExecuteWithoutError() {
        // Act & Assert
        assertThatCode(() -> notificationService.notifyPaymentValidated("invoice123", "receipt.pdf"))
                .doesNotThrowAnyException();
    }

    // ==================== NOTIFY COMMERCIAL FOR MANUAL VALIDATION ====================

    @Test
    @DisplayName("notifyCommercialForManualValidation - Devrait envoyer notification et email")
    void notifyCommercialForManualValidation_ShouldSendNotificationAndEmail() {
        // Arrange
        doNothing().when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.notifyCommercialForManualValidation(
                "invoice123", "commercial123", 1000.0, "BANK_TRANSFER"
        );

        // Assert
        verify(messagingTemplate).convertAndSend(
                eq("/topic/commercial/commercial123"), 
                any(NotificationService.NotificationPayload.class)
        );
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("notifyCommercialForManualValidation - Devrait gérer les erreurs d'envoi d'email")
    void notifyCommercialForManualValidation_ShouldHandleEmailErrors() {
        // Arrange
        doNothing().when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        doThrow(new RuntimeException("Email error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert - Ne devrait pas lancer d'exception
        assertThatCode(() -> notificationService.notifyCommercialForManualValidation(
                "invoice123", "commercial123", 1000.0, "BANK_TRANSFER"
        )).doesNotThrowAnyException();

        verify(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
    }

    // ==================== NOTIFY PAYMENT PENDING REVIEW ====================

    @Test
    @DisplayName("notifyPaymentPendingReview - Devrait envoyer notification WebSocket")
    void notifyPaymentPendingReview_ShouldSendWebSocketNotification() {
        // Arrange
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        doNothing().when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.notifyPaymentPendingReview("invoice123", "proof.pdf");

        // Assert
        verify(messagingTemplate).convertAndSend(
                eq("/topic/notifications"), 
                any(NotificationService.NotificationPayload.class)
        );
    }

    @Test
    @DisplayName("notifyPaymentPendingReview - Devrait gérer l'absence d'email commercial")
    void notifyPaymentPendingReview_ShouldHandleMissingCommercialEmail() {
        // Arrange
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.empty());
        doNothing().when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));

        // Act & Assert - Ne devrait pas lancer d'exception
        assertThatCode(() -> notificationService.notifyPaymentPendingReview("invoice123", "proof.pdf"))
                .doesNotThrowAnyException();

        verify(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // ==================== NOTIFICATION PAYLOAD ====================

    @Test
    @DisplayName("NotificationPayload - Devrait gérer les getters/setters correctement")
    void notificationPayload_ShouldHandleGettersSetters() {
        // Arrange & Act
        NotificationService.NotificationPayload payload = new NotificationService.NotificationPayload();
        payload.setType("TEST");
        payload.setInvoiceId("invoice123");
        payload.setMessage("Test message");
        payload.setLink("/test/link");

        // Assert
        assertThat(payload.getType()).isEqualTo("TEST");
        assertThat(payload.getInvoiceId()).isEqualTo("invoice123");
        assertThat(payload.getMessage()).isEqualTo("Test message");
        assertThat(payload.getLink()).isEqualTo("/test/link");
    }
}
