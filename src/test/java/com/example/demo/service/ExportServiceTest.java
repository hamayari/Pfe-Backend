package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportService();
    }

    @Test
    void testExportLogsToPDF() throws Exception {
        List<Map<String, String>> logs = new ArrayList<>();
        Map<String, String> log1 = new HashMap<>();
        log1.put("date", "2025-11-18 10:00:00");
        log1.put("event", "Test event 1");
        log1.put("level", "INFO");
        logs.add(log1);

        Map<String, String> log2 = new HashMap<>();
        log2.put("date", "2025-11-18 11:00:00");
        log2.put("event", "Test event 2");
        log2.put("level", "ERROR");
        logs.add(log2);

        byte[] result = exportService.exportLogsToPDF(logs);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportLogsToPDF_EmptyList() throws Exception {
        List<Map<String, String>> logs = new ArrayList<>();

        byte[] result = exportService.exportLogsToPDF(logs);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportLogsToCSV() {
        List<Map<String, String>> logs = new ArrayList<>();
        Map<String, String> log1 = new HashMap<>();
        log1.put("date", "2025-11-18 10:00:00");
        log1.put("event", "Test event 1");
        log1.put("level", "INFO");
        logs.add(log1);

        String result = exportService.exportLogsToCSV(logs);

        assertNotNull(result);
        assertTrue(result.contains("Date/Heure"));
        assertTrue(result.contains("Test event 1"));
    }

    @Test
    void testExportLogsToCSV_EmptyList() {
        List<Map<String, String>> logs = new ArrayList<>();

        String result = exportService.exportLogsToCSV(logs);

        assertNotNull(result);
        assertTrue(result.contains("Date/Heure"));
    }

    @Test
    void testExportSystemStatsToPDF() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cpuUsage", 45.5);
        stats.put("ramUsage", 60.2);
        stats.put("diskSpace", 75.0);
        stats.put("uptime", 86400000L); // 1 day
        stats.put("javaVersion", "17.0.1");
        stats.put("osName", "Linux");
        stats.put("totalMemory", "8GB");
        stats.put("freeMemory", "3GB");

        byte[] result = exportService.exportSystemStatsToPDF(stats);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testExportSystemStatsToPDF_MinimalStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cpuUsage", 0.0);
        stats.put("ramUsage", 0.0);
        stats.put("diskSpace", 0.0);
        stats.put("uptime", 0L);
        stats.put("javaVersion", "Unknown");
        stats.put("osName", "Unknown");
        stats.put("totalMemory", "0");
        stats.put("freeMemory", "0");

        byte[] result = exportService.exportSystemStatsToPDF(stats);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
