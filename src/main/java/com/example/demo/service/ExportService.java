package com.example.demo.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    public byte[] exportLogsToPDF(List<Map<String, String>> logs) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre
            Paragraph title = new Paragraph("Rapport de Monitoring Système")
                .setFontSize(20);
            document.add(title);

            // Date de génération
            Paragraph date = new Paragraph("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            document.add(date);

            document.add(new Paragraph("\n"));

            // Tableau des logs
            Table table = new Table(new float[]{1, 2}).useAllAvailableWidth();

            // En-têtes
            table.addCell(new Cell().add(new Paragraph("Date/Heure")));
            table.addCell(new Cell().add(new Paragraph("Événement")));

            // Données
            for (Map<String, String> log : logs) {
                table.addCell(new Cell().add(new Paragraph(log.get("date") != null ? log.get("date") : "")));
                table.addCell(new Cell().add(new Paragraph(log.get("event") != null ? log.get("event") : "")));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    public String exportLogsToCSV(List<Map<String, String>> logs) {
        StringBuilder csv = new StringBuilder();
        
        // En-têtes
        csv.append("Date/Heure,Événement,Niveau\n");
        
        // Données
        for (Map<String, String> log : logs) {
            String date = log.get("date") != null ? log.get("date") : "";
            String event = log.get("event") != null ? log.get("event") : "";
            String level = log.get("level") != null ? log.get("level") : "";
            
            // Échapper les virgules dans les valeurs
            csv.append("\"").append(date).append("\",");
            csv.append("\"").append(event).append("\",");
            csv.append("\"").append(level).append("\"\n");
        }
        
        return csv.toString();
    }

    public byte[] exportSystemStatsToPDF(Map<String, Object> stats) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre
            Paragraph title = new Paragraph("Statistiques Système")
                .setFontSize(20);
            document.add(title);

            // Date de génération
            Paragraph date = new Paragraph("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            document.add(date);

            document.add(new Paragraph("\n"));

            // Tableau des statistiques
            Table table = new Table(new float[]{1, 1}).useAllAvailableWidth();

            // En-têtes
            table.addCell(new Cell().add(new Paragraph("Métrique")));
            table.addCell(new Cell().add(new Paragraph("Valeur")));

            // Données
            table.addCell(new Cell().add(new Paragraph("CPU Usage")));
            table.addCell(new Cell().add(new Paragraph(stats.get("cpuUsage") + "%")));

            table.addCell(new Cell().add(new Paragraph("RAM Usage")));
            table.addCell(new Cell().add(new Paragraph(stats.get("ramUsage") + "%")));

            table.addCell(new Cell().add(new Paragraph("Disk Space")));
            table.addCell(new Cell().add(new Paragraph(stats.get("diskSpace") + "%")));

            table.addCell(new Cell().add(new Paragraph("Uptime")));
            table.addCell(new Cell().add(new Paragraph(formatUptime((Long) stats.get("uptime")))));

            table.addCell(new Cell().add(new Paragraph("Java Version")));
            table.addCell(new Cell().add(new Paragraph((String) stats.get("javaVersion"))));

            table.addCell(new Cell().add(new Paragraph("OS Name")));
            table.addCell(new Cell().add(new Paragraph((String) stats.get("osName"))));

            table.addCell(new Cell().add(new Paragraph("Total Memory")));
            table.addCell(new Cell().add(new Paragraph((String) stats.get("totalMemory"))));

            table.addCell(new Cell().add(new Paragraph("Free Memory")));
            table.addCell(new Cell().add(new Paragraph((String) stats.get("freeMemory"))));

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    private String formatUptime(long uptime) {
        long days = uptime / (24 * 60 * 60 * 1000);
        long hours = (uptime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (uptime % (60 * 60 * 1000)) / (60 * 1000);
        
        return days + "d " + hours + "h " + minutes + "m";
    }
} 