package com.example.demo.controller;

import com.example.demo.model.Governorate;
import com.example.demo.model.Structure;
import com.example.demo.repository.GovernorateRepository;
import com.example.demo.repository.StructureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private GovernorateRepository governorateRepository;

    @Autowired
    private StructureRepository structureRepository;

    @PostMapping("/init-governorates")
    public ResponseEntity<String> initializeGovernorates() {
        // Tous les gouvernorats de Tunisie
        String[] governorateNames = {
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", 
            "Bizerte", "Béja", "Jendouba", "Kef", "Siliana", "Kairouan", 
            "Kasserine", "Sidi Bouzid", "Sousse", "Monastir", "Mahdia", 
            "Sfax", "Gafsa", "Tozeur", "Kebili", "Gabès", "Médenine", "Tataouine"
        };

        int created = 0;
        for (String name : governorateNames) {
            // Vérifier si existe déjà
            List<Governorate> existing = governorateRepository.findAll();
            boolean exists = existing.stream().anyMatch(g -> name.equals(g.getName()));
            
            if (!exists) {
                Governorate governorate = new Governorate();
                governorate.setName(name);
                governorateRepository.save(governorate);
                created++;
                System.out.println("✅ Gouvernorat créé: " + name);
            }
        }
        
        return ResponseEntity.ok("Gouvernorats initialisés: " + created + " créés");
    }

    @PostMapping("/init-structures")
    public ResponseEntity<String> initializeStructures() {
        // Structures types en Tunisie
        String[] structureNames = {
            "Ministère de la Santé",
            "Ministère de l'Éducation", 
            "Ministère des Finances",
            "Ministère de l'Intérieur",
            "Ministère de la Justice",
            "Direction Régionale de la Santé",
            "Direction Régionale de l'Éducation",
            "Hôpital Régional",
            "Hôpital Universitaire",
            "Centre de Santé de Base",
            "École Primaire",
            "Collège",
            "Lycée",
            "Institut Supérieur",
            "Université",
            "Municipalité",
            "Conseil Régional",
            "Délégation",
            "Société Nationale",
            "Entreprise Publique",
            "Entreprise Privée",
            "ONTT",
            "ONAS",
            "STEG",
            "Tunisie Telecom"
        };

        int created = 0;
        for (String name : structureNames) {
            // Vérifier si existe déjà
            List<Structure> existing = structureRepository.findAll();
            boolean exists = existing.stream().anyMatch(s -> name.equals(s.getLibelle()));
            
            if (!exists) {
                Structure structure = new Structure();
                structure.setLibelle(name);
                structure.setCode(name.replaceAll(" ", "_").toUpperCase());
                structure.setActif(true);
                structureRepository.save(structure);
                created++;
                System.out.println("✅ Structure créée: " + name);
            }
        }
        
        return ResponseEntity.ok("Structures initialisées: " + created + " créées");
    }

    @PostMapping("/init-all")
    public ResponseEntity<String> initializeAll() {
        initializeGovernorates();
        initializeStructures();
        return ResponseEntity.ok("✅ Toutes les données de Tunisie initialisées!");
    }

    @GetMapping("/governorates")
    public ResponseEntity<List<Governorate>> getAllGovernorates() {
        return ResponseEntity.ok(governorateRepository.findAll());
    }

    @GetMapping("/structures") 
    public ResponseEntity<List<Structure>> getAllStructures() {
        return ResponseEntity.ok(structureRepository.findAll());
    }
}
