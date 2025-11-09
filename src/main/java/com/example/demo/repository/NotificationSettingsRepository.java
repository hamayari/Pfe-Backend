package com.example.demo.repository;

import com.example.demo.model.NotificationSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingsRepository extends MongoRepository<NotificationSettings, String> {
}








