package com.example.demo.controller;

import com.example.demo.model.Status;
import com.example.demo.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/statuses")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", maxAge = 3600)
@PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN','SUPER_ADMIN','PROJECT_MANAGER','DECISION_MAKER')")
public class StatusController {
    @Autowired
    private StatusRepository statusRepository;

    @GetMapping
    public ResponseEntity<List<Status>> getAllStatuses() {
        return ResponseEntity.ok(statusRepository.findAll());
    }
} 