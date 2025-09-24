package com.example.demo.controller;

import com.example.demo.service.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;

@RestController
@RequestMapping("/api/factures")
public class FactureController {
    @Autowired
    private FactureService factureService;

    @GetMapping
    public List<Map<String, Object>> getFactures() {
        return factureService.getFactures();
    }

    @GetMapping("/{id}/preuve")
    public Map<String, Object> getPreuve(@PathVariable String id) {
        return factureService.getPreuve(id);
    }

    @GetMapping("/{id}/recu")
    public Map<String, Object> getRecu(@PathVariable String id) {
        return factureService.getRecu(id);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return factureService.getStats();
    }

    @GetMapping("/{id}/preuve-pdf")
    public ResponseEntity<byte[]> getPreuvePdf(@PathVariable String id) throws IOException {
        byte[] pdf = factureService.generatePreuvePdf(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=preuve-" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/{id}/recu-pdf")
    public ResponseEntity<byte[]> getRecuPdf(@PathVariable String id) throws IOException {
        byte[] pdf = factureService.generateRecuPdf(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=recu-" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
} 