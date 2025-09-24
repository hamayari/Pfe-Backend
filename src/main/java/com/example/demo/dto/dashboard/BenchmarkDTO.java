package com.example.demo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenchmarkDTO {
    private String name;
    private BigDecimal value;
    private String category;
    private BigDecimal differenceFromCurrent;
}
