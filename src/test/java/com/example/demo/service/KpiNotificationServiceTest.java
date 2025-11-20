package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.enums.ERole;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpiNotificationServiceTest {

    @Mock
    private KpiAlertRepository alertRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private RealTimeNotificationService realTimeNotificationService;

    @InjectMocks
    private KpiNotificationService service;

    private KpiAlert mockAlert;
    private List<User> mockUsers;

    @BeforeEach
    void setUp() {
        mockAlert = createMockAlert();
        mockUsers = createMockUsers();
    }

    @Test
    void testSendAlertNotifications_Success() {
        // Given
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(mockUsers);
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(alertRepository, times(1)).save(any(KpiAlert.class));
        verify(messagingTemplate, atLeastOnce()).convertAndSendToUser(
            anyString(), anyString(), any()
        );
    }

    @Test
    void testSendAlertNotifications_AlreadySent() {
        // Given
        mockAlert.setNotificationSent(true);
        List<KpiAlert> alerts = Arrays.asList(mockAlert);

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(alertRepository, never()).save(any(KpiAlert.class));
    }

    @Test
    void testSendAlertNotifications_NoRecipients() {
        // Given
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(Collections.emptyList());

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(alertRepository, never()).save(any(KpiAlert.class));
    }

    @Test
    void testSendAlertNotifications_HighSeverity() {
        // Given
        mockAlert.setSeverity("HIGH");
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER))
            .thenReturn(mockUsers);
        when(userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER))
            .thenReturn(mockUsers);
        when(userRepository.findByRoles_Name(ERole.ROLE_ADMIN))
            .thenReturn(mockUsers);
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(userRepository, times(1)).findByRoles_Name(ERole.ROLE_ADMIN);
        verify(alertRepository, times(1)).save(any(KpiAlert.class));
    }

    @Test
    void testSendAlertNotifications_MediumSeverity() {
        // Given
        mockAlert.setSeverity("MEDIUM");
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(mockUsers);
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(alertRepository, times(1)).save(any(KpiAlert.class));
    }

    @Test
    void testSendWeeklyReport_Success() {
        // Given
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER))
            .thenReturn(mockUsers);

        // When
        service.sendWeeklyReport(alerts);

        // Then
        verify(emailService, times(mockUsers.size())).sendEmail(
            anyString(), anyString(), anyString()
        );
    }

    @Test
    void testSendWeeklyReport_NoDecideurs() {
        // Given
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER))
            .thenReturn(Collections.emptyList());

        // When
        service.sendWeeklyReport(alerts);

        // Then
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendMonthlyReport_Success() {
        // Given
        List<KpiAlert> alerts = Arrays.asList(mockAlert, createMockAlert());
        when(userRepository.findByRoles_Name(ERole.ROLE_DECISION_MAKER))
            .thenReturn(mockUsers);

        // When
        service.sendMonthlyReport(alerts);

        // Then
        verify(emailService, times(mockUsers.size())).sendEmail(
            anyString(), contains("Mensuel"), anyString()
        );
    }

    @Test
    void testSendUrgentAlerts_Success() {
        // Given
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(mockUsers);
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendUrgentAlerts(alerts);

        // Then
        verify(alertRepository, times(1)).save(any(KpiAlert.class));
    }

    @Test
    void testSendUrgentAlerts_AlreadySent() {
        // Given
        mockAlert.setNotificationSent(true);
        List<KpiAlert> alerts = Arrays.asList(mockAlert);

        // When
        service.sendUrgentAlerts(alerts);

        // Then
        verify(alertRepository, never()).save(any(KpiAlert.class));
    }

    @Test
    void testSendAlertNotifications_WithRealTimeNotification() {
        // Given
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(mockUsers);
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(realTimeNotificationService, times(mockUsers.size()))
            .createNotification(any(NotificationDTO.class));
    }

    @Test
    void testSendAlertNotifications_WithSms() {
        // Given
        mockAlert.setSeverity("HIGH");
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        
        User userWithPhone = createUserWithPhone();
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(Arrays.asList(userWithPhone));
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(smsService, atLeastOnce()).sendSmsWithTemplate(
            anyString(), anyString(), anyMap()
        );
    }

    @Test
    void testSendAlertNotifications_MultipleAlerts() {
        // Given
        KpiAlert alert1 = createMockAlert();
        KpiAlert alert2 = createMockAlert();
        alert2.setId("alert2");
        alert2.setKpiName("REVENUE_DROP");
        
        List<KpiAlert> alerts = Arrays.asList(alert1, alert2);
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(mockUsers);
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(alertRepository, times(2)).save(any(KpiAlert.class));
    }

    @Test
    void testSendAlertNotifications_WithDimension() {
        // Given
        mockAlert.setDimension("Region");
        mockAlert.setDimensionValue("North");
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(mockUsers);
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(alertRepository, times(1)).save(argThat(alert -> 
            alert.getDimension() != null && alert.getDimensionValue() != null
        ));
    }

    @Test
    void testSendAlertNotifications_WithRecommendation() {
        // Given
        mockAlert.setRecommendation("Contactez immédiatement le client");
        List<KpiAlert> alerts = Arrays.asList(mockAlert);
        
        when(userRepository.findByRoles_Name(any(ERole.class)))
            .thenReturn(mockUsers);
        when(alertRepository.save(any(KpiAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.sendAlertNotifications(alerts);

        // Then
        verify(alertRepository, times(1)).save(argThat(alert -> 
            alert.getRecommendation() != null
        ));
    }

    // Helper methods
    private KpiAlert createMockAlert() {
        KpiAlert alert = new KpiAlert();
        alert.setId("alert123");
        alert.setKpiName("INVOICE_OVERDUE");
        alert.setCurrentValue(5.0);
        alert.setStatus("ANORMAL");
        alert.setSeverity("MEDIUM");
        alert.setMessage("5 factures en retard détectées");
        alert.setDetectedAt(LocalDateTime.now());
        alert.setNotificationSent(false);
        return alert;
    }

    private List<User> createMockUsers() {
        List<User> users = new ArrayList<>();
        
        User user1 = new User();
        user1.setId("user1");
        user1.setUsername("decideur1");
        user1.setEmail("decideur1@example.com");
        users.add(user1);
        
        User user2 = new User();
        user2.setId("user2");
        user2.setUsername("pm1");
        user2.setEmail("pm1@example.com");
        users.add(user2);
        
        return users;
    }

    private User createUserWithPhone() {
        User user = new User();
        user.setId("user3");
        user.setUsername("decideur2");
        user.setEmail("decideur2@example.com");
        user.setPhoneNumber("+21612345678");
        return user;
    }
}
