package com.sentinelflow.backend.service;

import com.sentinelflow.backend.config.BackendProperties;
import com.sentinelflow.backend.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BackendHealthService {

    private final BackendProperties backendProperties;
    private final int serverPort;
    private final Instant startTime = Instant.now();
    private final AtomicReference<String> currentStatus = new AtomicReference<>("UP");

    public BackendHealthService(
            BackendProperties backendProperties,
            @Value("${server.port:8081}") int serverPort) {
        this.backendProperties = backendProperties;
        this.serverPort = serverPort;
    }

    public HealthResponse getHealth() {
        return new HealthResponse(
                backendProperties.getInstanceId(),
                backendProperties.getInstanceHost(),
                serverPort,
                currentStatus.get(),
                Instant.now(),
                Duration.between(startTime, Instant.now()).toSeconds()
        );
    }

    public boolean isHealthy() {
        return "UP".equalsIgnoreCase(currentStatus.get());
    }

    public void setStatus(String newStatus) {
        this.currentStatus.set(newStatus.toUpperCase());
    }
}
