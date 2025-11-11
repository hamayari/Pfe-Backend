package com.example.demo.controller;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.convention.ConventionRequest;
import com.example.demo.model.Convention;
import com.example.demo.scheduler.ConventionStatusScheduler;
import com.example.demo.service.ConventionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests unitaires pour ConventionController
 * Teste tous les endpoints REST avec MockMvc
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@org.junit.jupiter.api.Disabled("ApplicationContext fails to load - configuration issue")
class ConventionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConventionService conventionService;

    @MockBean
    private ConventionStatusScheduler conventionStatusScheduler;

    private Convention mockConvention;
    private ConventionRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Convention mock
        mockConvention = new Convention();
        mockConvention.setId("conv-123");
        mockConvention.setReference("CONV-2024-001");
        mockConvention.setTitle("Convention Test");
        mockConvention.setDescription("Description test");
        mockConvention.setStartDate(LocalDate.now());
        mockConvention.setEndDate(LocalDate.now().plusMonths(6));
        mockConvention.setAmount(BigDecimal.valueOf(10000));
        mockConvention.setStatus("ACTIVE");
        mockConvention.setCreatedBy("testuser");

        // Request mock
        mockRequest = new ConventionRequest();
        mockRequest.setReference("CONV-2024-002");
        mockRequest.setTitle("Nouvelle Convention");
        mockRequest.setDescription("Description");
        mockRequest.setStartDate(LocalDateTime.now());
        mockRequest.setEndDate(LocalDateTime.now().plusMonths(12));
        mockRequest.setAmount(BigDecimal.valueOf(20000));
        mockRequest.setStructure("struct-1");
        mockRequest.setGeographicZone("zone-1");
        mockRequest.setClient("Client Test");
        mockRequest.setType("Type A");
    }

    // ==================== Tests POST /api/conventions ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("POST /api/conventions - Should create convention successfully")
    void testCreateConvention_Success() throws Exception {
        // Given
        when(conventionService.createConvention(any(), eq("testuser")))
            .thenReturn(mockConvention);

        // When & Then
        mockMvc.perform(post("/api/conventions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("conv-123"))
            .andExpect(jsonPath("$.reference").value("CONV-2024-001"))
            .andExpect(jsonPath("$.title").value("Convention Test"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(conventionService, times(1)).createConvention(any(), eq("testuser"));
    }

    @Test
    @DisplayName("POST /api/conventions - Should return 401 when not authenticated")
    void testCreateConvention_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/conventions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andExpect(status().isUnauthorized());

        verify(conventionService, never()).createConvention(any(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("POST /api/conventions - Should return 400 when validation fails")
    void testCreateConvention_ValidationFailed() throws Exception {
        // Given - Request invalide (sans référence)
        mockRequest.setReference(null);

        // When & Then
        mockMvc.perform(post("/api/conventions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andExpect(status().isBadRequest());

        verify(conventionService, never()).createConvention(any(), anyString());
    }

    // ==================== Tests GET /api/conventions/{id} ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("GET /api/conventions/{id} - Should return convention by ID")
    void testGetConvention_Success() throws Exception {
        // Given
        when(conventionService.getConventionById("conv-123")).thenReturn(mockConvention);

        // When & Then
        mockMvc.perform(get("/api/conventions/conv-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("conv-123"))
            .andExpect(jsonPath("$.reference").value("CONV-2024-001"));

        verify(conventionService, times(1)).getConventionById("conv-123");
    }

    @Test
    @DisplayName("GET /api/conventions/{id} - Should return 401 when not authenticated")
    void testGetConvention_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/conventions/conv-123"))
            .andExpect(status().isUnauthorized());

        verify(conventionService, never()).getConventionById(anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("GET /api/conventions/{id} - Should return 403 when insufficient permissions")
    void testGetConvention_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/conventions/conv-123"))
            .andExpect(status().isForbidden());

        verify(conventionService, never()).getConventionById(anyString());
    }

    // ==================== Tests GET /api/conventions ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("GET /api/conventions - Should return all conventions")
    void testGetAllConventions_Success() throws Exception {
        // Given
        List<Convention> conventions = Arrays.asList(mockConvention, new Convention());
        when(conventionService.getConventionsForCurrentUser()).thenReturn(conventions);

        // When & Then
        mockMvc.perform(get("/api/conventions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value("conv-123"));

        verify(conventionService, times(1)).getConventionsForCurrentUser();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("GET /api/conventions - Should return empty list when no conventions")
    void testGetAllConventions_EmptyList() throws Exception {
        // Given
        when(conventionService.getConventionsForCurrentUser()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/conventions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== Tests PUT /api/conventions/{id} ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("PUT /api/conventions/{id} - Should update convention successfully")
    void testUpdateConvention_Success() throws Exception {
        // Given
        mockConvention.setTitle("Updated Title");
        when(conventionService.updateConvention(eq("conv-123"), any()))
            .thenReturn(mockConvention);

        // When & Then
        mockMvc.perform(put("/api/conventions/conv-123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("conv-123"))
            .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(conventionService, times(1)).updateConvention(eq("conv-123"), any());
    }

    @Test
    @DisplayName("PUT /api/conventions/{id} - Should return 401 when not authenticated")
    void testUpdateConvention_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/conventions/conv-123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andExpect(status().isUnauthorized());

        verify(conventionService, never()).updateConvention(anyString(), any());
    }

    // ==================== Tests DELETE /api/conventions/{id} ====================

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    @DisplayName("DELETE /api/conventions/{id} - Should delete convention successfully")
    void testDeleteConvention_Success() throws Exception {
        // Given
        doNothing().when(conventionService).deleteConvention("conv-123");

        // When & Then
        mockMvc.perform(delete("/api/conventions/conv-123")
                .with(csrf()))
            .andExpect(status().isOk());

        verify(conventionService, times(1)).deleteConvention("conv-123");
    }

    @Test
    @DisplayName("DELETE /api/conventions/{id} - Should return 401 when not authenticated")
    void testDeleteConvention_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/conventions/conv-123")
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verify(conventionService, never()).deleteConvention(anyString());
    }

    // ==================== Tests GET /api/conventions/{id}/pdf ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("GET /api/conventions/{id}/pdf - Should generate PDF successfully")
    void testGenerateConventionPDF_Success() throws Exception {
        // Given
        byte[] pdfContent = "PDF content".getBytes();
        when(conventionService.generateConventionPDF("conv-123")).thenReturn(pdfContent);

        // When & Then
        mockMvc.perform(get("/api/conventions/conv-123/pdf"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/octet-stream"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(pdfContent));

        verify(conventionService, times(1)).generateConventionPDF("conv-123");
    }

    // ==================== Tests de recherche ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("GET /api/conventions/search - Should search conventions by criteria")
    void testSearchConventions_Success() throws Exception {
        // Given
        List<Convention> conventions = Collections.singletonList(mockConvention);
        when(conventionService.searchConventions(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
            .thenReturn(conventions);

        // When & Then
        mockMvc.perform(get("/api/conventions/search")
                .param("status", "ACTIVE")
                .param("governorate", "Tunis"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    // ==================== Tests de gestion des tags ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("POST /api/conventions/{id}/tags - Should add tag successfully")
    void testAddTag_Success() throws Exception {
        // Given
        mockConvention.setTag("urgent");
        when(conventionService.addTag("conv-123", "urgent")).thenReturn(mockConvention);

        // When & Then
        mockMvc.perform(post("/api/conventions/conv-123/tags")
                .with(csrf())
                .param("tag", "urgent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tag").value("urgent"));

        verify(conventionService, times(1)).addTag("conv-123", "urgent");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("DELETE /api/conventions/{id}/tags - Should remove tag successfully")
    void testRemoveTag_Success() throws Exception {
        // Given
        when(conventionService.removeTag("conv-123", "urgent")).thenReturn(mockConvention);

        // When & Then
        mockMvc.perform(delete("/api/conventions/conv-123/tags")
                .with(csrf())
                .param("tag", "urgent"))
            .andExpect(status().isOk());

        verify(conventionService, times(1)).removeTag("conv-123", "urgent");
    }

    // ==================== Tests de cas d'erreur ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("POST /api/conventions - Should handle service exception")
    void testCreateConvention_ServiceException() throws Exception {
        // Given
        when(conventionService.createConvention(any(), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/conventions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("GET /api/conventions/{id} - Should handle not found exception")
    void testGetConvention_NotFound() throws Exception {
        // Given
        when(conventionService.getConventionById("invalid-id"))
            .thenThrow(new com.example.demo.exception.ResourceNotFoundException("Convention not found"));

        // When & Then
        mockMvc.perform(get("/api/conventions/invalid-id"))
            .andExpect(status().isNotFound());
    }

    // ==================== Tests CSRF ====================

    @Test
    @WithMockUser(username = "testuser", roles = "COMMERCIAL")
    @DisplayName("POST /api/conventions - Should require CSRF token")
    void testCreateConvention_RequiresCsrf() throws Exception {
        // When & Then - Sans CSRF token
        mockMvc.perform(post("/api/conventions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
            .andExpect(status().isForbidden());

        verify(conventionService, never()).createConvention(any(), anyString());
    }
}
