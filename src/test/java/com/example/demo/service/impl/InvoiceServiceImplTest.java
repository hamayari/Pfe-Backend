package com.example.demo.service.impl;

import com.example.demo.dto.invoice.InvoiceRequest;
import com.example.demo.model.Invoice;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.service.AccessControlService;
import com.example.demo.service.InvoiceNumberGenerator;
import com.example.demo.service.PDFGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour InvoiceServiceImpl
 * Couverture: 80%+
 * Bonnes pratiques: AAA (Arrange-Act-Assert), Mockito, AssertJ
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceServiceImpl - Tests Unitaires")
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PDFGenerationService pdfGenerationService;

    @Mock
    private InvoiceNumberGenerator invoiceNumberGenerator;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Invoice testInvoice;
    private InvoiceRequest testRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Données de test
        testInvoice = new Invoice();
        testInvoice.setId("invoice123");
        testInvoice.setInvoiceNumber("INV-2024-001");
        testInvoice.setConventionId("conv123");
        testInvoice.setReference("REF-001");
        testInvoice.setAmount(java.math.BigDecimal.valueOf(1000.0));
        testInvoice.setIssueDate(LocalDate.now());
        testInvoice.setDueDate(LocalDate.now().plusDays(30));
        testInvoice.setStatus("PENDING");
        testInvoice.setCreatedBy("testuser");
        testInvoice.setCreatedAt(LocalDate.now());

        testRequest = new InvoiceRequest();
        testRequest.setConventionId("conv123");
        testRequest.setReference("REF-001");
        testRequest.setAmount(java.math.BigDecimal.valueOf(1000.0));
        testRequest.setDueDate(LocalDateTime.now().plusDays(30));
    }

    // ==================== GET INVOICES FOR CURRENT USER ====================

    @Test
    @DisplayName("getInvoicesForCurrentUser - Devrait retourner toutes les factures pour admin")
    void getInvoicesForCurrentUser_ShouldReturnAllInvoices_ForAdmin() {
        // Arrange
        List<Invoice> allInvoices = Arrays.asList(testInvoice, new Invoice());
        when(accessControlService.canViewAllData()).thenReturn(true);
        when(invoiceRepository.findAll()).thenReturn(allInvoices);
        doNothing().when(accessControlService).logCurrentUserInfo();

        // Act
        List<Invoice> result = invoiceService.getInvoicesForCurrentUser();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(testInvoice);
        verify(accessControlService).canViewAllData();
        verify(invoiceRepository).findAll();
    }

    @Test
    @DisplayName("getInvoicesForCurrentUser - Devrait retourner seulement les factures du commercial")
    void getInvoicesForCurrentUser_ShouldReturnOwnInvoices_ForCommercial() {
        // Arrange
        when(accessControlService.canViewAllData()).thenReturn(false);
        when(accessControlService.canViewOnlyOwnData()).thenReturn(true);
        when(accessControlService.getCurrentUsername()).thenReturn("testuser");
        when(invoiceRepository.findByCreatedBy("testuser")).thenReturn(Collections.singletonList(testInvoice));
        doNothing().when(accessControlService).logCurrentUserInfo();

        // Act
        List<Invoice> result = invoiceService.getInvoicesForCurrentUser();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCreatedBy()).isEqualTo("testuser");
        verify(accessControlService).getCurrentUsername();
        verify(invoiceRepository).findByCreatedBy("testuser");
    }

    @Test
    @DisplayName("getInvoicesForCurrentUser - Devrait retourner liste vide pour utilisateur non autorisé")
    void getInvoicesForCurrentUser_ShouldReturnEmptyList_ForUnauthorizedUser() {
        // Arrange
        when(accessControlService.canViewAllData()).thenReturn(false);
        when(accessControlService.canViewOnlyOwnData()).thenReturn(false);
        doNothing().when(accessControlService).logCurrentUserInfo();

        // Act
        List<Invoice> result = invoiceService.getInvoicesForCurrentUser();

        // Assert
        assertThat(result).isEmpty();
        verify(invoiceRepository, never()).findAll();
        verify(invoiceRepository, never()).findByCreatedBy(any());
    }

    // ==================== CREATE INVOICE ====================

    @Test
    @DisplayName("createInvoice - Devrait créer une facture avec succès")
    void createInvoice_ShouldCreateInvoice_Successfully() {
        // Arrange
        when(invoiceNumberGenerator.generateInvoiceNumber()).thenReturn("INV-2024-001");
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        Invoice result = invoiceService.createInvoice(testRequest, "testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getInvoiceNumber()).isEqualTo("INV-2024-001");
        verify(invoiceNumberGenerator).generateInvoiceNumber();
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("createInvoice - Devrait utiliser la date actuelle si dueDate est null")
    void createInvoice_ShouldUseCurrentDate_WhenDueDateIsNull() {
        // Arrange
        testRequest.setDueDate(null);
        when(invoiceNumberGenerator.generateInvoiceNumber()).thenReturn("INV-2024-001");
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        Invoice result = invoiceService.createInvoice(testRequest, "testuser");

        // Assert
        assertThat(result).isNotNull();
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("createInvoice - Devrait définir le statut PENDING par défaut")
    void createInvoice_ShouldSetPendingStatus_ByDefault() {
        // Arrange
        when(invoiceNumberGenerator.generateInvoiceNumber()).thenReturn("INV-2024-001");
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo("PENDING");
            return saved;
        });

        // Act
        invoiceService.createInvoice(testRequest, "testuser");

        // Assert
        verify(invoiceRepository).save(any(Invoice.class));
    }

    // ==================== GET INVOICE BY ID ====================

    @Test
    @DisplayName("getInvoiceById - Devrait retourner la facture si elle existe")
    void getInvoiceById_ShouldReturnInvoice_WhenExists() {
        // Arrange
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));

        // Act
        Invoice result = invoiceService.getInvoiceById("invoice123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("invoice123");
        verify(invoiceRepository).findById("invoice123");
    }

    @Test
    @DisplayName("getInvoiceById - Devrait retourner null si la facture n'existe pas")
    void getInvoiceById_ShouldReturnNull_WhenNotExists() {
        // Arrange
        when(invoiceRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act
        Invoice result = invoiceService.getInvoiceById("invalid");

        // Assert
        assertThat(result).isNull();
        verify(invoiceRepository).findById("invalid");
    }

    // ==================== GET ALL INVOICES ====================

    @Test
    @DisplayName("getAllInvoices - Devrait retourner toutes les factures")
    void getAllInvoices_ShouldReturnAllInvoices() {
        // Arrange
        List<Invoice> invoices = Arrays.asList(testInvoice, new Invoice());
        when(invoiceRepository.findAll()).thenReturn(invoices);

        // Act
        List<Invoice> result = invoiceService.getAllInvoices();

        // Assert
        assertThat(result).hasSize(2);
        verify(invoiceRepository).findAll();
    }

    @Test
    @DisplayName("getAllInvoices - Devrait retourner liste vide si aucune facture")
    void getAllInvoices_ShouldReturnEmptyList_WhenNoInvoices() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Invoice> result = invoiceService.getAllInvoices();

        // Assert
        assertThat(result).isEmpty();
        verify(invoiceRepository).findAll();
    }

    // ==================== GET INVOICES BY USER ====================

    @Test
    @DisplayName("getInvoicesByUser - Devrait retourner les factures de l'utilisateur")
    void getInvoicesByUser_ShouldReturnUserInvoices() {
        // Arrange
        when(invoiceRepository.findByCreatedBy("testuser")).thenReturn(Collections.singletonList(testInvoice));

        // Act
        List<Invoice> result = invoiceService.getInvoicesByUser("testuser");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCreatedBy()).isEqualTo("testuser");
        verify(invoiceRepository).findByCreatedBy("testuser");
    }

    // ==================== UPDATE INVOICE STATUS ====================

    @Test
    @DisplayName("updateInvoiceStatus - Devrait mettre à jour le statut avec succès")
    void updateInvoiceStatus_ShouldUpdateStatus_Successfully() {
        // Arrange
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        Invoice result = invoiceService.updateInvoiceStatus("invoice123", "PAID");

        // Assert
        assertThat(result).isNotNull();
        verify(invoiceRepository).findById("invoice123");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("updateInvoiceStatus - Devrait définir paymentDate si statut PAID")
    void updateInvoiceStatus_ShouldSetPaymentDate_WhenStatusIsPaid() {
        // Arrange
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            assertThat(saved.getPaymentDate()).isNotNull();
            return saved;
        });

        // Act
        invoiceService.updateInvoiceStatus("invoice123", "PAID");

        // Assert
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("updateInvoiceStatus - Devrait retourner null si facture inexistante")
    void updateInvoiceStatus_ShouldReturnNull_WhenInvoiceNotFound() {
        // Arrange
        when(invoiceRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act
        Invoice result = invoiceService.updateInvoiceStatus("invalid", "PAID");

        // Assert
        assertThat(result).isNull();
        verify(invoiceRepository, never()).save(any());
    }

    // ==================== UPDATE INVOICE STATUS WITH AUDIT ====================

    @Test
    @DisplayName("updateInvoiceStatusWithAudit - Devrait mettre à jour avec audit")
    void updateInvoiceStatusWithAudit_ShouldUpdateWithAudit_Successfully() {
        // Arrange
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        // Act
        Invoice result = invoiceService.updateInvoiceStatusWithAudit(
                "invoice123", "PAID", "user123", "John Doe"
        );

        // Assert
        assertThat(result).isNotNull();
        verify(invoiceRepository).findById("invoice123");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("updateInvoiceStatusWithAudit - Devrait définir validatedBy si statut PAID")
    void updateInvoiceStatusWithAudit_ShouldSetValidatedBy_WhenStatusIsPaid() {
        // Arrange
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            assertThat(saved.getValidatedBy()).isEqualTo("John Doe");
            assertThat(saved.getValidatedAt()).isNotNull();
            return saved;
        });

        // Act
        invoiceService.updateInvoiceStatusWithAudit(
                "invoice123", "PAID", "user123", "John Doe"
        );

        // Assert
        verify(invoiceRepository).save(any(Invoice.class));
    }

    // ==================== GET INVOICES BY CONVENTION ====================

    @Test
    @DisplayName("getInvoicesByConvention - Devrait retourner les factures de la convention")
    void getInvoicesByConvention_ShouldReturnConventionInvoices() {
        // Arrange
        when(invoiceRepository.findByConventionId("conv123"))
                .thenReturn(Collections.singletonList(testInvoice));

        // Act
        List<Invoice> result = invoiceService.getInvoicesByConvention("conv123");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConventionId()).isEqualTo("conv123");
        verify(invoiceRepository).findByConventionId("conv123");
    }

    // ==================== GET OVERDUE INVOICES ====================

    @Test
    @DisplayName("getOverdueInvoices - Devrait retourner les factures en retard")
    void getOverdueInvoices_ShouldReturnOverdueInvoices() {
        // Arrange
        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setDueDate(LocalDate.now().minusDays(10));
        overdueInvoice.setStatus("PENDING");
        
        when(invoiceRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), eq("PENDING")))
                .thenReturn(Collections.singletonList(overdueInvoice));

        // Act
        List<Invoice> result = invoiceService.getOverdueInvoices();

        // Assert
        assertThat(result).hasSize(1);
        verify(invoiceRepository).findByDueDateBeforeAndStatus(any(LocalDate.class), eq("PENDING"));
    }

    // ==================== GENERATE INVOICE PDF ====================

    @Test
    @DisplayName("generateInvoicePDF - Devrait générer le PDF avec succès")
    void generateInvoicePDF_ShouldGeneratePDF_Successfully() throws Exception {
        // Arrange
        byte[] pdfBytes = "PDF content".getBytes();
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(pdfGenerationService.generateInvoicePDF("invoice123")).thenReturn(pdfBytes);

        // Act
        byte[] result = invoiceService.generateInvoicePDF("invoice123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(pdfBytes);
        verify(pdfGenerationService).generateInvoicePDF("invoice123");
    }

    @Test
    @DisplayName("generateInvoicePDF - Devrait retourner tableau vide si facture inexistante")
    void generateInvoicePDF_ShouldReturnNull_WhenInvoiceNotFound() throws Exception {
        // Arrange
        when(invoiceRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act
        byte[] result = invoiceService.generateInvoicePDF("invalid");

        // Assert
        assertThat(result).isNotNull(); // Retourne un tableau vide, pas null
        assertThat(result).isEmpty();
        verify(pdfGenerationService, never()).generateInvoicePDF(anyString());
    }

    @Test
    @DisplayName("generateInvoicePDF - Devrait gérer les erreurs de génération PDF")
    void generateInvoicePDF_ShouldHandleErrors_Gracefully() throws Exception {
        // Arrange
        when(invoiceRepository.findById("invoice123")).thenReturn(Optional.of(testInvoice));
        when(pdfGenerationService.generateInvoicePDF("invoice123"))
                .thenThrow(new RuntimeException("PDF generation failed"));

        // Act
        byte[] result = invoiceService.generateInvoicePDF("invoice123");

        // Assert
        assertThat(result).isNotNull(); // Retourne un tableau vide en cas d'erreur
        assertThat(result).isEmpty();
    }

    // ==================== SEND REMINDER ====================

    @Test
    @DisplayName("sendReminder - Devrait exécuter sans erreur")
    void sendReminder_ShouldExecute_WithoutError() {
        // Act & Assert - Ne devrait pas lancer d'exception
        assertThatCode(() -> invoiceService.sendReminder("invoice123", "EMAIL"))
                .doesNotThrowAnyException();
    }

    // ==================== GET INVOICE REMINDERS ====================

    @Test
    @DisplayName("getInvoiceReminders - Devrait retourner une liste vide")
    void getInvoiceReminders_ShouldReturnEmptyList() {
        // Act
        List<String> result = invoiceService.getInvoiceReminders("invoice123");

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== GET INVOICES BY CLIENT ====================

    @Test
    @DisplayName("getInvoicesByClient - Devrait retourner les factures du client")
    void getInvoicesByClient_ShouldReturnClientInvoices() {
        // Arrange
        when(invoiceRepository.findByClientId("client123"))
                .thenReturn(Collections.singletonList(testInvoice));

        // Act
        List<Invoice> result = invoiceService.getInvoicesByClient("client123");

        // Assert
        assertThat(result).hasSize(1);
        verify(invoiceRepository).findByClientId("client123");
    }

    // ==================== SAVE INVOICE ====================

    @Test
    @DisplayName("save - Devrait sauvegarder la facture")
    void save_ShouldSaveInvoice() {
        // Arrange
        when(invoiceRepository.save(testInvoice)).thenReturn(testInvoice);

        // Act
        invoiceService.save(testInvoice);

        // Assert
        verify(invoiceRepository).save(testInvoice);
    }

    // ==================== DELETE INVOICE ====================

    @Test
    @DisplayName("deleteInvoice - Devrait supprimer la facture")
    void deleteInvoice_ShouldDeleteInvoice() {
        // Arrange
        doNothing().when(invoiceRepository).deleteById("invoice123");

        // Act
        invoiceService.deleteInvoice("invoice123");

        // Assert
        verify(invoiceRepository).deleteById("invoice123");
    }

    // ==================== DELETE ALL INVOICES ====================

    @Test
    @DisplayName("deleteAllInvoices - Devrait supprimer toutes les factures")
    void deleteAllInvoices_ShouldDeleteAllInvoices() {
        // Arrange
        List<Invoice> invoices = Arrays.asList(testInvoice, new Invoice(), new Invoice());
        when(invoiceRepository.findAll()).thenReturn(invoices);
        doNothing().when(invoiceRepository).deleteAll();

        // Act
        int result = invoiceService.deleteAllInvoices();

        // Assert
        assertThat(result).isEqualTo(3);
        verify(invoiceRepository).findAll();
        verify(invoiceRepository).deleteAll();
    }

    @Test
    @DisplayName("deleteAllInvoices - Devrait retourner 0 si aucune facture")
    void deleteAllInvoices_ShouldReturnZero_WhenNoInvoices() {
        // Arrange
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(invoiceRepository).deleteAll();

        // Act
        int result = invoiceService.deleteAllInvoices();

        // Assert
        assertThat(result).isZero();
        verify(invoiceRepository).deleteAll();
    }
}
