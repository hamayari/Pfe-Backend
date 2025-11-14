package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.SignupRequest;
import com.example.demo.payload.response.JwtResponse;
import com.example.demo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires PURS pour AuthController
 * ✅ Pas de @SpringBootTest
 * ✅ Pas de contexte Spring
 * ✅ Uniquement Mockito
 * ✅ Tests isolés et rapides
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice() // Pour gérer les exceptions
            .build();
    }

    @Test
    @DisplayName("POST /api/auth/signup - Success")
    void testSignup_Success() throws Exception {
        // Given
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User mockUser = new User();
        mockUser.setId("123");
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");

        when(authService.registerUser(any(SignupRequest.class))).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(authService, times(1)).registerUser(any(SignupRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/authenticate - Success")
    void testAuthenticate_Success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        List<String> roles = Arrays.asList("ROLE_USER");
        JwtResponse response = new JwtResponse(
            "mock-jwt-token",
            "123",
            "testuser",
            "test@example.com",
            roles,
            false
        );

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(authService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/authenticate - Service called correctly")
    void testAuthenticate_ServiceCalled() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        List<String> roles = Arrays.asList("ROLE_USER");
        JwtResponse response = new JwtResponse(
            "mock-jwt-token",
            "123",
            "testuser",
            "test@example.com",
            roles,
            false
        );

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

        // When
        mockMvc.perform(post("/api/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        verify(authService, times(1)).authenticateUser(argThat(req -> 
            req.getUsername().equals("testuser") && 
            req.getPassword().equals("password123")
        ));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - Success")
    void testForgotPassword_Success() throws Exception {
        // Given
        String email = "test@example.com";
        doNothing().when(authService).initiatePasswordReset(email);

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                .param("email", email))
            .andExpect(status().isOk())
            .andExpect(content().string("Password reset email sent"));

        verify(authService, times(1)).initiatePasswordReset(email);
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Success")
    void testResetPassword_Success() throws Exception {
        // Given
        String token = "reset-token";
        String newPassword = "newPassword123";
        doNothing().when(authService).completePasswordReset(token, newPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .param("token", token)
                .param("newPassword", newPassword))
            .andExpect(status().isOk())
            .andExpect(content().string("Password has been reset successfully"));

        verify(authService, times(1)).completePasswordReset(token, newPassword);
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Invalid Token")
    void testResetPassword_InvalidToken() throws Exception {
        // Given
        String token = "invalid-token";
        String newPassword = "newPassword123";
        doThrow(new RuntimeException("Invalid token"))
            .when(authService).completePasswordReset(token, newPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .param("token", token)
                .param("newPassword", newPassword))
            .andExpect(status().isBadRequest());

        verify(authService, times(1)).completePasswordReset(token, newPassword);
    }
}
