package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.repository.ConventionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DecideurService {

    private final ConventionRepository conventionRepository;

    public Map<String, Object> getDashboardData(
        String applicationId, 
        String governorate, 
        String structureId,
        String startDate, 
        String endDate) {
        
        List<Convention> conventions = conventionRepository.findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(
            governorate, 
            structureId, 
            applicationId,
            startDate != null ? LocalDate.parse(startDate) : null,
            endDate != null ? LocalDate.parse(endDate) : null);

        // Calculate KPIs
        BigDecimal totalAmount = conventions.stream()
            .map(Convention::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        long activeConventions = conventions.stream()
            .filter(c -> "ACTIVE".equals(c.getStatus()))
            .count();
            
        double paymentRate = conventions.stream()
            .filter(c -> c.getPaymentStatus() != null)
            .mapToDouble(c -> "COMPLETED".equals(c.getPaymentStatus()) ? 1 : 0)
            .average().orElse(0);

        // Prepare data for charts
        Map<String, BigDecimal> pieData = conventions.stream()
            .collect(Collectors.groupingBy(
                Convention::getGovernorate,
                Collectors.reducing(BigDecimal.ZERO,
                    Convention::getAmount,
                    BigDecimal::add)));

        // Data for bar chart (amounts by month)
        Map<String, BigDecimal> barData = conventions.stream()
            .collect(Collectors.groupingBy(
                c -> c.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.reducing(BigDecimal.ZERO,
                    Convention::getAmount,
                    BigDecimal::add)));

        // Data for line chart (number of conventions by month)
        Map<String, Long> lineData = conventions.stream()
            .collect(Collectors.groupingBy(
                c -> c.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()));

        // Data for radar chart (distribution by structure)
        Map<String, BigDecimal> radarData = conventions.stream()
            .collect(Collectors.groupingBy(
                Convention::getStructureId,
                Collectors.reducing(BigDecimal.ZERO,
                    Convention::getAmount,
                    BigDecimal::add)));

        return Map.of(
            "totalAmount", totalAmount,
            "activeConventions", activeConventions,
            "paymentRate", paymentRate,
            "pieChartData", pieData,
            "barChartData", barData,
            "lineChartData", lineData,
            "radarChartData", radarData
        );
    }

    public List<Convention> getConventionsWithFilters(String zoneId, String structureId, String applicationId, 
        LocalDate startDate, LocalDate endDate) {
        return conventionRepository.findByZoneGeographiqueIdAndStructureIdAndApplicationIdAndStartDateBetween(
            zoneId, structureId, applicationId, startDate, endDate);
    }

    public BigDecimal calculateTotalAmount(String zoneId, String structureId, String applicationId, 
        LocalDate startDate, LocalDate endDate) {
        return getConventionsWithFilters(zoneId, structureId, applicationId, startDate, endDate)
            .stream()
            .map(Convention::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> prepareChartData(String zoneId, String structureId, String applicationId, 
        LocalDate startDate, LocalDate endDate) {
        return getConventionsWithFilters(zoneId, structureId, applicationId, startDate, endDate)
            .stream()
            .collect(Collectors.groupingBy(
                Convention::getZoneGeographiqueId,
                Collectors.reducing(BigDecimal.ZERO,
                    Convention::getAmount,
                    BigDecimal::add)
            ));
    }

    public byte[] exportToPDF(String zoneId, String structureId, String applicationId, 
        LocalDate startDate, LocalDate endDate) throws IOException {
        List<Convention> conventions = getConventionsWithFilters(zoneId, structureId, applicationId, startDate, endDate);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add title
        Paragraph title = new Paragraph("Convention Report")
            .setFontSize(20);
        document.add(title);

        // Add filters info
        document.add(new Paragraph("Applied Filters:"));
        document.add(new Paragraph("Zone: " + zoneId));
        document.add(new Paragraph("Structure: " + structureId));
        document.add(new Paragraph("Application: " + applicationId));
        document.add(new Paragraph("Period: " + startDate + " - " + endDate));

        // Create table
        Table table = new com.itextpdf.layout.element.Table(new float[]{1,1,1,1,1}).useAllAvailableWidth();
        
        // Add headers
        String[] headers = {"ID", "Start Date", "Amount", "Status", "Structure"};
        for (String header : headers) {
            com.itextpdf.layout.element.Cell pdfCell = new com.itextpdf.layout.element.Cell();
            pdfCell.add(new Paragraph(header));
            table.addCell(pdfCell);
        }

        // Add data
        for (Convention convention : conventions) {
            com.itextpdf.layout.element.Cell pdfCell = new com.itextpdf.layout.element.Cell();
            pdfCell.add(new Paragraph(convention.getId()));
            table.addCell(pdfCell);

            pdfCell = new com.itextpdf.layout.element.Cell();
            pdfCell.add(new Paragraph(convention.getStartDate().toString()));
            table.addCell(pdfCell);

            pdfCell = new com.itextpdf.layout.element.Cell();
            pdfCell.add(new Paragraph(convention.getAmount().toString()));
            table.addCell(pdfCell);

            pdfCell = new com.itextpdf.layout.element.Cell();
            pdfCell.add(new Paragraph(convention.getStatus()));
            table.addCell(pdfCell);

            pdfCell = new com.itextpdf.layout.element.Cell();
            pdfCell.add(new Paragraph(convention.getStructureId()));
            table.addCell(pdfCell);
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] exportToExcel(String zoneId, String structureId, String applicationId, 
        LocalDate startDate, LocalDate endDate) throws IOException {
        List<Convention> conventions = getConventionsWithFilters(zoneId, structureId, applicationId, startDate, endDate);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Conventions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Start Date", "Amount", "Status", "Structure", "Zone", "Application"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell excelCell = headerRow.createCell(i);
                excelCell.setCellValue(headers[i]);
            }

            // Add data rows
            int rowNum = 1;
            for (Convention convention : conventions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(convention.getId());
                row.createCell(1).setCellValue(convention.getStartDate().toString());
                row.createCell(2).setCellValue(convention.getAmount().doubleValue());
                row.createCell(3).setCellValue(convention.getStatus());
                row.createCell(4).setCellValue(convention.getStructureId());
                row.createCell(5).setCellValue(convention.getZoneGeographiqueId());
                row.createCell(6).setCellValue(convention.getApplicationId());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
