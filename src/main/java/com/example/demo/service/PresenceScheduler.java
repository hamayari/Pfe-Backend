package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

/**
 * Periodically enforces presence status by marking inactive users as offline.
 * A user is considered inactive if lastLoginAt is older than the configured threshold.
 */
@Component
@RequiredArgsConstructor
public class PresenceScheduler {

    private static final Logger log = LoggerFactory.getLogger(PresenceScheduler.class);

    private final UserRepository userRepository;

    // Threshold after which an inactive user is set to offline
    private static final Duration INACTIVITY_THRESHOLD = Duration.ofMinutes(2);

    // Runs every minute
    @Scheduled(fixedDelay = 60_000)
    public void markInactiveUsersOffline() {
        try {
            Instant cutoff = Instant.now().minus(INACTIVITY_THRESHOLD);
            List<User> onlineUsers = userRepository.findByStatus("online");
            int updated = 0;
            for (User user : onlineUsers) {
                Instant last = user.getLastLoginAt();
                if (last == null || last.isBefore(cutoff)) {
                    user.setStatus("offline");
                    userRepository.save(user);
                    updated++;
                }
            }
            if (updated > 0) {
                log.debug("PresenceScheduler: {} user(s) set to offline due to inactivity", updated);
            }
        } catch (Exception ex) {
            log.warn("PresenceScheduler error: {}", ex.getMessage());
        }
    }
}


