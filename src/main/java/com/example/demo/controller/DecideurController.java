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
@CrossOrigin(origins = "*")
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
    
    @GetMapping("/top-commercials")
    public ResponseEntity<?> getTopCommercials() {
        System.out.println("========================================");
        System.out.println("🏆 [GET TOP COMMERCIALS] Endpoint appelé");
        try {
            var result = decideurService.getTopCommercials();
            System.out.println("✅ Résultat: " + result.size() + " commerciaux");
            System.out.println("========================================");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            throw e;
        }
    }
    
    @GetMapping("/repartition-gouvernorat")
    public ResponseEntity<?> getRepartitionGouvernorat() {
        System.out.println("📊 [GET REPARTITION GOUVERNORAT] Endpoint appelé");
        try {
            var result = decideurService.getRepartitionParGouvernorat();
            System.out.println("✅ Résultat: " + result.size() + " gouvernorats");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/repartition-structure")
    public ResponseEntity<?> getRepartitionStructure() {
        System.out.println("📊 [GET REPARTITION STRUCTURE] Endpoint appelé");
        try {
            var result = decideurService.getRepartitionParStructure();
            System.out.println("✅ Résultat: " + result.size() + " structures");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/performance")
    public ResponseEntity<?> getPerformanceData() {
        System.out.println("📈 [GET PERFORMANCE] Endpoint appelé");
        try {
            var result = decideurService.getPerformanceData();
            System.out.println("✅ Résultat: " + result.size() + " mois");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/recent-activities")
    public ResponseEntity<?> getRecentActivities() {
        System.out.println("🔔 [GET RECENT ACTIVITIES] Endpoint appelé");
        try {
            var result = decideurService.getRecentActivities();
            System.out.println("✅ Résultat: " + result.size() + " activités");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/kpis")
    public ResponseEntity<?> getKPIs() {
        System.out.println("📊 [GET KPIS] Endpoint appelé");
        try {
            var result = decideurService.getKPIs();
            System.out.println("✅ KPIs: " + result.getTotalConventions() + " conventions, " + 
                             result.getPendingInvoices() + " factures en attente");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/structures")
    public ResponseEntity<?> getStructures() {
        System.out.println("📋 [GET STRUCTURES] Endpoint appelé");
        try {
            var result = decideurService.getStructures();
            System.out.println("✅ Résultat: " + result.size() + " structures");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/applications")
    public ResponseEntity<?> getApplications() {
        System.out.println("📋 [GET APPLICATIONS] Endpoint appelé");
        try {
            var result = decideurService.getApplications();
            System.out.println("✅ Résultat: " + result.size() + " applications");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
