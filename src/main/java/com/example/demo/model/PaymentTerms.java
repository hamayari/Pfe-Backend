package com.example.demo.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentTerms {
    private BigDecimal amount;
    private int paymentDays;  // Number of days after invoice generation
    private String currency = "EUR";
    private String paymentMethod;
    private int numberOfPayments; // Nombre d'échéances
    private int intervalDays; // Intervalle en jours entre chaque échéance
}
