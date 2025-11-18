package com.example.demo.service;

import com.example.demo.dto.KPIMetricsDTO;
import com.example.demo.dto.ConventionStatsDTO;
import com.example.demo.dto.InvoiceStatsDTO;
import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CommercialDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(CommercialDashboardService.class);

    @Autowired
    private ConventionRepository conventionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    public KPIMetricsDTO getKPIMetrics(String userId, String startDate, String endDate, 
                                     String structureId, String governorate) {
        KPIMetricsDTO kpi = new KPIMetricsDTO();
        
        try {
            // ✅ CORRECTION : Filtrer par commercial (createdBy = userId)
            List<Convention> conventions = conventionRepository.findByCreatedBy(userId);
            List<Invoice> invoices = invoiceRepository.findByCreatedBy(userId);
        
        kpi.setTotalConventions(conventions.size());
        kpi.setActiveConventions((int) conventions.stream()
            .filter(c -> "ACTIVE".equals(c.getStatus())).count());
        kpi.setExpiredConventions((int) conventions.stream()
            .filter(c -> "EXPIRED".equals(c.getStatus())).count());
        
        kpi.setTotalInvoices(invoices.size());
        kpi.setPaidInvoices((int) invoices.stream()
            .filter(i -> "PAID".equals(i.getStatus())).count());
        kpi.setOverdueInvoices((int) invoices.stream()
            .filter(i -> "OVERDUE".equals(i.getStatus())).count());
        
        // Calculer le taux de recouvrement
        double totalAmount = invoices.stream().mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0).sum();
        double paidAmount = invoices.stream()
            .filter(i -> "PAID".equals(i.getStatus()))
            .mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0).sum();
        
        kpi.setCollectionRate(totalAmount > 0 ? (paidAmount / totalAmount) * 100 : 0);
        kpi.setPendingAmount(totalAmount - paidAmount);
        
        // Calculer le délai moyen de paiement
        double avgPaymentTime = invoices.stream()
            .filter(i -> "PAID".equals(i.getStatus()) && i.getPaymentDate() != null)
            .mapToDouble(i -> {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                    i.getDueDate(), i.getPaymentDate());
                return Math.max(0, days);
            })
            .average()
            .orElse(0);
        
        kpi.setAveragePaymentTime(avgPaymentTime);
        
        // Calculer les revenus mensuels
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        double monthlyRevenue = invoices.stream()
            .filter(i -> "PAID".equals(i.getStatus()) && 
                        i.getPaymentDate() != null &&
                        i.getPaymentDate().isAfter(monthStart))
            .mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0)
            .sum();
        
            kpi.setMonthlyRevenue(monthlyRevenue);
            
            return kpi;
        } catch (Exception e) {
            // En cas d'erreur, retourner des valeurs par défaut
            System.err.println("Erreur dans getKPIMetrics: " + e.getMessage());
            e.printStackTrace();
            
            kpi.setTotalConventions(0);
            kpi.setActiveConventions(0);
            kpi.setExpiredConventions(0);
            kpi.setTotalInvoices(0);
            kpi.setPaidInvoices(0);
            kpi.setOverdueInvoices(0);
            kpi.setCollectionRate(0.0);
            kpi.setAveragePaymentTime(0.0);
            kpi.setMonthlyRevenue(0.0);
            kpi.setPendingAmount(0.0);
            
            return kpi;
        }
    }

    public ConventionStatsDTO getConventionStats(String userId) {
        ConventionStatsDTO stats = new ConventionStatsDTO();
        try {
            // ✅ CORRECTION : Filtrer par commercial (createdBy = userId)
            List<Convention> conventions = conventionRepository.findByCreatedBy(userId);
        
        stats.setTotal(conventions.size());
        stats.setActive((int) conventions.stream()
            .filter(c -> "ACTIVE".equals(c.getStatus())).count());
        stats.setPending((int) conventions.stream()
            .filter(c -> "PENDING".equals(c.getStatus())).count());
        stats.setExpired((int) conventions.stream()
            .filter(c -> "EXPIRED".equals(c.getStatus())).count());
        
        // Statistiques par gouvernorat
        Map<String, Integer> byGovernorate = conventions.stream()
            .collect(Collectors.groupingBy(
                Convention::getGovernorate,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        stats.setByGovernorate(byGovernorate);
        
        // Statistiques par structure
        Map<String, Integer> byStructure = conventions.stream()
            .collect(Collectors.groupingBy(
                c -> c.getStructureId(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
            stats.setByStructure(byStructure);
            
            return stats;
        } catch (Exception e) {
            System.err.println("Erreur dans getConventionStats: " + e.getMessage());
            e.printStackTrace();
            
            stats.setTotal(0);
            stats.setActive(0);
            stats.setPending(0);
            stats.setExpired(0);
            stats.setByGovernorate(new HashMap<>());
            stats.setByStructure(new HashMap<>());
            
            return stats;
        }
    }

    public InvoiceStatsDTO getInvoiceStats(String userId) {
        InvoiceStatsDTO stats = new InvoiceStatsDTO();
        try {
            // ✅ CORRECTION : Filtrer par commercial (createdBy = userId)
            List<Invoice> invoices = invoiceRepository.findByCreatedBy(userId);
        
        stats.setTotal(invoices.size());
        stats.setPaid((int) invoices.stream()
            .filter(i -> "PAID".equals(i.getStatus())).count());
        stats.setPending((int) invoices.stream()
            .filter(i -> "PENDING".equals(i.getStatus())).count());
        stats.setOverdue((int) invoices.stream()
            .filter(i -> "OVERDUE".equals(i.getStatus())).count());
        
        stats.setTotalAmount(invoices.stream().mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0).sum());
        stats.setPaidAmount(invoices.stream()
            .filter(i -> "PAID".equals(i.getStatus()))
            .mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0).sum());
        stats.setPendingAmount(invoices.stream()
            .filter(i -> "PENDING".equals(i.getStatus()))
            .mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0).sum());
            stats.setOverdueAmount(invoices.stream()
                .filter(i -> "OVERDUE".equals(i.getStatus()))
                .mapToDouble(i -> i.getAmount() != null ? i.getAmount().doubleValue() : 0.0).sum());
            
            return stats;
        } catch (Exception e) {
            System.err.println("Erreur dans getInvoiceStats: " + e.getMessage());
            e.printStackTrace();
            
            stats.setTotal(0);
            stats.setPaid(0);
            stats.setPending(0);
            stats.setOverdue(0);
            stats.setTotalAmount(0.0);
            stats.setPaidAmount(0.0);
            stats.setPendingAmount(0.0);
            stats.setOverdueAmount(0.0);
            
            return stats;
        }
    }

    public List<Convention> getConventionsByDeadline(String userId, int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return conventionRepository.findByCreatedByAndDueDateBefore(userId, deadline);
    }

    public List<Convention> getExpiredConventions(String userId) {
        return conventionRepository.findByCreatedByAndStatus(userId, "EXPIRED");
    }

    public List<Invoice> getOverdueInvoices(String userId) {
        try {
            return invoiceRepository.findByCreatedByAndStatusAndDueDateBefore(
                userId, "PENDING", LocalDate.now());
        } catch (Exception e) {
            // Fallback: récupérer toutes les factures et filtrer
            return invoiceRepository.findByCreatedBy(userId).stream()
                .filter(i -> "PENDING".equals(i.getStatus()) && 
                           i.getDueDate() != null && 
                           i.getDueDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        }
    }

    public List<Invoice> getUpcomingInvoices(String userId, int days) {
        try {
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().plusDays(days);
            return invoiceRepository.findByCreatedByAndDueDateBetween(userId, start, end);
        } catch (Exception e) {
            // Fallback: récupérer toutes les factures et filtrer
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().plusDays(days);
            return invoiceRepository.findByCreatedBy(userId).stream()
                .filter(i -> i.getDueDate() != null && 
                           !i.getDueDate().isBefore(start) && 
                           !i.getDueDate().isAfter(end))
                .collect(Collectors.toList());
        }
    }

    public List<Convention> searchConventions(String userId, String reference, String title,
                                            String structureId, String governorate, String status,
                                            String startDate, String endDate, String tags,
                                            Double amountMin, Double amountMax) {
        // ✅ CORRECTION : Filtrer par commercial (createdBy = userId)
        List<Convention> conventions = conventionRepository.findByCreatedBy(userId);
        
        return conventions.stream()
            .filter(c -> reference == null || c.getReference().contains(reference))
            .filter(c -> title == null || c.getTitle().contains(title))
            .filter(c -> structureId == null || structureId.equals(c.getStructureId()))
            .filter(c -> governorate == null || governorate.equals(c.getGovernorate()))
            .filter(c -> status == null || status.equals(c.getStatus()))
            .filter(c -> amountMin == null || c.getAmount().compareTo(BigDecimal.valueOf(amountMin)) >= 0)
            .filter(c -> amountMax == null || c.getAmount().compareTo(BigDecimal.valueOf(amountMax)) <= 0)
            .collect(Collectors.toList());
    }

    public List<Invoice> searchInvoices(String userId, String invoiceNumber, String conventionId,
                                      String status, String startDate, String endDate,
                                      Double amountMin, Double amountMax, String dueDateFrom,
                                      String dueDateTo) {
        // ✅ CORRECTION : Filtrer par commercial (createdBy = userId)
        List<Invoice> invoices = invoiceRepository.findByCreatedBy(userId);
        
        return invoices.stream()
            .filter(i -> invoiceNumber == null || i.getInvoiceNumber().contains(invoiceNumber))
            .filter(i -> conventionId == null || conventionId.equals(i.getConventionId()))
            .filter(i -> status == null || status.equals(i.getStatus()))
            .filter(i -> amountMin == null || i.getAmount().compareTo(BigDecimal.valueOf(amountMin)) >= 0)
            .filter(i -> amountMax == null || i.getAmount().compareTo(BigDecimal.valueOf(amountMax)) <= 0)
            .collect(Collectors.toList());
    }

    public static class InvoiceBatchResult {
        private List<Invoice> invoices;
        private List<String> errors;
        public InvoiceBatchResult(List<Invoice> invoices, List<String> errors) {
            this.invoices = invoices;
            this.errors = errors;
        }
        public List<Invoice> getInvoices() { return invoices; }
        public List<String> getErrors() { return errors; }
    }

    public InvoiceBatchResult generateInvoicesBatch(String userId, List<String> conventionIds,
                                             String dueDate, Integer paymentTerms, Boolean sendEmail) {
        List<Invoice> generatedInvoices = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try {
            for (String conventionId : conventionIds) {
                Convention convention = conventionRepository.findById(conventionId).orElse(null);
                if (convention == null) {
                    errors.add("Convention introuvable : " + conventionId);
                    continue;
                }
                if (convention.getAmount() == null || convention.getReference() == null) {
                    errors.add("Champs amount ou reference manquants pour la convention : " + conventionId);
                    continue;
                }
                if (convention.getStructureId() == null || convention.getStructureId().isEmpty()) {
                    errors.add("Champ structureId manquant pour la convention : " + conventionId);
                    continue;
                }
                
                // Vérifier qu'une facture n'existe pas déjà pour cette convention
                List<Invoice> existingInvoices = invoiceRepository.findByConventionId(conventionId);
                if (!existingInvoices.isEmpty()) {
                    errors.add("Une facture existe déjà pour la convention : " + convention.getReference());
                    continue;
                }
                
                Invoice invoice = new Invoice();
                invoice.setConventionId(conventionId);
                invoice.setAmount(convention.getAmount());
                invoice.setInvoiceNumber("FACT-" + convention.getReference() + "-" + System.currentTimeMillis());
                invoice.setStatus("PENDING");
                invoice.setDueDate(dueDate != null ? LocalDate.parse(dueDate) : LocalDate.now().plusDays(paymentTerms != null ? paymentTerms : 30));
                invoice.setCreatedBy(userId);
                invoice.setCreatedAt(LocalDate.now());
                Invoice savedInvoice = invoiceRepository.save(invoice);
                generatedInvoices.add(savedInvoice);
                
                // 🔔 NOTIFICATION AUTOMATIQUE - Facture générée
                try {
                    NotificationDTO notification = new NotificationDTO();
                    notification.setType("info");
                    notification.setTitle("📄 Nouvelle Facture Générée");
                    notification.setMessage("Facture " + savedInvoice.getInvoiceNumber() + " générée pour la convention " + convention.getReference() + " (Montant: " + savedInvoice.getAmount() + "€)");
                    notification.setPriority("medium");
                    notification.setCategory("invoice");
                    notification.setUserId(userId);
                    notification.setSource("CommercialDashboardService");
                    
                    realTimeNotificationService.createNotification(notification);
                    System.out.println("🔔 Notification envoyée pour la génération de facture " + savedInvoice.getInvoiceNumber());
                } catch (Exception e) {
                    System.err.println("❌ Erreur envoi notification génération facture: " + e.getMessage());
                }
                
                // Optionnel : envoyer email si demandé
                // ...
            }
        } catch (Exception e) {
            errors.add("Erreur technique : " + e.getMessage());
            e.printStackTrace();
        }
        return new InvoiceBatchResult(generatedInvoices, errors);
    }

    public byte[] exportConventions(String userId, String format, String reference,
                                  String title, String structureId, String governorate, String status) {
        List<Convention> conventions = searchConventions(userId, reference, title,
            structureId, governorate, status, null, null, null, null, null);
        
        if ("excel".equalsIgnoreCase(format)) {
            return exportConventionsToExcel(conventions);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return exportConventionsToPDF(conventions);
        } else if ("csv".equalsIgnoreCase(format)) {
            return exportConventionsToCSV(conventions);
        }
        
        return new byte[0];
    }
    
    private byte[] exportConventionsToExcel(List<Convention> conventions) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Conventions");
            
            // Style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // En-tête
            Row header = sheet.createRow(0);
            String[] columns = {"Référence", "Titre", "Structure", "Gouvernorat", "Statut", "Montant (DT)", "Date Création"};
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Données
            int rowIdx = 1;
            for (Convention c : conventions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getReference());
                row.createCell(1).setCellValue(c.getTitle());
                row.createCell(2).setCellValue(c.getStructureId());
                row.createCell(3).setCellValue(c.getGovernorate());
                row.createCell(4).setCellValue(c.getStatus());
                row.createCell(5).setCellValue(c.getAmount().doubleValue());
                row.createCell(6).setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    private byte[] exportConventionsToPDF(List<Convention> conventions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(out);
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf, com.itextpdf.kernel.geom.PageSize.A4.rotate());
            
            // Titre
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph("Rapport des Conventions")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(title);
            
            // Date de génération
            com.itextpdf.layout.element.Paragraph date = new com.itextpdf.layout.element.Paragraph(
                "Généré le " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(10)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(date);
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));
            
            // Tableau
            float[] columnWidths = {2, 3, 2, 2, 2, 2, 2};
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(columnWidths);
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            
            // En-tête du tableau
            String[] headers = {"Référence", "Titre", "Structure", "Gouvernorat", "Statut", "Montant", "Date"};
            for (String header : headers) {
                table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new com.itextpdf.layout.element.Paragraph(header))
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                    .setBold());
            }
            
            // Données
            for (Convention c : conventions) {
                table.addCell(c.getReference());
                table.addCell(c.getTitle());
                table.addCell(c.getStructureId());
                table.addCell(c.getGovernorate());
                table.addCell(c.getStatus());
                table.addCell(String.format("%.2f DT", c.getAmount()));
                table.addCell(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    private byte[] exportConventionsToCSV(List<Convention> conventions) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Référence,Titre,Structure,Gouvernorat,Statut,Montant,Date\n");
            
            for (Convention c : conventions) {
                csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\"\n",
                    c.getReference(),
                    c.getTitle(),
                    c.getStructureId(),
                    c.getGovernorate(),
                    c.getStatus(),
                    c.getAmount(),
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""));
            }
            
            return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public byte[] exportInvoices(String userId, String format, String invoiceNumber,
                               String conventionId, String status) {
        List<Invoice> invoices = searchInvoices(userId, invoiceNumber, conventionId,
            status, null, null, null, null, null, null);
        
        if ("excel".equalsIgnoreCase(format)) {
            return exportInvoicesToExcel(invoices);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return exportInvoicesToPDF(invoices);
        } else if ("csv".equalsIgnoreCase(format)) {
            return exportInvoicesToCSV(invoices);
        }
        
        return new byte[0];
    }
    
    private byte[] exportInvoicesToExcel(List<Invoice> invoices) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Factures");
            
            // Style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // En-tête
            Row header = sheet.createRow(0);
            String[] columns = {"N° Facture", "Convention", "Statut", "Montant (DT)", "Échéance", "Date Création"};
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Données
            int rowIdx = 1;
            for (Invoice inv : invoices) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(inv.getInvoiceNumber());
                row.createCell(1).setCellValue(inv.getConventionId());
                row.createCell(2).setCellValue(inv.getStatus());
                row.createCell(3).setCellValue(inv.getAmount().doubleValue());
                row.createCell(4).setCellValue(inv.getDueDate() != null ? inv.getDueDate().toString() : "");
                row.createCell(5).setCellValue(inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : "");
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    private byte[] exportInvoicesToPDF(List<Invoice> invoices) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(out);
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf, com.itextpdf.kernel.geom.PageSize.A4.rotate());
            
            // Titre
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph("Rapport des Factures")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(title);
            
            // Date de génération
            com.itextpdf.layout.element.Paragraph date = new com.itextpdf.layout.element.Paragraph(
                "Généré le " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(10)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(date);
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));
            
            // Tableau
            float[] columnWidths = {2, 2, 2, 2, 2, 2};
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(columnWidths);
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            
            // En-tête du tableau
            String[] headers = {"N° Facture", "Convention", "Statut", "Montant", "Échéance", "Date"};
            for (String header : headers) {
                table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new com.itextpdf.layout.element.Paragraph(header))
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                    .setBold());
            }
            
            // Données
            for (Invoice inv : invoices) {
                table.addCell(inv.getInvoiceNumber());
                table.addCell(inv.getConventionId());
                table.addCell(inv.getStatus());
                table.addCell(String.format("%.2f DT", inv.getAmount()));
                table.addCell(inv.getDueDate() != null ? inv.getDueDate().toString() : "");
                table.addCell(inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : "");
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    private byte[] exportInvoicesToCSV(List<Invoice> invoices) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("N° Facture,Convention,Statut,Montant,Échéance,Date\n");
            
            for (Invoice inv : invoices) {
                csv.append(String.format("\"%s\",\"%s\",\"%s\",%.2f,\"%s\",\"%s\"\n",
                    inv.getInvoiceNumber(),
                    inv.getConventionId(),
                    inv.getStatus(),
                    inv.getAmount(),
                    inv.getDueDate() != null ? inv.getDueDate().toString() : "",
                    inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : ""));
            }
            
            return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public Convention addTagToConvention(String userId, String conventionId, String tag) {
        Convention convention = conventionRepository.findById(conventionId).orElse(null);
        if (convention != null && convention.getCreatedBy().equals(userId)) {
            String tags = convention.getTag();
            if (tags == null) {
                tags = tag;
            } else {
                tags += "," + tag;
            }
            convention.setTag(tags);
            return conventionRepository.save(convention);
        }
        return convention;
    }

    public Convention removeTagFromConvention(String userId, String conventionId, String tag) {
        Convention convention = conventionRepository.findById(conventionId).orElse(null);
        if (convention != null && convention.getCreatedBy().equals(userId)) {
            String tags = convention.getTag();
            if (tags != null) {
                tags = tags.replace(tag, "").trim();
                if (tags.isEmpty()) {
                    tags = null;
                }
                convention.setTag(tags);
                return conventionRepository.save(convention);
            }
        }
        return convention;
    }

    public List<Map<String, Object>> getConventionAudit(String userId, String conventionId) {
        // Exemple d'audit simulé
        List<Map<String, Object>> audit = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("date", LocalDate.now().toString());
        entry.put("action", "Création");
        entry.put("user", userId);
        audit.add(entry);
        return audit;
    }

    public Convention duplicateConvention(String userId, String conventionId, Map<String, Object> newData) {
        Convention original = conventionRepository.findById(conventionId).orElse(null);
        if (original != null && original.getCreatedBy().equals(userId)) {
            Convention duplicate = new Convention();
            duplicate.setTitle(original.getTitle() + " (Copie)");
            duplicate.setReference(original.getReference() + "_COPY");
            duplicate.setAmount(original.getAmount());
            duplicate.setStructureId(original.getStructureId());
            duplicate.setGovernorate(original.getGovernorate());
            duplicate.setStatus("PENDING");
            duplicate.setCreatedBy(userId);
            
            return conventionRepository.save(duplicate);
        }
        return null;
    }

    public List<Convention> updateConventionsStatus(String userId, List<String> conventionIds, String status) {
        List<Convention> updatedConventions = new ArrayList<>();
        
        for (String conventionId : conventionIds) {
            Convention convention = conventionRepository.findById(conventionId).orElse(null);
            if (convention != null && convention.getCreatedBy().equals(userId)) {
                convention.setStatus(status);
                updatedConventions.add(conventionRepository.save(convention));
            }
        }
        
        return updatedConventions;
    }

    public LocalDate calculateDueDate(String userId, String conventionId, int paymentTerms) {
        Convention convention = conventionRepository.findById(conventionId).orElse(null);
        if (convention != null && convention.getCreatedBy().equals(userId)) {
            return LocalDate.now().plusDays(paymentTerms);
        }
        return LocalDate.now();
    }


    @Autowired
    private RealTimeNotificationService realTimeNotificationService;
    
    @Autowired
    private EmailService emailService;

    public void sendInvoiceByEmail(String userId, String invoiceId, String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("L'email du client est obligatoire pour l'envoi de la facture.");
        }
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            if (invoice == null) {
                throw new ResourceNotFoundException("Facture introuvable");
            }
            if (!invoice.getCreatedBy().equals(userId)) {
                throw new UnauthorizedException("Vous n'êtes pas autorisé à envoyer cette facture.");
            }
            // Mise à jour de l'invoice avec les informations d'envoi
            invoice.setClientEmail(email);
            invoice.setSentToClientAt(LocalDateTime.now());
            invoice.setSentBy(userId);
            invoiceRepository.save(invoice);
            
            // Envoi de l'email au client
            try {
                Map<String, String> variables = new HashMap<>();
                variables.put("invoiceNumber", invoice.getInvoiceNumber());
                variables.put("amount", String.valueOf(invoice.getAmount()));
                variables.put("dueDate", invoice.getDueDate() != null ? invoice.getDueDate().toString() : "N/A");
                
                emailService.sendInvoiceReminderEmail(email, variables);
                logger.info("Facture {} envoyée à {}", invoice.getInvoiceNumber(), email);
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi de l'email de facture: {}", e.getMessage(), e);
            }

            // Notification automatique - Facture envoyée
            try {
                NotificationDTO notification = new NotificationDTO();
                notification.setType("info");
                notification.setTitle("Facture Envoyée");
                notification.setMessage("Facture " + invoice.getInvoiceNumber() + " envoyée à " + email + " (Montant: " + invoice.getAmount() + "€)");
                notification.setPriority("medium");
                notification.setCategory("invoice");
                notification.setUserId(userId);
                notification.setSource("CommercialDashboardService");
                
                realTimeNotificationService.createNotification(notification);
                logger.info("Notification envoyée pour l'envoi de facture {}", invoice.getInvoiceNumber());
            } catch (Exception e) {
                logger.error("Erreur envoi notification facture: {}", e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la facture par email: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de la facture par email: " + e.getMessage());
        }
    }

    public byte[] generateInvoicePDF(String userId, String invoiceId) {
        // Simulation PDF (à remplacer par iText ou autre)
        String content = "PDF Facture: " + invoiceId;
        return content.getBytes();
    }

    public Invoice markInvoiceAsPaid(String userId, String invoiceId, String paymentDate, String paymentMethod) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice != null && invoice.getCreatedBy().equals(userId)) {
            invoice.setStatus("PAID");
            if (paymentDate != null) {
                invoice.setPaymentDate(LocalDate.parse(paymentDate));
            } else {
                invoice.setPaymentDate(LocalDate.now());
            }
            return invoiceRepository.save(invoice);
        }
        return invoice;
    }

    public Invoice recordPartialPayment(String userId, String invoiceId, Double amount, String paymentDate) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice != null && invoice.getCreatedBy().equals(userId)) {
            // Logique de paiement partiel : on ajoute le montant payé à un champ "partialPaidAmount"
            BigDecimal currentPartial = invoice.getPartialPaidAmount() != null ? invoice.getPartialPaidAmount() : BigDecimal.ZERO;
            BigDecimal addAmount = amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO;
            invoice.setPartialPaidAmount(currentPartial.add(addAmount));
            if (paymentDate != null) {
                invoice.setPaymentDate(LocalDate.parse(paymentDate));
            }
            // Si le montant total est atteint, on passe la facture à "PAID"
            if (invoice.getPartialPaidAmount().compareTo(invoice.getAmount()) >= 0) {
                invoice.setStatus("PAID");
            }
            return invoiceRepository.save(invoice);
        }
        return invoice;
    }

    public void deleteInvoice(String userId, String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice == null) {
            throw new ResourceNotFoundException("Invoice not found");
        }
        if (!userId.equals(invoice.getCreatedBy())) {
            throw new UnauthorizedException("Unauthorized to delete this invoice");
        }
        invoiceRepository.deleteById(invoiceId);
    }

    public Invoice getInvoiceById(String id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }
    public void saveInvoice(Invoice invoice) {
        invoiceRepository.save(invoice);
    }
} 