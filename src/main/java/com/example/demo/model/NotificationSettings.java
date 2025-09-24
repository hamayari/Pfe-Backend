package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "notification_settings")
public class NotificationSettings {
    @Id
    private String id; // use "global" for global settings

    // Reminder configuration
    private List<Integer> reminderDays; // e.g., [7,3,1]
    private boolean autoReminderEnabled; // enable daily scheduler logic
    private String reminderFrequency; // daily, weekly, etc. (informational)

    // Channels
    private boolean emailEnabled;
    private boolean smsEnabled;

    // Optional quiet hours (not enforced yet)
    private boolean quietHoursEnabled;
    private String quietHoursStart; // "22:00"
    private String quietHoursEnd;   // "08:00"
}


