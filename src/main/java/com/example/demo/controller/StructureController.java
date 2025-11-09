package com.example.demo.controller;

import com.example.demo.model.Structure;
import com.example.demo.repository.StructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/structures")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true", maxAge = 3600)
public class StructureController {
    private static final Logger logger = LoggerFactory.getLogger(StructureController.class);
    
    @Autowired
    private StructureRepository structureRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN','SUPER_ADMIN','PROJECT_MANAGER','DECISION_MAKER')")
    public ResponseEntity<List<Structure>> getAllStructures() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("GET /api/structures - User: {}, Authorities: {}", 
            auth != null ? auth.getName() : "anonymous", 
            auth != null ? auth.getAuthorities() : "none");
        return ResponseEntity.ok(structureRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Structure> createStructure(@RequestBody Structure structure) {
        try {
            Structure savedStructure = structureRepository.save(structure);
            return ResponseEntity.ok(savedStructure);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN','SUPER_ADMIN','PROJECT_MANAGER','DECISION_MAKER')")
    public ResponseEntity<Structure> getStructureById(@PathVariable String id) {
        return structureRepository.findById(id)
                .map(structure -> ResponseEntity.ok(structure))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Structure> updateStructure(@PathVariable String id, @RequestBody Structure structure) {
        if (structureRepository.existsById(id)) {
            structure.setId(id);
            Structure updatedStructure = structureRepository.save(structure);
            return ResponseEntity.ok(updatedStructure);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteStructure(@PathVariable String id) {
        if (structureRepository.existsById(id)) {
            structureRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
} 