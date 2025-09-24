package com.example.demo.repository;

import com.example.demo.model.Structure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureRepository extends MongoRepository<Structure, String> {
}