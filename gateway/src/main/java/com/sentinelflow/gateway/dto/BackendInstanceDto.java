package com.sentinelflow.gateway.dto;

import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.model.InstanceStatus;

import java.time.Instant;

public record BackendInstanceDto(
        String id,
        String host,
        int port,
        String baseUrl,
        InstanceStatus status,
        Instant lastHeartbeat,
        double averageLatencyMs,
        int activeRequests,
        double errorRate,
        int consecutiveFailures,
        int consecutiveSuccesses
) {
    public static BackendInstanceDto from(BackendInstance instance) {
        return new BackendInstanceDto(
                instance.getId(),
                instance.getHost(),
                instance.getPort(),
                instance.getBaseUrl(),
                instance.getStatus(),
                instance.getLastHeartbeat(),
                instance.getAverageLatencyMs(),
                instance.getActiveRequests(),
                instance.getErrorRate(),
                instance.getConsecutiveFailures(),
                instance.getConsecutiveSuccesses()
        );
    }
}
