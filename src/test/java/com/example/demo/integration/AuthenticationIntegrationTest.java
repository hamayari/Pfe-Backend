package com.example.demo.integration;

import com.example.demo.model.User;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.SignupRequest;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests d'intégration pour l'authentification
 * Teste le flux complet d'inscription et de connexion
 * Utilise MongoDB embarqué (Flapdoodle) - Aucune dépendance externe
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données avant chaque test
        userRepository.deleteAll();
    }

    // ==================== Tests d'inscription ====================

    @Test
    @DisplayName("Integration: Complete signup flow")
    void testCompleteSignupFlow() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("integrationuser");
        signupRequest.setEmail("integration@test.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("ROLE_COMMERCIAL"));

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk());

        // Vérifier que l'utilisateur existe dans la base
        User savedUser = userRepository.findByUsername("integrationuser").orElse(null);
        assert savedUser != null;
        assert savedUser.getEmail().equals("integration@test.com");
    }

    @Test
    @DisplayName("Integration: Signup with duplicate username should fail")
    void testSignupDuplicateUsername() throws Exception {
        // Given - Créer un premier utilisateur
        SignupRequest firstRequest = new SignupRequest();
        firstRequest.setUsername("duplicateuser");
        firstRequest.setEmail("first@test.com");
        firstRequest.setPassword("password123");
        firstRequest.setRoles(Set.of("ROLE_COMMERCIAL"));

        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isOk());

        // When - Tenter de créer un utilisateur avec le même username
        SignupRequest duplicateRequest = new SignupRequest();
        duplicateRequest.setUsername("duplicateuser");
        duplicateRequest.setEmail("second@test.com");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setRoles(Set.of("ROLE_COMMERCIAL"));

        // Then
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
            .andExpect(status().is4xxClientError());
    }

    // ==================== Tests de connexion ====================

    @Test
    @DisplayName("Integration: Complete login flow after signup")
    void testCompleteLoginFlowAfterSignup() throws Exception {
        // Given - Inscription
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("loginuser");
        signupRequest.setEmail("login@test.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("ROLE_COMMERCIAL"));

        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk());

        // When - Connexion
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/authenticate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.username").value("loginuser"))
            .andExpect(jsonPath("$.email").value("login@test.com"))
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Integration: Login with wrong password should fail")
    void testLoginWithWrongPassword() throws Exception {
        // Given - Inscription
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("secureuser");
        signupRequest.setEmail("secure@test.com");
        signupRequest.setPassword("correctpassword");
        signupRequest.setRoles(Set.of("ROLE_COMMERCIAL"));

        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk());

        // When - Connexion avec mauvais mot de passe
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("secureuser");
        loginRequest.setPassword("wrongpassword");

        // Then
        mockMvc.perform(post("/api/auth/authenticate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Integration: Login with non-existent user should fail")
    void testLoginNonExistentUser() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/authenticate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().is4xxClientError());
    }

    // ==================== Tests de validation ====================

    @Test
    @DisplayName("Integration: Signup with invalid email should fail")
    void testSignupInvalidEmail() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("validuser");
        signupRequest.setEmail("invalid-email");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("ROLE_COMMERCIAL"));

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Integration: Signup with short password should fail")
    void testSignupShortPassword() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("validuser");
        signupRequest.setEmail("valid@test.com");
        signupRequest.setPassword("123");
        signupRequest.setRoles(Set.of("ROLE_COMMERCIAL"));

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isBadRequest());
    }

    // ==================== Tests de sécurité ====================

    @Test
    @DisplayName("Integration: Signup without CSRF token should fail")
    void testSignupWithoutCsrf() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("csrfuser");
        signupRequest.setEmail("csrf@test.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("ROLE_COMMERCIAL"));

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isForbidden());
    }

    // ==================== Tests de flux complets ====================

    @Test
    @DisplayName("Integration: Multiple users can signup and login independently")
    void testMultipleUsersSignupAndLogin() throws Exception {
        // User 1
        SignupRequest user1Signup = new SignupRequest();
        user1Signup.setUsername("user1");
        user1Signup.setEmail("user1@test.com");
        user1Signup.setPassword("password1");
        user1Signup.setRoles(Set.of("ROLE_COMMERCIAL"));

        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Signup)))
            .andExpect(status().isOk());

        // User 2
        SignupRequest user2Signup = new SignupRequest();
        user2Signup.setUsername("user2");
        user2Signup.setEmail("user2@test.com");
        user2Signup.setPassword("password2");
        user2Signup.setRoles(Set.of("ROLE_ADMIN"));

        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Signup)))
            .andExpect(status().isOk());

        // Login User 1
        LoginRequest user1Login = new LoginRequest();
        user1Login.setUsername("user1");
        user1Login.setPassword("password1");

        mockMvc.perform(post("/api/auth/authenticate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("user1"));

        // Login User 2
        LoginRequest user2Login = new LoginRequest();
        user2Login.setUsername("user2");
        user2Login.setPassword("password2");

        mockMvc.perform(post("/api/auth/authenticate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("user2"));
    }
}
