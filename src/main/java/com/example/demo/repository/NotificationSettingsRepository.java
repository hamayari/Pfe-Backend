package com.example.demo.repository;

import com.example.demo.model.NotificationSettings;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationSettingsRepository extends MongoRepository<NotificationSettings, String> {
}




