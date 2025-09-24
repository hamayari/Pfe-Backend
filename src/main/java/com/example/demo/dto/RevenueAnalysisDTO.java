package com.example.demo.dto;

import java.util.Map;

public class RevenueAnalysisDTO {
    private Map<String, Double> monthlyRevenue;
    private double totalRevenue;
    private double growthRate;
    private double averageMonthlyRevenue;
    private String period;
    private Map<String, Double> quarterlyRevenue;
    private Map<String, Double> yearlyRevenue;

    public RevenueAnalysisDTO() {}

    // Getters and Setters
    public Map<String, Double> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(Map<String, Double> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

    public double getAverageMonthlyRevenue() {
        return averageMonthlyRevenue;
    }

    public void setAverageMonthlyRevenue(double averageMonthlyRevenue) {
        this.averageMonthlyRevenue = averageMonthlyRevenue;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Map<String, Double> getQuarterlyRevenue() {
        return quarterlyRevenue;
    }

    public void setQuarterlyRevenue(Map<String, Double> quarterlyRevenue) {
        this.quarterlyRevenue = quarterlyRevenue;
    }

    public Map<String, Double> getYearlyRevenue() {
        return yearlyRevenue;
    }

    public void setYearlyRevenue(Map<String, Double> yearlyRevenue) {
        this.yearlyRevenue = yearlyRevenue;
    }
}







