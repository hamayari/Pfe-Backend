package com.example.demo.controller;

import com.example.demo.model.ConventionHistory;
import com.example.demo.service.ConventionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller pour gérer l'historique des conventions
 */
@RestController
@RequestMapping("/api/convention-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ConventionHistoryController {

    private final ConventionHistoryService historyService;

    /**
     * Récupère l'historique complet d'une convention
     */
    @GetMapping("/convention/{conventionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'COMMERCIAL')")
    public ResponseEntity<List<ConventionHistory>> getConventionHistory(@PathVariable String conventionId) {
        List<ConventionHistory> history = historyService.getConventionHistory(conventionId);
        return ResponseEntity.ok(history);
    }

    /**
     * Récupère l'historique par référence de convention
     */
    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'COMMERCIAL')")
    public ResponseEntity<List<ConventionHistory>> getHistoryByReference(@PathVariable String reference) {
        List<ConventionHistory> history = historyService.getHistoryByReference(reference);
        return ResponseEntity.ok(history);
    }

    /**
     * Récupère l'historique des modifications d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<List<ConventionHistory>> getUserHistory(@PathVariable String userId) {
        List<ConventionHistory> history = historyService.getUserHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Compte le nombre de modifications d'une convention
     */
    @GetMapping("/convention/{conventionId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'COMMERCIAL')")
    public ResponseEntity<Map<String, Long>> countModifications(@PathVariable String conventionId) {
        long count = historyService.countModifications(conventionId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
