package com.example.demo.controller;

import com.example.demo.model.ZoneGeographique;
import com.example.demo.repository.ZoneGeographiqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/zones-geographiques")
@CrossOrigin(origins = "*")
public class ZoneGeographiqueController {

    @Autowired
    private ZoneGeographiqueRepository zoneGeographiqueRepository;

    // Récupérer toutes les zones géographiques
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','COMMERCIAL','PROJECT_MANAGER','DECISION_MAKER')")
    public ResponseEntity<List<ZoneGeographique>> getAllZonesGeographiques() {
        try {
            List<ZoneGeographique> zones = zoneGeographiqueRepository.findAll();
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Récupérer une zone géographique par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','COMMERCIAL','PROJECT_MANAGER','DECISION_MAKER')")
    public ResponseEntity<ZoneGeographique> getZoneGeographiqueById(@PathVariable String id) {
        try {
            Optional<ZoneGeographique> zone = zoneGeographiqueRepository.findById(id);
            return zone.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Créer une nouvelle zone géographique
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ZoneGeographique> createZoneGeographique(@RequestBody ZoneGeographique zoneGeographique) {
        try {
            // Vérifier l'unicité du code
            if (zoneGeographiqueRepository.findByCode(zoneGeographique.getCode()).isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            ZoneGeographique savedZone = zoneGeographiqueRepository.save(zoneGeographique);
            return ResponseEntity.ok(savedZone);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Mettre à jour une zone géographique
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ZoneGeographique> updateZoneGeographique(@PathVariable String id, @RequestBody ZoneGeographique zoneGeographique) {
        try {
            if (!zoneGeographiqueRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            zoneGeographique.setId(id);
            ZoneGeographique updatedZone = zoneGeographiqueRepository.save(zoneGeographique);
            return ResponseEntity.ok(updatedZone);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Supprimer une zone géographique
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteZoneGeographique(@PathVariable String id) {
        try {
            if (!zoneGeographiqueRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            zoneGeographiqueRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Désactiver une zone géographique
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ZoneGeographique> deactivateZoneGeographique(@PathVariable String id) {
        try {
            Optional<ZoneGeographique> zoneOpt = zoneGeographiqueRepository.findById(id);
            if (zoneOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ZoneGeographique zone = zoneOpt.get();
            zone.setActif(false);
            ZoneGeographique updatedZone = zoneGeographiqueRepository.save(zone);
            return ResponseEntity.ok(updatedZone);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Récupérer les zones par gouvernement
    @GetMapping("/by-government/{government}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','COMMERCIAL','PROJECT_MANAGER','DECISION_MAKER')")
    public ResponseEntity<List<ZoneGeographique>> getZonesByGovernment(@PathVariable String government) {
        try {
            List<ZoneGeographique> zones = zoneGeographiqueRepository.findByGouvernement(government);
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

