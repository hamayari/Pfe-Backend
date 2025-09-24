package com.example.demo.controller;

import com.example.demo.service.NotificationAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/analytics")
public class NotificationAnalyticsController {

    @Autowired
    private NotificationAnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        return ResponseEntity.ok(analyticsService.getSummary(s, e));
    }

    @GetMapping("/daily-series")
    public ResponseEntity<Map<String, Object>> dailySeries(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        return ResponseEntity.ok(analyticsService.getDailySeries(s, e));
    }
}




