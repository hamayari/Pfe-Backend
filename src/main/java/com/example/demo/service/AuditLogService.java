package com.example.demo.service;

import com.example.demo.enums.ActionType;
import com.example.demo.model.AuditLog;
import com.example.demo.model.User;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    public void logAction(String username, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public void logActionWithTarget(String username, String targetEntityId, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setEntityId(targetEntityId);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public void logUserAction(User user, ActionType action, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(user.getUsername());
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public void log(ActionType action, String username, String message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setDetails(message);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> searchLogs(String action, String entityType, String userId, 
                                   LocalDateTime startDate, LocalDateTime endDate, 
                                   Pageable pageable) {
        return auditLogRepository.searchLogs(action, entityType, userId, 
                                           startDate != null ? startDate.toInstant(ZoneOffset.UTC) : null,
                                           endDate != null ? endDate.toInstant(ZoneOffset.UTC) : null,
                                           pageable);
    }

    public Page<AuditLog> getLogsByUser(String userId, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(userId, pageable);
    }

    public byte[] exportFilteredLogsToCsv(String action, String entityType, String userId, 
                                LocalDateTime startDate, LocalDateTime endDate) {
        Page<AuditLog> logsPage = auditLogRepository.searchLogs(action, entityType, userId,
                                                              startDate != null ? startDate.toInstant(ZoneOffset.UTC) : null,
                                                              endDate != null ? endDate.toInstant(ZoneOffset.UTC) : null,
                                                              Pageable.unpaged());
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        try (java.io.PrintWriter writer = new java.io.PrintWriter(outputStream)) {
            writer.println("Timestamp,Username,Action,EntityType,EntityId,Details");
            java.time.format.DateTimeFormatter dateFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (AuditLog log : logsPage.getContent()) {
                writer.println(String.format(
                    "%s,%s,%s,%s,%s,\"%s\"",
                    log.getTimestamp() != null ? log.getTimestamp().format(dateFormat) : "",
                    log.getUsername() != null ? log.getUsername() : "",
                    log.getAction() != null ? log.getAction().name() : "",
                    log.getEntityType() != null ? log.getEntityType() : "",
                    log.getEntityId() != null ? log.getEntityId() : "",
                    log.getDetails() != null ? log.getDetails().replace("\"", "\"\"") : ""
                ));
            }
            writer.flush();
            return outputStream.toByteArray();
        }
    }
}
