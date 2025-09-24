package com.example.demo.repository;

import com.example.demo.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    
    // Trouver une application par son code
    Optional<Application> findByCode(String code);
    
    // Trouver les applications actives
    List<Application> findByActifTrue();
    
    // Trouver les applications inactives
    List<Application> findByActifFalse();
    
    // Vérifier l'existence d'une application par code
    boolean existsByCode(String code);
}