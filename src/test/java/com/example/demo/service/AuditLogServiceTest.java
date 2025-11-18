package com.example.demo.service;

import com.example.demo.enums.ActionType;
import com.example.demo.model.AuditLog;
import com.example.demo.model.User;
import com.example.demo.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog mockLog;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockLog = new AuditLog();
        mockLog.setId("log1");
        mockLog.setUsername("testuser");
        mockLog.setAction(ActionType.CREATE);
        mockLog.setDetails("Test action");
        mockLog.setTimestamp(LocalDateTime.now());

        mockUser = new User();
        mockUser.setUsername("testuser");
    }

    @Test
    void testSave() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(mockLog);

        AuditLog result = auditLogService.save(mockLog);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(auditLogRepository).save(mockLog);
    }

    @Test
    void testLogAction() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(mockLog);

        auditLogService.logAction("testuser", "Test details");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogActionWithTarget() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(mockLog);

        auditLogService.logActionWithTarget("testuser", "entity123", "Test details");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogUserAction() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(mockLog);

        auditLogService.logUserAction(mockUser, ActionType.CREATE, "Test details");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(mockLog);

        auditLogService.log(ActionType.UPDATE, "testuser", "Test message");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testSearchLogs() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(mockLog);
        Page<AuditLog> page = new PageImpl<>(logs);

        when(auditLogRepository.searchLogs(anyString(), anyString(), anyString(), any(), any(), any(Pageable.class)))
            .thenReturn(page);

        Page<AuditLog> result = auditLogService.searchLogs("CREATE", "Convention", "user1",
            LocalDateTime.now().minusDays(7), LocalDateTime.now(), Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetLogsByUser() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(mockLog);
        Page<AuditLog> page = new PageImpl<>(logs);

        when(auditLogRepository.findByUsernameOrderByTimestampDesc("testuser", Pageable.unpaged()))
            .thenReturn(page);

        Page<AuditLog> result = auditLogService.getLogsByUser("testuser", Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testExportFilteredLogsToCsv() {
        List<AuditLog> logs = new ArrayList<>();
        mockLog.setEntityType("Convention");
        mockLog.setEntityId("conv1");
        logs.add(mockLog);
        Page<AuditLog> page = new PageImpl<>(logs);

        when(auditLogRepository.searchLogs(anyString(), anyString(), anyString(), any(), any(), any(Pageable.class)))
            .thenReturn(page);

        byte[] csv = auditLogService.exportFilteredLogsToCsv("CREATE", "Convention", "user1",
            LocalDateTime.now().minusDays(7), LocalDateTime.now());

        assertNotNull(csv);
        assertTrue(csv.length > 0);
        String csvContent = new String(csv);
        assertTrue(csvContent.contains("Timestamp"));
        assertTrue(csvContent.contains("testuser"));
    }
}
