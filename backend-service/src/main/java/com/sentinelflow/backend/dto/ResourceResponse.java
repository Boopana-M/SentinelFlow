package com.sentinelflow.backend.dto;

import java.time.Instant;

public record ResourceResponse(
        String message,
        String instanceId,
        int port,
        Instant timestamp,
        String requestId
) {
}
