package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.demo.controller.MonitoringController;

import java.util.Map;

@Service
public class MonitoringWebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private MonitoringController monitoringController;
    
    @Autowired
    @SuppressWarnings("unused")
    private AlertService alertService;

    // @Scheduled(fixedRate = 5000) // DÉSACTIVÉ - Envoi toutes les 5 secondes
    public void sendSystemStats() {
        // DÉSACTIVÉ pour éviter les logs inutiles
        return;
    }

    // @Scheduled(fixedRate = 30000) // DÉSACTIVÉ - Envoi toutes les 30 secondes
    public void sendUsageHistory() {
        // DÉSACTIVÉ pour éviter les logs inutiles
        return;
    }

    // @Scheduled(fixedRate = 10000) // DÉSACTIVÉ - Envoi toutes les 10 secondes
    public void sendPerformance() {
        // DÉSACTIVÉ pour éviter les logs inutiles
        return;
    }

    // @Scheduled(fixedRate = 15000) // DÉSACTIVÉ - Envoi toutes les 15 secondes
    public void sendLogs() {
        // DÉSACTIVÉ pour éviter les logs inutiles
        return;
    }
} 