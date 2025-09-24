package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.service.PushNotificationService;

@RestController
@RequestMapping("/api/push")
public class PushController {
    // Stockage en mémoire pour la démo (à remplacer par une vraie base en prod)
    private final CopyOnWriteArrayList<Map<String, Object>> subscriptions = new CopyOnWriteArrayList<>();

    @Autowired
    private PushNotificationService pushNotificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> subscription) {
        subscriptions.add(subscription);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/notify-all")
    public ResponseEntity<?> notifyAll(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "Notification test");
        for (Map<String, Object> sub : subscriptions) {
            try {
                pushNotificationService.sendPushNotification(sub, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscriptions")
    public CopyOnWriteArrayList<Map<String, Object>> getSubscriptions() {
        return subscriptions;
    }
} 