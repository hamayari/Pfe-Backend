package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CalendarFiltersDTO {
    private List<String> eventTypes;
    private List<String> statuses;
    private String userId;
    private LocalDate startDate;
    private LocalDate endDate;
}







































