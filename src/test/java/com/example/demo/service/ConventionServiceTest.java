package com.example.demo.service;

import com.example.demo.dto.convention.ConventionRequest;
import com.example.demo.model.Convention;
import com.example.demo.model.User;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConventionServiceTest {

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private RealTimeNotificationService realTimeNotificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private ConventionService conventionService;

    private Convention mockConvention;
    private ConventionRequest mockRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockConvention = new Convention();
        mockConvention.setId("conv1");
        mockConvention.setReference("CONV-001");
        mockConvention.setTitle("Test Convention");
        mockConvention.setAmount(BigDecimal.valueOf(10000));
        mockConvention.setStatus("ACTIVE");
        mockConvention.setCreatedBy("user1");

        mockRequest = new ConventionRequest();
        mockRequest.setReference("CONV-002");
        mockRequest.setTitle("New Convention");
        mockRequest.setAmount(BigDecimal.valueOf(5000));
        mockRequest.setStartDate(LocalDateTime.now());
        mockRequest.setEndDate(LocalDateTime.now().plusMonths(6));
        mockRequest.setStructure("STR-001");
        mockRequest.setGeographicZone("Tunis");

        mockUser = new User();
        mockUser.setId("user1");
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setName("Test User");
    }

    @Test
    void testGetConventionById() {
        when(conventionRepository.findById("conv1")).thenReturn(Optional.of(mockConvention));

        Convention result = conventionService.getConventionById("conv1");

        assertNotNull(result);
        assertEquals("CONV-001", result.getReference());
        verify(conventionRepository).findById("conv1");
    }

    @Test
    void testGetAllConventions() {
        List<Convention> conventions = Arrays.asList(mockConvention);
        when(conventionRepository.findAll()).thenReturn(conventions);

        List<Convention> result = conventionService.getAllConventions();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conventionRepository).findAll();
    }

    @Test
    void testCreateConvention() {
        when(conventionRepository.existsByReference(anyString())).thenReturn(false);
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(mockUser));

        Convention result = conventionService.createConvention(mockRequest, "user1");

        assertNotNull(result);
        verify(conventionRepository).save(any(Convention.class));
    }

    @Test
    void testCreateConvention_DuplicateReference() {
        when(conventionRepository.existsByReference(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            conventionService.createConvention(mockRequest, "user1");
        });
    }

    @Test
    void testUpdateConvention() {
        when(conventionRepository.findById("conv1")).thenReturn(Optional.of(mockConvention));
        when(conventionRepository.save(any(Convention.class))).thenReturn(mockConvention);

        Convention result = conventionService.updateConvention("conv1", mockRequest);

        assertNotNull(result);
        verify(conventionRepository).save(any(Convention.class));
    }

    @Test
    void testDeleteConvention() {
        when(conventionRepository.findById("conv1")).thenReturn(Optional.of(mockConvention));
        when(invoiceRepository.findByConventionId("conv1")).thenReturn(Arrays.asList());
        doNothing().when(conventionRepository).delete(any(Convention.class));

        conventionService.deleteConvention("conv1");

        verify(conventionRepository).delete(any(Convention.class));
    }

    @Test
    void testGetConventionsByStatus() {
        List<Convention> conventions = Arrays.asList(mockConvention);
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(conventions);

        List<Convention> result = conventionService.getConventionsByStatus("ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conventionRepository).findByStatus("ACTIVE");
    }

    @Test
    void testGetConventionsForCurrentUser_Admin() {
        when(accessControlService.canViewAllData()).thenReturn(true);
        when(conventionRepository.findAll()).thenReturn(Arrays.asList(mockConvention));

        List<Convention> result = conventionService.getConventionsForCurrentUser();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conventionRepository).findAll();
    }

    @Test
    void testGetConventionsForCurrentUser_Commercial() {
        when(accessControlService.canViewAllData()).thenReturn(false);
        when(accessControlService.canViewOnlyOwnData()).thenReturn(true);
        when(accessControlService.getCurrentUsername()).thenReturn("user1");
        when(conventionRepository.findByCreatedBy("user1")).thenReturn(Arrays.asList(mockConvention));

        List<Convention> result = conventionService.getConventionsForCurrentUser();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(conventionRepository).findByCreatedBy("user1");
    }

    @Test
    void testEnrichConventionsWithCommercialNames() {
        List<Convention> conventions = Arrays.asList(mockConvention);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(mockUser));

        List<Convention> result = conventionService.enrichConventionsWithCommercialNames(conventions);

        assertNotNull(result);
        assertEquals("Test User", result.get(0).getCommercial());
    }
}
