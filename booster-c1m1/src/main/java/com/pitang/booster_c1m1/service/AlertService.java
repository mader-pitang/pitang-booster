package com.pitang.booster_c1m1.service;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final MeterRegistry meterRegistry;

    /**
     * Logs a simple alert with metrics tracking
     * Following YAGNI principle - keeping it simple for now
     */
    public void logAlert(String alertType, String message) {
        Counter.builder("alerts.triggered.total")
            .tag("type", alertType)
            .register(meterRegistry)
            .increment();

        log.error("ALERT - Type: {} - Message: {}", alertType, message);
    }

    /**
     * Alert for business logic issues - can be called from service layer
     */
    public void alertDatabaseConnectionIssue(String error) {
        logAlert("DATABASE_CONNECTION", "Database connection issue: " + error);
    }

    /**
     * Alert for user creation spikes - simple threshold check
     */
    public void alertUserCreationSpike(int usersCreatedInLastHour) {
        if (usersCreatedInLastHour > 100) {
            logAlert("USER_CREATION_SPIKE",
                String.format("Unusual user creation spike: %d users in last hour", usersCreatedInLastHour));
        }
    }
}