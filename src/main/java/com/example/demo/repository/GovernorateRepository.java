package com.example.demo.repository;

import com.example.demo.model.Governorate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GovernorateRepository extends MongoRepository<Governorate, String> {
} 