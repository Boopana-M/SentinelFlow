package com.sentinelflow.backend.service;

import com.sentinelflow.backend.config.BackendProperties;
import com.sentinelflow.backend.dto.ResourceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ResourceService {

    private final BackendProperties backendProperties;
    private final int serverPort;

    public ResourceService(
            BackendProperties backendProperties,
            @Value("${server.port:8081}") int serverPort) {
        this.backendProperties = backendProperties;
        this.serverPort = serverPort;
    }

    public ResourceResponse processResource(String requestId) {
        return new ResourceResponse(
                "Processed successfully by backend instance",
                backendProperties.getInstanceId(),
                serverPort,
                Instant.now(),
                requestId != null ? requestId : "n/a"
        );
    }

    public ResourceResponse processSlow(long delayMs, String requestId) {
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Simulated delay interrupted", e);
            }
        }
        return new ResourceResponse(
                "Processed after " + delayMs + "ms delay by backend instance",
                backendProperties.getInstanceId(),
                serverPort,
                Instant.now(),
                requestId != null ? requestId : "n/a"
        );
    }
}
