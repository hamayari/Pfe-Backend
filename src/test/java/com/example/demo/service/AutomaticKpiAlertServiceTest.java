package com.example.demo.service;

import com.example.demo.enums.ERole;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomaticKpiAlertServiceTest {

    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private UserRepository userRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private ConventionRepository conventionRepository;
    @Mock private InAppNotificationService notificationService;
    @Mock private KpiAlertRepository kpiAlertRepository;
    @Mock private KpiAlertManagementService kpiAlertManagementService;
    @Mock private KpiAlertEmailService emailService;
    @Mock private KpiAlertSmsService smsService;

    @InjectMocks
    private AutomaticKpiAlertService service;

    private List<Invoice> mockInvoices;
    private List<Convention> mockConventions;
    private List<User> mockUsers;

    @BeforeEach
    void setUp() {
        mockInvoices = createMockInvoices();
        mockConventions = createMockConventions();
        mockUsers = createMockUsers();
    }

    private List<Invoice> createMockInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        
        Invoice i1 = new Invoice();
        i1.setId("inv1");
        i1.setStatus("PAID");
        i1.setAmount(BigDecimal.valueOf(10000));
        i1.setDueDate(LocalDate.now().minusDays(10));
        invoices.add(i1);
        
        Invoice i2 = new Invoice();
        i2.setId("inv2");
        i2.setStatus("OVERDUE");
        i2.setAmount(BigDecimal.valueOf(15000));
        i2.setDueDate(LocalDate.now().minusDays(5));
        invoices.add(i2);
        
        Invoice i3 = new Invoice();
        i3.setId("inv3");
        i3.setStatus("PENDING");
        i3.setAmount(BigDecimal.valueOf(20000));
        i3.setDueDate(LocalDate.now().plusDays(5));
        invoices.add(i3);
        
        return invoices;
    }

    private List<Convention> createMockConventions() {
        List<Convention> conventions = new ArrayList<>();
        
        Convention c1 = new Convention();
        c1.setId("conv1");
        c1.setStatus("ACTIVE");
        c1.setAmount(BigDecimal.valueOf(10000));
        conventions.add(c1);
        
        Convention c2 = new Convention();
        c2.setId("conv2");
        c2.setStatus("PENDING");
        c2.setAmount(BigDecimal.valueOf(15000));
        conventions.add(c2);
        
        return conventions;
    }

    private List<User> createMockUsers() {
        List<User> users = new ArrayList<>();
        
        Role pmRole = new Role();
        pmRole.setName(ERole.ROLE_PROJECT_MANAGER);
        
        User pm = new User();
        pm.setId("pm1");
        pm.setUsername("projectmanager");
        pm.setEmail("pm@example.com");
        pm.setRoles(Set.of(pmRole));
        users.add(pm);
        
        return users;
    }

    @Test
    void testCheckKpiAnomalies_WithHighOverdueRate() {
        when(invoiceRepository.count()).thenReturn(10L);
        when(invoiceRepository.findByStatus("OVERDUE")).thenReturn(Arrays.asList(mockInvoices.get(1)));
        when(invoiceRepository.findByStatus("PENDING")).thenReturn(Arrays.asList(mockInvoices.get(2)));
        when(invoiceRepository.findByStatus("SENT")).thenReturn(new ArrayList<>());
        when(invoiceRepository.findByStatus("DRAFT")).thenReturn(new ArrayList<>());
        when(invoiceRepository.findByStatus("PAID")).thenReturn(Arrays.asList(mockInvoices.get(0)));
        when(conventionRepository.count()).thenReturn(10L);
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(Arrays.asList(mockConventions.get(0)));
        when(userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER)).thenReturn(mockUsers);

        service.checkKpiAnomalies();

        verify(invoiceRepository, atLeastOnce()).count();
        verify(invoiceRepository, atLeastOnce()).findByStatus(anyString());
    }

    @Test
    void testCheckKpiAnomalies_AllNormal() {
        when(invoiceRepository.count()).thenReturn(100L);
        when(invoiceRepository.findByStatus("OVERDUE")).thenReturn(new ArrayList<>());
        when(invoiceRepository.findByStatus("PENDING")).thenReturn(new ArrayList<>());
        when(invoiceRepository.findByStatus("SENT")).thenReturn(new ArrayList<>());
        when(invoiceRepository.findByStatus("DRAFT")).thenReturn(new ArrayList<>());
        when(invoiceRepository.findByStatus("PAID")).thenReturn(mockInvoices);
        when(conventionRepository.count()).thenReturn(100L);
        when(conventionRepository.findByStatus("ACTIVE")).thenReturn(mockConventions);

        service.checkKpiAnomalies();

        verify(invoiceRepository, atLeastOnce()).count();
    }

    @Test
    void testCheckKpiAnomalies_WithException() {
        when(invoiceRepository.count()).thenThrow(new RuntimeException("DB Error"));

        assertDoesNotThrow(() -> service.checkKpiAnomalies());
    }

    @Test
    void testCheckKpiAnomalies_NoProjectManagers() {
        when(invoiceRepository.count()).thenReturn(10L);
        when(invoiceRepository.findByStatus(anyString())).thenReturn(new ArrayList<>());
        when(conventionRepository.count()).thenReturn(10L);
        when(conventionRepository.findByStatus(anyString())).thenReturn(new ArrayList<>());
        when(userRepository.findByRoles_Name(ERole.ROLE_PROJECT_MANAGER)).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> service.checkKpiAnomalies());
    }
}
