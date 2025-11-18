package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.User;
import com.example.demo.model.Structure;
import com.example.demo.model.Application;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecideurServiceTest {

    @Mock
    private ConventionRepository conventionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private StructureRepository structureRepository;
    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private DecideurService service;

    private List<Convention> mockConventions;
    private List<Invoice> mockInvoices;
    private List<User> mockUsers;

    @BeforeEach
    void setUp() {
        mockConventions = createMockConventions();
        mockInvoices = createMockInvoices();
        mockUsers = createMockUsers();
    }

    private List<Convention> createMockConventions() {
        List<Convention> conventions = new ArrayList<>();
        
        Convention c1 = new Convention();
        c1.setId("1");
        c1.setReference("CONV-001");
        c1.setStatus("ACTIVE");
        c1.setAmount(BigDecimal.valueOf(10000));
        c1.setZoneGeographiqueId("Tunis");
        c1.setStructureId("struct1");
        c1.setApplicationId("app1");
        c1.setStartDate(LocalDate.now().minusMonths(2));
        c1.setEndDate(LocalDate.now().plusMonths(10));
        c1.setCreatedBy("user1");
        c1.setCreatedAt(LocalDate.now().minusMonths(2));
        c1.setPaymentStatus("COMPLETED");
        conventions.add(c1);
        
        Convention c2 = new Convention();
        c2.setId("2");
        c2.setReference("CONV-002");
        c2.setStatus("ACTIVE");
        c2.setAmount(BigDecimal.valueOf(15000));
        c2.setZoneGeographiqueId("Sfax");
        c2.setStructureId("struct2");
        c2.setApplicationId("app1");
        c2.setStartDate(LocalDate.now().minusMonths(1));
        c2.setEndDate(LocalDate.now().plusMonths(11));
        c2.setCreatedBy("user2");
        c2.setCreatedAt(LocalDate.now().minusMonths(1));
        c2.setPaymentStatus("PENDING");
        conventions.add(c2);
        
        Convention c3 = new Convention();
        c3.setId("3");
        c3.setReference("CONV-003");
        c3.setStatus("EXPIRED");
        c3.setAmount(BigDecimal.valueOf(8000));
        c3.setZoneGeographiqueId("Tunis");
        c3.setStructureId("struct1");
        c3.setApplicationId("app2");
        c3.setStartDate(LocalDate.now().minusMonths(12));
        c3.setEndDate(LocalDate.now().minusDays(5));
        c3.setCreatedBy("user1");
        c3.setCreatedAt(LocalDate.now().minusMonths(12));
        conventions.add(c3);
        
        return conventions;
    }

    private List<Invoice> createMockInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        
        Invoice i1 = new Invoice();
        i1.setId("inv1");
        i1.setInvoiceNumber("INV-001");
        i1.setStatus("PAID");
        i1.setAmount(BigDecimal.valueOf(10000));
        i1.setConventionId("1");
        invoices.add(i1);
        
        Invoice i2 = new Invoice();
        i2.setId("inv2");
        i2.setInvoiceNumber("INV-002");
        i2.setStatus("PENDING");
        i2.setAmount(BigDecimal.valueOf(15000));
        i2.setConventionId("2");
        invoices.add(i2);
        
        Invoice i3 = new Invoice();
        i3.setId("inv3");
        i3.setInvoiceNumber("INV-003");
        i3.setStatus("PAID");
        i3.setAmount(BigDecimal.valueOf(8000));
        i3.setConventionId("3");
        invoices.add(i3);
        
        return invoices;
    }

    private List<User> createMockUsers() {
        List<User> users = new ArrayList<>();
        
        User u1 = new User();
        u1.setUsername("user1");
        u1.setName("John Doe");
        users.add(u1);
        
        User u2 = new User();
        u2.setUsername("user2");
        u2.setName("Jane Smith");
        users.add(u2);
        
        return users;
    }

    @Test
    void testGetDashboardData() {
        when(conventionRepository.findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(
            anyString(), anyString(), anyString(), any(), any())).thenReturn(mockConventions);

        Map<String, Object> result = service.getDashboardData("app1", "Tunis", "struct1", null, null);

        assertNotNull(result);
        assertTrue(result.containsKey("totalAmount"));
        assertTrue(result.containsKey("activeConventions"));
        assertTrue(result.containsKey("paymentRate"));
        assertTrue(result.containsKey("pieChartData"));
        assertTrue(result.containsKey("barChartData"));
        assertTrue(result.containsKey("lineChartData"));
        assertTrue(result.containsKey("radarChartData"));
    }

    @Test
    void testGetConventionsWithFilters() {
        when(conventionRepository.findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(
            anyString(), anyString(), anyString(), any(), any())).thenReturn(mockConventions);

        List<Convention> result = service.getConventionsWithFilters(
            "Tunis", "struct1", "app1", LocalDate.now().minusMonths(3), LocalDate.now());

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testCalculateTotalAmount() {
        when(conventionRepository.findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(
            anyString(), anyString(), anyString(), any(), any())).thenReturn(mockConventions);

        BigDecimal result = service.calculateTotalAmount(
            "Tunis", "struct1", "app1", LocalDate.now().minusMonths(3), LocalDate.now());

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(33000), result);
    }

    @Test
    void testPrepareChartData() {
        when(conventionRepository.findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(
            anyString(), anyString(), anyString(), any(), any())).thenReturn(mockConventions);

        Map<String, BigDecimal> result = service.prepareChartData(
            "Tunis", "struct1", "app1", LocalDate.now().minusMonths(3), LocalDate.now());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testExportToPDF() throws Exception {
        when(conventionRepository.findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(
            anyString(), anyString(), anyString(), any(), any())).thenReturn(mockConventions);

        byte[] result = service.exportToPDF(
            "Tunis", "struct1", "app1", LocalDate.now().minusMonths(3), LocalDate.now());

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportToExcel() throws Exception {
        when(conventionRepository.findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(
            anyString(), anyString(), anyString(), any(), any())).thenReturn(mockConventions);

        byte[] result = service.exportToExcel(
            "Tunis", "struct1", "app1", LocalDate.now().minusMonths(3), LocalDate.now());

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGetTopCommercials() {
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(mockUsers.get(0)));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(mockUsers.get(1)));

        List<DecideurService.CommercialStats> result = service.getTopCommercials();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() <= 5);
        assertTrue(result.get(0).getPerformance() >= 0);
    }

    @Test
    void testGetTopCommercials_EmptyList() {
        when(conventionRepository.findAll()).thenReturn(new ArrayList<>());

        List<DecideurService.CommercialStats> result = service.getTopCommercials();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRepartitionParGouvernorat() {
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        List<DecideurService.RepartitionStats> result = service.getRepartitionParGouvernorat();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size()); // Tunis et Sfax
    }

    @Test
    void testGetRepartitionParStructure() {
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        List<DecideurService.StructureStats> result = service.getRepartitionParStructure();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getValue() > 0);
        assertNotNull(result.get(0).getColor());
    }

    @Test
    void testGetRepartitionParStructure_EmptyList() {
        when(conventionRepository.findAll()).thenReturn(new ArrayList<>());

        List<DecideurService.StructureStats> result = service.getRepartitionParStructure();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPerformanceData() {
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        List<DecideurService.PerformanceData> result = service.getPerformanceData();

        assertNotNull(result);
        assertEquals(6, result.size()); // 6 derniers mois
        assertNotNull(result.get(0).getMonth());
        assertNotNull(result.get(0).getValue());
    }

    @Test
    void testGetRecentActivities() {
        when(conventionRepository.findAll()).thenReturn(mockConventions);

        List<DecideurService.ActivityData> result = service.getRecentActivities();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).getIcon());
        assertNotNull(result.get(0).getTitle());
    }

    @Test
    void testGetRecentActivities_EmptyList() {
        when(conventionRepository.findAll()).thenReturn(new ArrayList<>());

        List<DecideurService.ActivityData> result = service.getRecentActivities();

        assertNotNull(result);
        assertFalse(result.isEmpty()); // Devrait avoir au moins un message par défaut
    }

    @Test
    void testGetStructures() {
        Structure s1 = new Structure();
        s1.setCode("STRUCT1");
        Structure s2 = new Structure();
        s2.setCode("STRUCT2");
        
        when(structureRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        List<String> result = service.getStructures();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("STRUCT1"));
        assertTrue(result.contains("STRUCT2"));
    }

    @Test
    void testGetApplications() {
        Application a1 = new Application();
        a1.setCode("APP1");
        Application a2 = new Application();
        a2.setCode("APP2");
        
        when(applicationRepository.findAll()).thenReturn(Arrays.asList(a1, a2));

        List<String> result = service.getApplications();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("APP1"));
        assertTrue(result.contains("APP2"));
    }

    @Test
    void testGetKPIs() {
        when(conventionRepository.findAll()).thenReturn(mockConventions);
        when(invoiceRepository.findAll()).thenReturn(mockInvoices);

        DecideurService.KPIData result = service.getKPIs();

        assertNotNull(result);
        assertEquals(3, result.getTotalConventions());
        assertEquals(2, result.getActiveConventions());
        assertEquals(BigDecimal.valueOf(18000), result.getTotalRevenue()); // 10000 + 8000 (PAID)
        assertEquals(1, result.getPendingInvoices());
        assertEquals(BigDecimal.valueOf(15000), result.getPendingAmount());
        assertEquals(2, result.getPaidInvoices());
        assertTrue(result.getPaymentRate() > 0);
    }

    @Test
    void testGetKPIs_EmptyData() {
        when(conventionRepository.findAll()).thenReturn(new ArrayList<>());
        when(invoiceRepository.findAll()).thenReturn(new ArrayList<>());

        DecideurService.KPIData result = service.getKPIs();

        assertNotNull(result);
        assertEquals(0, result.getTotalConventions());
        assertEquals(0, result.getActiveConventions());
        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
        assertEquals(0, result.getPendingInvoices());
        assertEquals(0.0, result.getPaymentRate());
    }
}
