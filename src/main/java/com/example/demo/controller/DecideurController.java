package com.example.demo.controller;

import com.example.demo.service.DecideurService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/decideur")
public class DecideurController {
    
    private final DecideurService decideurService;
    
    public DecideurController(DecideurService decideurService) {
        this.decideurService = decideurService;
    }
    
    @GetMapping
    public ResponseEntity<?> getDashboardData(
        @RequestParam(required = false) String application,
        @RequestParam(required = false) String governorate,
        @RequestParam(required = false) String structure,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate) {
        
        // Implémentez la logique de récupération des données
        return ResponseEntity.ok(decideurService.getDashboardData(
            application, governorate, structure, startDate, endDate));
    }
    
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPDF(
        @RequestParam(required = false) String zone,
        @RequestParam(required = false) String structure,
        @RequestParam(required = false) String application,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) throws IOException {
        LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate) : null;
        
        byte[] pdfBytes = decideurService.exportToPDF(
            zone, structure, application, parsedStartDate, parsedEndDate);
            
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }
    
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
        @RequestParam(required = false) String zone,
        @RequestParam(required = false) String structure,
        @RequestParam(required = false) String application,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) throws IOException {
        LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate) : null;
        
        byte[] excelBytes = decideurService.exportToExcel(
            zone, structure, application, parsedStartDate, parsedEndDate);
            
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
