package com.example.demo.repository;

import com.example.demo.model.ActionLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ActionLogRepository extends MongoRepository<ActionLog, String> {
    List<ActionLog> findByUsername(String username);
}
