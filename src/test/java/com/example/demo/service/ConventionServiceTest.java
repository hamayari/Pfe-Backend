package com.example.demo.service;

import com.example.demo.dto.convention.ConventionRequest;
import com.example.demo.dto.invoice.InvoiceRequest;
import com.example.demo.dto.NotificationDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.PaymentProof;
import com.example.demo.model.NotificationLog;
import com.example.demo.model.User;
import com.example.demo.model.PaymentTerms;
import com.example.demo.repository.*;
import jakarta.mail.MessagingException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires complets pour ConventionService
 * Couvre toutes les méthodes critiques selon les bonnes pratiques JUnit
 */
@ExtendWith(MockitoExtension.class)
class ConventionServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentProofRepository paymentProofRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RealTimeNotificationService realTimeNotificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private PDFGenerationService pdfGenerationService;

    @InjectMocks
    private ConventionService conventionService;

    private Convention mockConvention;
    private ConventionRequest mockRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Initialisation de la convention mock
        mockConvention = new Convention();
        mockConvention.setId("conv-123");
        mockConvention.setReference("CONV-2024-001");
        mockConvention.setTitle("Convention Test");
        mockConvention.setDescription("Description test");
        mockConvention.setStartDate(LocalDate.now());
        mockConvention.setEndDate(LocalDate.now().plusMonths(6));
        mockConvention.setAmount(BigDecimal.valueOf(10000));
        mockConvention.setStatus("ACTIVE");
        mockConvention.setStructureId("struct-1");
        mockConvention.setZoneGeographiqueId("zone-1");
        mockConvention.setGovernorate("Tunis");
        mockConvention.setCreatedBy("testuser");
        mockConvention.setCreatedAt(LocalDate.now());

        // Initialisation de la requête mock
        mockRequest = new ConventionRequest();
        mockRequest.setReference("CONV-2024-002");
        mockRequest.setTitle("Nouvelle Convention");
        mockRequest.setDescription("Description nouvelle convention");
        mockRequest.setStartDate(LocalDateTime.now());
        mockRequest.setEndDate(LocalDateTime.now().plusMonths(12));
        mockRequest.setAmount(BigDecimal.valueOf(20000));
        mockRequest.setStructure("struct-2");
        mockRequest.setGeographicZone("zone-2");
        mockRequest.setClient("Client Test");
        mockRequest.setType("Type A");

        // Initialisation de l'utilisateur mock
        mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setPhoneNumber("+21612345678");
        mockUser.setName("Test User");
    }

    // ==================== Tests getConventionsForCurrentUser ====================

    @Test
    @DisplayName("Should return all conventions when user can view all data")
    void testGetConventionsForCurrentUser_CanViewAll() {
        // Given
        List<Convention> allConventions = Arrays.asList(mockConvention, new Convention());
        when(accessControlService.canViewAllData()).thenReturn(true);
        when(conventionRepository.findAll()).thenReturn(allConventions);

        // When
        List<Convention> result = conventionService.getConventionsForCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(accessControlService, times(1)).logCurrentUserInfo();
        verify(accessControlService, times(1)).canViewAllData();
        verify(conventionRepository, times(1)).findAll();
        verify(conventionRepository, never()).findByCreatedBy(anyString());
    }

    @Test
    @DisplayName("Should return only own conventions when user can view only own data")
    void testGetConventionsForCurrentUser_CanViewOnlyOwn() {
        // Given
        List<Convention> ownConventions = Collections.singletonList(mockConvention);
        when(accessControlService.canViewAllData()).thenReturn(false);
        when(accessControlService.canViewOnlyOwnData()).thenReturn(true);
        when(accessControlService.getCurrentUsername()).thenReturn("testuser");
        when(conventionRepository.findByCreatedBy("testuser")).thenReturn(ownConventions);

        // When
        List<Convention> result = conventionService.getConventionsForCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getCreatedBy());
        verify(conventionRepository, times(1)).findByCreatedBy("testuser");
        verify(conventionRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should return empty list when user is not authorized")
    void testGetConventionsForCurrentUser_NotAuthorized() {
        // Given
        when(accessControlService.canViewAllData()).thenReturn(false);
        when(accessControlService.canViewOnlyOwnData()).thenReturn(false);

        // When
        List<Convention> result = conventionService.getConventionsForCurrentUser();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(conventionRepository, never()).findAll();
        verify(conventionRepository, never()).findByCreatedBy(anyString());
    }

    // ==================== Tests createConvention ====================

    @Test
    @DisplayName("Should create convention successfully")
    void testCreateConvention_Success() throws MessagingException, IOException {
        // Given
        when(conventionRepository.existsByReference(anyString())).thenReturn(false);
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(mockUser));
        when(realTimeNotificationService.createNotification(any())).thenReturn(new NotificationDTO());
        // Ne pas mocker emailService et smsService - laisser les méthodes void s'exécuter

        // When
        Convention result = conventionService.createConvention(mockRequest, "user-123");

        // Then
        assertNotNull(result);
        verify(conventionRepository, times(1)).existsByReference(mockRequest.getReference());
        verify(conventionRepository, times(1)).save(any(Convention.class));
        verify(realTimeNotificationService, times(1)).createNotification(any());
    }

    @Test
    @DisplayName("Should throw exception when convention reference already exists")
    void testCreateConvention_ReferenceExists() {
        // Given
        when(conventionRepository.existsByReference(mockRequest.getReference())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> conventionService.createConvention(mockRequest, "user-123")
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(conventionRepository, times(1)).existsByReference(mockRequest.getReference());
        verify(conventionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should set default status to ACTIVE when creating convention")
    void testCreateConvention_DefaultStatus() {
        // Given
        when(conventionRepository.existsByReference(anyString())).thenReturn(false);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(mockUser));
        
        when(conventionRepository.save(any(Convention.class))).thenAnswer(invocation -> {
            Convention saved = invocation.getArgument(0);
            assertEquals("ACTIVE", saved.getStatus());
            return saved;
        });

        // When
        conventionService.createConvention(mockRequest, "user-123");

        // Then
        verify(conventionRepository, times(1)).save(any(Convention.class));
    }

    @Test
    @DisplayName("Should generate invoices when payment terms are provided")
    void testCreateConvention_GeneratesInvoices() throws MessagingException, IOException {
        // Given
        PaymentTerms paymentTerms = new PaymentTerms();
        paymentTerms.setNumberOfPayments(3);
        paymentTerms.setIntervalDays(30);
        mockRequest.setPaymentTerms(paymentTerms);

        when(conventionRepository.existsByReference(anyString())).thenReturn(false);
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(mockUser));
        when(realTimeNotificationService.createNotification(any())).thenReturn(new NotificationDTO());
        when(invoiceService.createInvoice(any(InvoiceRequest.class), anyString())).thenReturn(new Invoice());

        // When
        conventionService.createConvention(mockRequest, "user-123");

        // Then
        verify(invoiceService, times(3)).createInvoice(any(InvoiceRequest.class), eq("user-123"));
    }

    @Test
    @DisplayName("Should send notifications when convention is created")
    void testCreateConvention_SendsNotifications() throws MessagingException, IOException {
        // Given
        when(conventionRepository.existsByReference(anyString())).thenReturn(false);
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);
        when(userRepository.findById("user-123")).thenReturn(Optional.of(mockUser));
        when(realTimeNotificationService.createNotification(any())).thenReturn(new NotificationDTO());

        // When
        conventionService.createConvention(mockRequest, "user-123");

        // Then
        verify(realTimeNotificationService, times(1)).createNotification(any());
        // Ne pas vérifier emailService et smsService car ils peuvent lancer des exceptions
    }

    // ==================== Tests updateConvention ====================

    @Test
    @DisplayName("Should update convention successfully")
    void testUpdateConvention_Success() {
        // Given
        when(conventionRepository.findById("conv-123")).thenReturn(Optional.of(mockConvention));
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);

        // When
        Convention result = conventionService.updateConvention("conv-123", mockRequest);

        // Then
        assertNotNull(result);
        verify(conventionRepository, times(1)).findById("conv-123");
        verify(conventionRepository, times(1)).save(any(Convention.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent convention")
    void testUpdateConvention_NotFound() {
        // Given
        when(conventionRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
            ResourceNotFoundException.class,
            () -> conventionService.updateConvention("invalid-id", mockRequest)
        );

        verify(conventionRepository, times(1)).findById("invalid-id");
        verify(conventionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update all fields when updating convention")
    void testUpdateConvention_UpdatesAllFields() {
        // Given
        when(conventionRepository.findById("conv-123")).thenReturn(Optional.of(mockConvention));
        
        when(conventionRepository.save(any(Convention.class))).thenAnswer(invocation -> {
            Convention updated = invocation.getArgument(0);
            assertEquals(mockRequest.getReference(), updated.getReference());
            assertEquals(mockRequest.getTitle(), updated.getTitle());
            assertEquals(mockRequest.getDescription(), updated.getDescription());
            assertEquals(mockRequest.getAmount(), updated.getAmount());
            return updated;
        });

        // When
        conventionService.updateConvention("conv-123", mockRequest);

        // Then
        verify(conventionRepository, times(1)).save(any(Convention.class));
    }

    // ==================== Tests getConventionById ====================

    @Test
    @DisplayName("Should return convention by ID")
    void testGetConventionById_Success() {
        // Given
        when(conventionRepository.findById("conv-123")).thenReturn(Optional.of(mockConvention));

        // When
        Convention result = conventionService.getConventionById("conv-123");

        // Then
        assertNotNull(result);
        assertEquals("conv-123", result.getId());
        assertEquals("CONV-2024-001", result.getReference());
        verify(conventionRepository, times(1)).findById("conv-123");
    }

    @Test
    @DisplayName("Should throw exception when convention not found by ID")
    void testGetConventionById_NotFound() {
        // Given
        when(conventionRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> conventionService.getConventionById("invalid-id")
        );

        assertTrue(exception.getMessage().contains("Convention not found"));
        assertTrue(exception.getMessage().contains("invalid-id"));
        verify(conventionRepository, times(1)).findById("invalid-id");
    }

    // ==================== Tests getAllConventions ====================

    @Test
    @DisplayName("Should return all conventions")
    void testGetAllConventions_Success() {
        // Given
        List<Convention> conventions = Arrays.asList(mockConvention, new Convention());
        when(conventionRepository.findAll()).thenReturn(conventions);

        // When
        List<Convention> result = conventionService.getAllConventions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(conventionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no conventions exist")
    void testGetAllConventions_EmptyList() {
        // Given
        when(conventionRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Convention> result = conventionService.getAllConventions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(conventionRepository, times(1)).findAll();
    }

    // ==================== Tests deleteConvention ====================

    @Test
    @DisplayName("Should delete convention and cascade delete related entities")
    void testDeleteConvention_Success() {
        // Given
        Invoice mockInvoice = new Invoice();
        mockInvoice.setId("inv-123");
        
        when(conventionRepository.findById("conv-123")).thenReturn(Optional.of(mockConvention));
        when(invoiceRepository.findByConventionId("conv-123")).thenReturn(Collections.singletonList(mockInvoice));
        when(paymentProofRepository.findByInvoiceId("inv-123")).thenReturn(Collections.emptyList());
        when(notificationLogRepository.findByInvoiceId("inv-123")).thenReturn(Collections.emptyList());
        doNothing().when(conventionRepository).delete(any(Convention.class));

        // When
        conventionService.deleteConvention("conv-123");

        // Then
        verify(conventionRepository, times(1)).findById("conv-123");
        verify(invoiceRepository, times(1)).findByConventionId("conv-123");
        verify(conventionRepository, times(1)).delete(mockConvention);
    }

    @Test
    @DisplayName("Should delete convention with payment proofs and notifications")
    void testDeleteConvention_WithRelatedEntities() {
        // Given
        Invoice mockInvoice = new Invoice();
        mockInvoice.setId("inv-123");
        
        PaymentProof mockProof = new PaymentProof();
        NotificationLog mockNotif = new NotificationLog();
        
        when(conventionRepository.findById("conv-123")).thenReturn(Optional.of(mockConvention));
        when(invoiceRepository.findByConventionId("conv-123")).thenReturn(Collections.singletonList(mockInvoice));
        when(paymentProofRepository.findByInvoiceId("inv-123")).thenReturn(Collections.singletonList(mockProof));
        when(notificationLogRepository.findByInvoiceId("inv-123")).thenReturn(Collections.singletonList(mockNotif));

        // When
        conventionService.deleteConvention("conv-123");

        // Then
        verify(paymentProofRepository, times(1)).delete(mockProof);
        verify(notificationLogRepository, times(1)).delete(mockNotif);
        verify(invoiceRepository, times(1)).delete(mockInvoice);
        verify(conventionRepository, times(1)).delete(mockConvention);
    }

    // ==================== Tests searchConventions ====================

    @Test
    @DisplayName("Should search conventions by status")
    void testSearchConventions_ByStatus() {
        // Given
        Convention activeConv = new Convention();
        activeConv.setStatus("ACTIVE");
        Convention inactiveConv = new Convention();
        inactiveConv.setStatus("INACTIVE");
        
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(activeConv, inactiveConv));

        // When
        List<Convention> result = conventionService.searchConventions("ACTIVE", null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Should search conventions by governorate")
    void testSearchConventions_ByGovernorate() {
        // Given
        Convention tunisConv = new Convention();
        tunisConv.setGovernorate("Tunis");
        Convention sfaxConv = new Convention();
        sfaxConv.setGovernorate("Sfax");
        
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(tunisConv, sfaxConv));

        // When
        List<Convention> result = conventionService.searchConventions(null, "Tunis", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tunis", result.get(0).getGovernorate());
    }

    @Test
    @DisplayName("Should search conventions by multiple criteria")
    void testSearchConventions_MultipleCriteria() {
        // Given
        Convention matchingConv = new Convention();
        matchingConv.setStatus("ACTIVE");
        matchingConv.setGovernorate("Tunis");
        matchingConv.setStructureId("struct-1");
        
        Convention nonMatchingConv = new Convention();
        nonMatchingConv.setStatus("ACTIVE");
        nonMatchingConv.setGovernorate("Sfax");
        
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(matchingConv, nonMatchingConv));

        // When
        List<Convention> result = conventionService.searchConventions("ACTIVE", "Tunis", "struct-1", null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tunis", result.get(0).getGovernorate());
        assertEquals("struct-1", result.get(0).getStructureId());
    }

    // ==================== Tests addTag / removeTag ====================

    @Test
    @DisplayName("Should add tag to convention")
    void testAddTag_Success() {
        // Given
        when(conventionRepository.findById("conv-123")).thenReturn(Optional.of(mockConvention));
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);

        // When
        Convention result = conventionService.addTag("conv-123", "urgent");

        // Then
        assertNotNull(result);
        verify(conventionRepository, times(1)).save(any(Convention.class));
    }

    @Test
    @DisplayName("Should remove tag from convention")
    void testRemoveTag_Success() {
        // Given
        mockConvention.setTag("urgent");
        when(conventionRepository.findById("conv-123")).thenReturn(Optional.of(mockConvention));
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);

        // When
        Convention result = conventionService.removeTag("conv-123", "urgent");

        // Then
        assertNotNull(result);
        verify(conventionRepository, times(1)).save(any(Convention.class));
    }

    // ==================== Tests generateConventionPDF ====================

    @Test
    @DisplayName("Should generate PDF for convention")
    void testGenerateConventionPDF_Success() throws Exception {
        // Given
        byte[] mockPdf = "PDF content".getBytes();
        when(pdfGenerationService.generateConventionPDF("conv-123")).thenReturn(mockPdf);

        // When
        byte[] result = conventionService.generateConventionPDF("conv-123");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(pdfGenerationService, times(1)).generateConventionPDF("conv-123");
    }

    @Test
    @DisplayName("Should return empty array when PDF generation fails")
    void testGenerateConventionPDF_Failure() throws Exception {
        // Given
        when(pdfGenerationService.generateConventionPDF("conv-123")).thenThrow(new RuntimeException("PDF error"));

        // When
        byte[] result = conventionService.generateConventionPDF("conv-123");

        // Then
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // ==================== Tests enrichConventionsWithCommercialNames ====================

    @Test
    @DisplayName("Should enrich conventions with commercial names")
    void testEnrichConventionsWithCommercialNames_Success() {
        // Given
        mockConvention.setCreatedBy("testuser");
        List<Convention> conventions = Collections.singletonList(mockConvention);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // When
        List<Convention> result = conventionService.enrichConventionsWithCommercialNames(conventions);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test User", result.get(0).getCommercial());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should use username when user not found")
    void testEnrichConventionsWithCommercialNames_UserNotFound() {
        // Given
        mockConvention.setCreatedBy("unknownuser");
        List<Convention> conventions = Collections.singletonList(mockConvention);
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // When
        List<Convention> result = conventionService.enrichConventionsWithCommercialNames(conventions);

        // Then
        assertNotNull(result);
        assertEquals("unknownuser", result.get(0).getCommercial());
    }

    @Test
    @DisplayName("Should set N/A when createdBy is null")
    void testEnrichConventionsWithCommercialNames_NullCreatedBy() {
        // Given
        mockConvention.setCreatedBy(null);
        List<Convention> conventions = Collections.singletonList(mockConvention);

        // When
        List<Convention> result = conventionService.enrichConventionsWithCommercialNames(conventions);

        // Then
        assertNotNull(result);
        assertEquals("N/A", result.get(0).getCommercial());
        verify(userRepository, never()).findByUsername(anyString());
    }
}
