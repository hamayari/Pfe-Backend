package com.example.demo.repository;

import com.example.demo.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    Page<AuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Instant startDate, Instant endDate, Pageable pageable);
    @Query("{'action': ?0, 'entityType': ?1, 'entityId': ?2, 'timestamp': {$gte: ?3, $lte: ?4}}")
    Page<AuditLog> searchLogs(String action, String entityType, String entityId, Instant startDate, Instant endDate, Pageable pageable);
    Page<AuditLog> searchByDetailsContainingIgnoreCase(String details, Pageable pageable);
    Page<AuditLog> findByDetailsContainingIgnoreCase(String details, Pageable pageable);
}
