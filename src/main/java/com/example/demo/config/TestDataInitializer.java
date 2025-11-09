package com.example.demo.config;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(10) // Exécuter après DataInitializer
public class TestDataInitializer implements CommandLineRunner {

    private final ConventionRepository conventionRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public void run(String... args) throws Exception {
        long conventionCount = conventionRepository.count();
        long invoiceCount = invoiceRepository.count();
        
        System.out.println("📊 État actuel de la base de données:");
        System.out.println("   - Conventions: " + conventionCount);
        System.out.println("   - Factures: " + invoiceCount);
        
        // Créer des conventions de test si la base est vide
        if (conventionCount == 0) {
            createTestConventions();
        } else {
            System.out.println("ℹ️ Des conventions existent déjà dans la base de données");
            System.out.println("💡 Pour réinitialiser, supprimez manuellement les collections dans MongoDB");
        }

        // Créer des factures de test si la base est vide
        if (invoiceCount == 0) {
            createTestInvoices();
        } else {
            System.out.println("ℹ️ Des factures existent déjà dans la base de données");
        }
    }

    private void createTestConventions() {
        List<Convention> conventions = new ArrayList<>();

        // Convention 1
        Convention conv1 = new Convention();
        conv1.setReference("CONV-2025-001");
        conv1.setStructureId("STR-001");
        conv1.setStatus("ACTIVE");
        conv1.setStartDate(LocalDate.now().minusMonths(2));
        conv1.setEndDate(LocalDate.now().plusMonths(10));
        conv1.setAmount(new BigDecimal("50000"));
        conventions.add(conv1);

        // Convention 2
        Convention conv2 = new Convention();
        conv2.setReference("CONV-2025-002");
        conv2.setStructureId("STR-002");
        conv2.setStatus("ACTIVE");
        conv2.setStartDate(LocalDate.now().minusMonths(1));
        conv2.setEndDate(LocalDate.now().plusMonths(11));
        conv2.setAmount(new BigDecimal("75000"));
        conventions.add(conv2);

        // Convention 3
        Convention conv3 = new Convention();
        conv3.setReference("CONV-2025-003");
        conv3.setStructureId("STR-003");
        conv3.setStatus("PENDING");
        conv3.setStartDate(LocalDate.now());
        conv3.setEndDate(LocalDate.now().plusYears(1));
        conv3.setAmount(new BigDecimal("100000"));
        conventions.add(conv3);

        // Convention 4
        Convention conv4 = new Convention();
        conv4.setReference("CONV-2024-045");
        conv4.setStructureId("STR-004");
        conv4.setStatus("EXPIRED");
        conv4.setStartDate(LocalDate.now().minusYears(1));
        conv4.setEndDate(LocalDate.now().minusMonths(1));
        conv4.setAmount(new BigDecimal("60000"));
        conventions.add(conv4);

        // Convention 5
        Convention conv5 = new Convention();
        conv5.setReference("CONV-2025-004");
        conv5.setStructureId("STR-005");
        conv5.setStatus("ACTIVE");
        conv5.setStartDate(LocalDate.now().minusMonths(3));
        conv5.setEndDate(LocalDate.now().plusMonths(9));
        conv5.setAmount(new BigDecimal("85000"));
        conventions.add(conv5);

        conventionRepository.saveAll(conventions);
        System.out.println("✅ " + conventions.size() + " conventions de test créées");
    }

    private void createTestInvoices() {
        List<Invoice> invoices = new ArrayList<>();

        // Facture 1 - Payée
        Invoice inv1 = new Invoice();
        inv1.setInvoiceNumber("INV-2025-001");
        inv1.setConventionId("CONV-2025-001");
        inv1.setAmount(new BigDecimal("10000"));
        inv1.setStatus("PAID");
        inv1.setIssueDate(LocalDate.now().minusMonths(2));
        inv1.setDueDate(LocalDate.now().minusMonths(1));
        inv1.setPaymentDate(LocalDate.now().minusMonths(1).plusDays(5));
        invoices.add(inv1);

        // Facture 2 - En attente
        Invoice inv2 = new Invoice();
        inv2.setInvoiceNumber("INV-2025-002");
        inv2.setConventionId("CONV-2025-002");
        inv2.setAmount(new BigDecimal("15000"));
        inv2.setStatus("PENDING");
        inv2.setIssueDate(LocalDate.now().minusDays(15));
        inv2.setDueDate(LocalDate.now().plusDays(15));
        invoices.add(inv2);

        // Facture 3 - En retard
        Invoice inv3 = new Invoice();
        inv3.setInvoiceNumber("INV-2025-003");
        inv3.setConventionId("CONV-2025-003");
        inv3.setAmount(new BigDecimal("20000"));
        inv3.setStatus("OVERDUE");
        inv3.setIssueDate(LocalDate.now().minusMonths(2));
        inv3.setDueDate(LocalDate.now().minusDays(10));
        invoices.add(inv3);

        // Facture 4 - Payée
        Invoice inv4 = new Invoice();
        inv4.setInvoiceNumber("INV-2025-004");
        inv4.setConventionId("CONV-2025-004");
        inv4.setAmount(new BigDecimal("17000"));
        inv4.setStatus("PAID");
        inv4.setIssueDate(LocalDate.now().minusMonths(1));
        inv4.setDueDate(LocalDate.now().minusDays(15));
        inv4.setPaymentDate(LocalDate.now().minusDays(10));
        invoices.add(inv4);

        // Facture 5 - En attente
        Invoice inv5 = new Invoice();
        inv5.setInvoiceNumber("INV-2025-005");
        inv5.setConventionId("CONV-2025-005");
        inv5.setAmount(new BigDecimal("12500"));
        inv5.setStatus("PENDING");
        inv5.setIssueDate(LocalDate.now().minusDays(5));
        inv5.setDueDate(LocalDate.now().plusDays(25));
        invoices.add(inv5);

        // Facture 6 - En retard
        Invoice inv6 = new Invoice();
        inv6.setInvoiceNumber("INV-2025-006");
        inv6.setConventionId("CONV-2025-001");
        inv6.setAmount(new BigDecimal("8000"));
        inv6.setStatus("OVERDUE");
        inv6.setIssueDate(LocalDate.now().minusMonths(1));
        inv6.setDueDate(LocalDate.now().minusDays(5));
        invoices.add(inv6);

        invoiceRepository.saveAll(invoices);
        System.out.println("✅ " + invoices.size() + " factures de test créées");
    }
}
