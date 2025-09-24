package com.example.demo.model;

import lombok.Data;

@Data
public class NotificationPreference {
    private boolean emailEnabled = true;
    private boolean smsEnabled = false;
    private boolean whatsappEnabled = false;
    private int daysBeforeDueDate = 5;
}
