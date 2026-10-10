package com.sentinelflow.backend.dto;

import java.time.Instant;

public record HealthResponse(
        String instanceId,
        String host,
        int port,
        String status,
        Instant timestamp,
        long uptimeSeconds
) {
}
