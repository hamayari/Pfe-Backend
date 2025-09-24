package com.example.demo.dto;

import lombok.Data;

@Data
public class NotificationPreferencesDTO {
    private String userId;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean browserEnabled;
    private String timezone;
}







































