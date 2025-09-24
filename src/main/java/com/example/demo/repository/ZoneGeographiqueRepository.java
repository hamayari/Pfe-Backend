package com.example.demo.repository;

import com.example.demo.model.ZoneGeographique;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneGeographiqueRepository extends MongoRepository<ZoneGeographique, String> {
    
    // Trouver une zone géographique par son code
    Optional<ZoneGeographique> findByCode(String code);
    
    // Trouver les zones géographiques actives
    List<ZoneGeographique> findByActifTrue();
    
    // Trouver les zones par gouvernement
    List<ZoneGeographique> findByGouvernement(String gouvernement);
    
    // Vérifier l'existence d'une zone par code
    boolean existsByCode(String code);
}