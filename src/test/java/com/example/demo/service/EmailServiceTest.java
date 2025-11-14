package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmailService
 * Utilise Mockito pour mocker JavaMailSender (pas de vrai envoi d'email)
 */
@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    public void testSendEmail() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Email";
        String body = "<h1>Test</h1><p>Ceci est un email de test</p>";
        
        // Mock: ne fait rien quand on envoie un email
        doNothing().when(mailSender).send(any(javax.mail.internet.MimeMessage.class));
        
        // Act & Assert
        // Le test passe si aucune exception n'est levée
        emailService.sendEmail(to, subject, body);
        
        // Vérifier que send() a été appelé
        verify(mailSender, times(1)).send(any(javax.mail.internet.MimeMessage.class));
    }
}
