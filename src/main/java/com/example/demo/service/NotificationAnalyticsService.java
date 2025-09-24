package com.example.demo.service;

import com.example.demo.repository.NotificationLogRepository;
import com.example.demo.model.NotificationLog;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationAnalyticsService {

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    public Map<String, Object> getSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        long total = notificationLogRepository.findBySentAtBetween(start, end).size();
        long emails = notificationLogRepository.countByTypeAndSentAtBetween("EMAIL", start, end);
        long sms = notificationLogRepository.countByTypeAndSentAtBetween("SMS", start, end);
        long system = notificationLogRepository.countByTypeAndSentAtBetween("SYSTEM", start, end);
        long failed = notificationLogRepository.countByStatusAndSentAtBetween("FAILED", start, end);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("byChannel", Map.of(
                "EMAIL", emails,
                "SMS", sms,
                "SYSTEM", system
        ));
        result.put("failed", failed);
        return result;
    }

    public Map<String, Object> getDailySeries(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> series = new HashMap<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            LocalDateTime s = cursor.atStartOfDay();
            LocalDateTime e = cursor.plusDays(1).atStartOfDay().minusNanos(1);
            long total = notificationLogRepository.findBySentAtBetween(s, e).size();
            long emails = notificationLogRepository.countByTypeAndSentAtBetween("EMAIL", s, e);
            long sms = notificationLogRepository.countByTypeAndSentAtBetween("SMS", s, e);
            long system = notificationLogRepository.countByTypeAndSentAtBetween("SYSTEM", s, e);
            series.put(cursor.toString(), Map.of(
                    "total", total,
                    "EMAIL", emails,
                    "SMS", sms,
                    "SYSTEM", system
            ));
            cursor = cursor.plusDays(1);
        }
        return series;
    }
}




