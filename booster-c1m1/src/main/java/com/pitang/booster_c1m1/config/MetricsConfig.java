package com.pitang.booster_c1m1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter userCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("users.created.total")
                .description("Total number of users created")
                .register(meterRegistry);
    }

    @Bean
    public Counter userUpdatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("users.updated.total")
                .description("Total number of users updated")
                .register(meterRegistry);
    }

    @Bean
    public Counter userDeletedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("users.deleted.total")
                .description("Total number of users deleted")
                .register(meterRegistry);
    }

    @Bean
    public Counter userNotFoundCounter(MeterRegistry meterRegistry) {
        return Counter.builder("users.not_found.total")
                .description("Total number of user not found errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter emailConflictCounter(MeterRegistry meterRegistry) {
        return Counter.builder("users.email_conflict.total")
                .description("Total number of email conflict errors")
                .register(meterRegistry);
    }

    @Bean
    public Timer databaseQueryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("database.query.duration")
                .description("Database query execution time")
                .register(meterRegistry);
    }

}