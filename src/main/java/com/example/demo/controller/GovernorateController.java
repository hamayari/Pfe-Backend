package com.example.demo.controller;

import com.example.demo.model.Governorate;
import com.example.demo.repository.GovernorateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/governorates")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", maxAge = 3600)
@PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN','SUPER_ADMIN')")
public class GovernorateController {
    @Autowired
    private GovernorateRepository governorateRepository;

    @GetMapping
    public ResponseEntity<List<Governorate>> getAllGovernorates() {
        return ResponseEntity.ok(governorateRepository.findAll());
    }
} 