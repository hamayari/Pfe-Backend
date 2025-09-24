package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.model.Invoice;
import com.example.demo.model.Structure;
import com.example.demo.model.Governorate;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.GovernorateRepository;
import com.example.demo.repository.ConventionRepository;
import com.example.demo.repository.InvoiceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class ExcelExportService {

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private GovernorateRepository governorateRepository;

    @Autowired
    private ConventionRepository conventionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private static final String COMPANY_NAME = "Cascade Solutions";
    private static final String COMPANY_ADDRESS = "123 Rue de l'Innovation, 1000 Tunis";
    private static final String COMPANY_PHONE = "+216 71 123 456";
    private static final String COMPANY_EMAIL = "contact@cascade-solutions.tn";

    public byte[] exportInvoicesToExcel(List<String> invoiceIds) throws IOException {
        // Génération à la demande - pas de stockage en base
        return generateInvoicesExcelContent(invoiceIds);
    }

    private byte[] generateInvoicesExcelContent(List<String> invoiceIds) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Factures");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Add company header
            addCompanyHeader(sheet, headerStyle);

            // Create headers
            Row headerRow = sheet.createRow(3);
            String[] headers = {
                "Numéro Facture", "Référence", "Montant", "Statut", "Date d'émission", 
                "Date d'échéance", "Structure", "Gouvernorat", "Convention liée", "Commentaires"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add data
            int rowNum = 4;
            for (String invoiceId : invoiceIds) {
                Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
                if (invoice != null) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : invoice.getId());
                    row.createCell(1).setCellValue(invoice.getReference() != null ? invoice.getReference() : "");
                    
                    Cell amountCell = row.createCell(2);
                    amountCell.setCellValue(invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0.0);
                    amountCell.setCellStyle(currencyStyle);
                    
                    row.createCell(3).setCellValue(getStatusLabel(invoice.getStatus()));
                    
                    Cell issueDateCell = row.createCell(4);
                    if (invoice.getIssueDate() != null) {
                        issueDateCell.setCellValue(invoice.getIssueDate());
                        issueDateCell.setCellStyle(dateStyle);
                    }
                    
                    Cell dueDateCell = row.createCell(5);
                    if (invoice.getDueDate() != null) {
                        dueDateCell.setCellValue(invoice.getDueDate());
                        dueDateCell.setCellStyle(dateStyle);
                    }

                    // Get convention details
                    Convention convention = null;
                    if (invoice.getConventionId() != null) {
                        convention = conventionRepository.findById(invoice.getConventionId()).orElse(null);
                    }

                    if (convention != null) {
                        Structure structure = structureRepository.findById(convention.getStructureId()).orElse(null);
                        row.createCell(6).setCellValue(structure != null ? structure.getLibelle() : convention.getStructureId());
                        
                        Governorate governorate = governorateRepository.findById(convention.getGovernorate()).orElse(null);
                        row.createCell(7).setCellValue(governorate != null ? governorate.getName() : convention.getGovernorate());
                        
                        row.createCell(8).setCellValue(convention.getReference());
                    } else {
                        row.createCell(6).setCellValue("");
                        row.createCell(7).setCellValue("");
                        row.createCell(8).setCellValue("");
                    }
                    
                    row.createCell(9).setCellValue(invoice.getComments() != null ? invoice.getComments() : "");
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary
            addInvoiceSummary(sheet, rowNum, headerStyle, currencyStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportConventionsToExcel(List<String> conventionIds) throws IOException {
        // Génération à la demande - pas de stockage en base
        return generateConventionsExcelContent(conventionIds);
    }

    private byte[] generateConventionsExcelContent(List<String> conventionIds) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Conventions");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Add company header
            addCompanyHeader(sheet, headerStyle);

            // Create headers
            Row headerRow = sheet.createRow(3);
            String[] headers = {
                "Référence", "Titre", "Structure", "Gouvernorat", "Montant", "Statut", 
                "Date de début", "Date de fin", "Modalité de paiement", "Tags"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add data
            int rowNum = 4;
            for (String conventionId : conventionIds) {
                Convention convention = conventionRepository.findById(conventionId).orElse(null);
                if (convention != null) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(convention.getReference());
                    row.createCell(1).setCellValue(convention.getTitle());
                    
                    Structure structure = structureRepository.findById(convention.getStructureId()).orElse(null);
                    row.createCell(2).setCellValue(structure != null ? structure.getLibelle() : convention.getStructureId());
                    
                    Governorate governorate = governorateRepository.findById(convention.getGovernorate()).orElse(null);
                    row.createCell(3).setCellValue(governorate != null ? governorate.getName() : convention.getGovernorate());
                    
                    Cell amountCell = row.createCell(4);
                    amountCell.setCellValue(convention.getAmount() != null ? convention.getAmount().doubleValue() : 0.0);
                    amountCell.setCellStyle(currencyStyle);
                    
                    row.createCell(5).setCellValue(convention.getStatus());
                    
                    Cell startDateCell = row.createCell(6);
                    if (convention.getStartDate() != null) {
                        startDateCell.setCellValue(convention.getStartDate());
                        startDateCell.setCellStyle(dateStyle);
                    }
                    
                    Cell endDateCell = row.createCell(7);
                    if (convention.getEndDate() != null) {
                        endDateCell.setCellValue(convention.getEndDate());
                        endDateCell.setCellStyle(dateStyle);
                    }
                    
                    row.createCell(8).setCellValue(getPaymentMethodLabel(convention));
                    
                    if (convention.getTag() != null && !convention.getTag().isEmpty()) {
                        row.createCell(9).setCellValue(convention.getTag());
                    } else {
                        row.createCell(9).setCellValue("");
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary
            addConventionSummary(sheet, rowNum, headerStyle, currencyStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void addCompanyHeader(Sheet sheet, CellStyle headerStyle) {
        // Company name
        Row companyRow = sheet.createRow(0);
        Cell companyCell = companyRow.createCell(0);
        companyCell.setCellValue(COMPANY_NAME);
        companyCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        // Company details
        Row detailsRow = sheet.createRow(1);
        detailsRow.createCell(0).setCellValue(COMPANY_ADDRESS);
        detailsRow.createCell(1).setCellValue("Tél: " + COMPANY_PHONE);
        detailsRow.createCell(2).setCellValue("Email: " + COMPANY_EMAIL);

        // Export date
        Row dateRow = sheet.createRow(2);
        dateRow.createCell(0).setCellValue("Export généré le: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void addInvoiceSummary(Sheet sheet, int startRow, CellStyle headerStyle, CellStyle currencyStyle) {
        Row summaryRow = sheet.createRow(startRow + 1);
        Cell summaryCell = summaryRow.createCell(0);
        summaryCell.setCellValue("RÉSUMÉ");
        summaryCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRow + 1, 0, 9));

        Row totalRow = sheet.createRow(startRow + 2);
        totalRow.createCell(0).setCellValue("Total des factures:");
        totalRow.createCell(1).setCellValue(startRow - 3); // Number of invoices

        // Calculate total amount
        double totalAmount = 0.0;
        for (int i = 4; i < startRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell amountCell = row.getCell(2);
                if (amountCell != null && amountCell.getNumericCellValue() != 0) {
                    totalAmount += amountCell.getNumericCellValue();
                }
            }
        }

        Cell totalAmountCell = totalRow.createCell(2);
        totalAmountCell.setCellValue(totalAmount);
        totalAmountCell.setCellStyle(currencyStyle);
    }

    private void addConventionSummary(Sheet sheet, int startRow, CellStyle headerStyle, CellStyle currencyStyle) {
        Row summaryRow = sheet.createRow(startRow + 1);
        Cell summaryCell = summaryRow.createCell(0);
        summaryCell.setCellValue("RÉSUMÉ");
        summaryCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRow + 1, 0, 9));

        Row totalRow = sheet.createRow(startRow + 2);
        totalRow.createCell(0).setCellValue("Total des conventions:");
        totalRow.createCell(1).setCellValue(startRow - 3); // Number of conventions

        // Calculate total amount
        double totalAmount = 0.0;
        for (int i = 4; i < startRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell amountCell = row.getCell(4);
                if (amountCell != null && amountCell.getNumericCellValue() != 0) {
                    totalAmount += amountCell.getNumericCellValue();
                }
            }
        }

        Cell totalAmountCell = totalRow.createCell(2);
        totalAmountCell.setCellValue(totalAmount);
        totalAmountCell.setCellStyle(currencyStyle);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00 TND"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));
        return style;
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Inconnu";
        switch (status.toUpperCase()) {
            case "PAID": return "Payée";
            case "PENDING": return "En attente";
            case "OVERDUE": return "En retard";
            default: return status;
        }
    }

    private String getPaymentMethodLabel(Convention convention) {
        if (convention.getPaymentTerms() == null) return "Non défini";
        return convention.getPaymentTerms().getPaymentMethod() != null ? 
               convention.getPaymentTerms().getPaymentMethod() : "Non défini";
    }
} 