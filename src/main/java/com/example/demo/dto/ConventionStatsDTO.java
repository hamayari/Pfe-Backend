package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ConventionStatsDTO {
    private int total;
    private int active;
    private int pending;
    private int expired;
    private Map<String, Integer> byGovernorate;
    private Map<String, Integer> byStructure;
}














































