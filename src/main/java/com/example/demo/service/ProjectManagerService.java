package com.example.demo.service;

import com.example.demo.model.Convention;
import com.example.demo.repository.ConventionRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectManagerService {

    private final ConventionRepository conventionRepository;

    public ProjectManagerService(ConventionRepository conventionRepository) {
        this.conventionRepository = conventionRepository;
    }

    public List<Map<String, Object>> getConventionsWithFilters(String commercial, String governorate, String status) {
        return conventionRepository.findAll().stream()
            .filter(c -> commercial == null || c.getCommercial().equals(commercial))
            .filter(c -> governorate == null || c.getGovernorate().equals(governorate))
            .filter(c -> status == null || c.getStatus().equals(status))
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getLatePaymentsAndIssues() {
        return conventionRepository.findByPaymentStatus("LATE")
            .stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }

    public void addCommentToConvention(String conventionId, String comment) {
        conventionRepository.findById(conventionId).ifPresent(convention -> {
            convention.addComment(comment);
            conventionRepository.save(convention);
        });
    }

    public byte[] exportConventionsReport() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Implémentation d'export
        return outputStream.toByteArray();
    }

    private Map<String, Object> convertToMap(Convention convention) {
        return Map.of(
            "id", convention.getId(),
            "commercial", convention.getCommercial(),
            "governorate", convention.getGovernorate(),
            "status", convention.getStatus(),
            "paymentStatus", convention.getPaymentStatus(),
            "comments", convention.getComments()
        );
    }
}
