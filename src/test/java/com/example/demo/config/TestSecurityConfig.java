package com.example.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les tests
 * Permet l'accès aux endpoints d'authentification sans authentification préalable
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
