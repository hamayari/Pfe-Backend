package com.example.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.mockito.Mockito;

/**
 * Configuration de test pour mocker tous les services externes
 * Garantit que les tests n'ont AUCUNE dépendance externe
 */
@TestConfiguration
public class TestConfig {

    /**
     * Mock JavaMailSender pour éviter l'envoi de vrais emails
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
    
    // Ajoutez d'autres mocks ici si nécessaire
    // Par exemple: TwilioRestClient, StripeClient, etc.
}
