package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.Structure;
import com.example.demo.model.Governorate;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {

    @Mock
    private StructureRepository structureRepository;

    @Mock
    private GovernorateRepository governorateRepository;

    @Mock
    private ConventionRepository conventionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ExcelExportService excelExportService;

    private Invoice mockInvoice;
    private Convention mockConvention;
    private Structure mockStructure;
    private Governorate mockGovernorate;

    @BeforeEach
    void setUp() {
        mockInvoice = new Invoice();
        mockInvoice.setId("inv1");
        mockInvoice.setInvoiceNumber("FACT001");
        mockInvoice.setAmount(BigDecimal.valueOf(1000));
        mockInvoice.setStatus("PAID");
        mockInvoice.setIssueDate(LocalDate.now());
        mockInvoice.setDueDate(LocalDate.now().plusDays(30));
        mockInvoice.setConventionId("conv1");

        mockConvention = new Convention();
        mockConvention.setId("conv1");
        mockConvention.setReference("REF001");
        mockConvention.setTitle("Test Convention");
        mockConvention.setAmount(BigDecimal.valueOf(5000));
        mockConvention.setStatus("ACTIVE");
        mockConvention.setStructureId("struct1");
        mockConvention.setGovernorate("gov1");
        mockConvention.setStartDate(LocalDate.now());
        mockConvention.setEndDate(LocalDate.now().plusMonths(6));

        mockStructure = new Structure();
        mockStructure.setId("struct1");
        mockStructure.setLibelle("Test Structure");

        mockGovernorate = new Governorate();
        mockGovernorate.setId("gov1");
        mockGovernorate.setName("Tunis");
    }

    @Test
    void testExportInvoicesToExcel() throws Exception {
        List<String> invoiceIds = Arrays.asList("inv1");
        
        when(invoiceRepository.findById("inv1")).thenReturn(Optional.of(mockInvoice));
        when(conventionRepository.findById("conv1")).thenReturn(Optional.of(mockConvention));
        when(structureRepository.findById("struct1")).thenReturn(Optional.of(mockStructure));
        when(governorateRepository.findById("gov1")).thenReturn(Optional.of(mockGovernorate));

        byte[] result = excelExportService.exportInvoicesToExcel(invoiceIds);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(invoiceRepository).findById("inv1");
    }

    @Test
    void testExportConventionsToExcel() throws Exception {
        List<String> conventionIds = Arrays.asList("conv1");
        
        when(conventionRepository.findById("conv1")).thenReturn(Optional.of(mockConvention));
        when(structureRepository.findById("struct1")).thenReturn(Optional.of(mockStructure));
        when(governorateRepository.findById("gov1")).thenReturn(Optional.of(mockGovernorate));

        byte[] result = excelExportService.exportConventionsToExcel(conventionIds);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(conventionRepository).findById("conv1");
    }

    @Test
    void testExportInvoicesToExcel_EmptyList() throws Exception {
        List<String> invoiceIds = Arrays.asList();

        byte[] result = excelExportService.exportInvoicesToExcel(invoiceIds);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportConventionsToExcel_EmptyList() throws Exception {
        List<String> conventionIds = Arrays.asList();

        byte[] result = excelExportService.exportConventionsToExcel(conventionIds);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportInvoicesToExcel_NotFound() throws Exception {
        List<String> invoiceIds = Arrays.asList("inv999");
        
        when(invoiceRepository.findById("inv999")).thenReturn(Optional.empty());

        byte[] result = excelExportService.exportInvoicesToExcel(invoiceIds);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportConventionsToExcel_NotFound() throws Exception {
        List<String> conventionIds = Arrays.asList("conv999");
        
        when(conventionRepository.findById("conv999")).thenReturn(Optional.empty());

        byte[] result = excelExportService.exportConventionsToExcel(conventionIds);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
