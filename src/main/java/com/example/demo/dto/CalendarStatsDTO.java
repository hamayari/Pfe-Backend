package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CalendarStatsDTO {
    private Long totalEvents;
    private Long pendingEvents;
    private Long overdueEvents;
    private Long completedEvents;
    private Map<String, Long> eventsByType;
    private Map<String, Long> eventsByStatus;
    private Map<String, Long> eventsByDay;
}














































