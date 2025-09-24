package com.example.demo.service;

import com.example.demo.model.ActionLog;
import com.example.demo.repository.ActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {
    @Autowired
    private ActionLogRepository actionLogRepository;

    public List<ActionLog> getUserActionLogs(String username) {
        return actionLogRepository.findByUsername(username);
    }

    public void logAction(String username, String actionType, String description) {
        ActionLog log = new ActionLog();
        log.setAction(actionType);
        log.setDetails(description);
        log.setUsername(username);
        log.setTimestamp(LocalDateTime.now());
        actionLogRepository.save(log);
    }
}
