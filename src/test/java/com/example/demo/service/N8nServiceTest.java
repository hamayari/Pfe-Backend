package com.example.demo.service;

import com.example.demo.controller.PublicCrudController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour N8nService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class N8nServiceTest {

    @Mock
    private PublicCrudController publicCrudController;

    @InjectMocks
    private N8nService n8nService;

    private Map<String, Object> mockData;
    private Map<String, Object> mockResponse;

    @BeforeEach
    void setUp() {
        // Mock data
        mockData = new HashMap<>();
        mockData.put("id", "test-123");
        mockData.put("name", "Test Item");
        mockData.put("amount", 1000);

        // Mock response
        mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("message", "Operation successful");
        mockResponse.put("data", mockData);
    }

    // ==================== Tests executeOperation ====================

    @Test
    @DisplayName("Should execute operation successfully")
    void testExecuteOperation_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.executeOperation("create", "CONVENTION", mockData);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        verify(publicCrudController, times(1)).executeOperation(any());
    }

    @Test
    @DisplayName("Should handle null controller")
    void testExecuteOperation_NullController() {
        // Given
        n8nService = new N8nService(); // Sans injection du controller

        // When
        Map<String, Object> result = n8nService.executeOperation("create", "CONVENTION", mockData);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("non disponible"));
    }

    @Test
    @DisplayName("Should handle controller exception")
    void testExecuteOperation_Exception() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        Map<String, Object> result = n8nService.executeOperation("create", "CONVENTION", mockData);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("Erreur"));
    }

    // ==================== Tests CRUD Conventions ====================

    @Test
    @DisplayName("Should create convention")
    void testCreateConvention_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.createConvention(mockData);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        verify(publicCrudController, times(1)).executeOperation(any());
    }

    @Test
    @DisplayName("Should get conventions")
    void testGetConventions_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.getConventions(new HashMap<>());

        // Then
        assertNotNull(result);
        verify(publicCrudController, times(1)).executeOperation(any());
    }

    @Test
    @DisplayName("Should update convention")
    void testUpdateConvention_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.updateConvention("conv-123", mockData);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        verify(publicCrudController, times(1)).executeOperation(any());
    }

    @Test
    @DisplayName("Should delete convention")
    void testDeleteConvention_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.deleteConvention("conv-123");

        // Then
        assertNotNull(result);
        verify(publicCrudController, times(1)).executeOperation(any());
    }

    // ==================== Tests CRUD Invoices ====================

    @Test
    @DisplayName("Should create invoice")
    void testCreateInvoice_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.createInvoice(mockData);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    @DisplayName("Should get invoices")
    void testGetInvoices_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.getInvoices(new HashMap<>());

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should update invoice")
    void testUpdateInvoice_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.updateInvoice("inv-123", mockData);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should delete invoice")
    void testDeleteInvoice_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.deleteInvoice("inv-123");

        // Then
        assertNotNull(result);
    }

    // ==================== Tests CRUD Users ====================

    @Test
    @DisplayName("Should create user")
    void testCreateUser_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.createUser(mockData);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    @DisplayName("Should get users")
    void testGetUsers_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.getUsers(new HashMap<>());

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should update user")
    void testUpdateUser_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.updateUser("user-123", mockData);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should delete user")
    void testDeleteUser_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.deleteUser("user-123");

        // Then
        assertNotNull(result);
    }

    // ==================== Tests CRUD Structures ====================

    @Test
    @DisplayName("Should create structure")
    void testCreateStructure_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.createStructure(mockData);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should get structures")
    void testGetStructures_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.getStructures(new HashMap<>());

        // Then
        assertNotNull(result);
    }

    // ==================== Tests CRUD Applications ====================

    @Test
    @DisplayName("Should create application")
    void testCreateApplication_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.createApplication(mockData);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should get applications")
    void testGetApplications_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.getApplications(new HashMap<>());

        // Then
        assertNotNull(result);
    }

    // ==================== Tests Notifications ====================

    @Test
    @DisplayName("Should send notification")
    void testSendNotification_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.sendNotification(mockData);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should get notifications")
    void testGetNotifications_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.getNotifications(new HashMap<>());

        // Then
        assertNotNull(result);
    }

    // ==================== Tests Payments ====================

    @Test
    @DisplayName("Should create payment")
    void testCreatePayment_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.createPayment(mockData);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should validate payment")
    void testValidatePayment_Success() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.validatePayment("payment-123", mockData);

        // Then
        assertNotNull(result);
    }

    // ==================== Tests Edge Cases ====================

    @Test
    @DisplayName("Should handle empty data")
    void testExecuteOperation_EmptyData() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.executeOperation("create", "CONVENTION", new HashMap<>());

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle null data")
    void testExecuteOperation_NullData() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.executeOperation("create", "CONVENTION", null);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle invalid operation")
    void testExecuteOperation_InvalidOperation() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.executeOperation("invalid", "CONVENTION", mockData);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle invalid entity type")
    void testExecuteOperation_InvalidEntityType() {
        // Given
        when(publicCrudController.executeOperation(any()))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        Map<String, Object> result = n8nService.executeOperation("create", "INVALID", mockData);

        // Then
        assertNotNull(result);
    }
}
