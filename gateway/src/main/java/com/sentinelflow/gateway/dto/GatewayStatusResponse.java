package com.sentinelflow.gateway.dto;

import java.time.Instant;

public record GatewayStatusResponse(
        String name,
        String instanceId,
        String status,
        String version,
        Instant timestamp
) {
    public static GatewayStatusResponse operational(String name, String instanceId, String version) {
        return new GatewayStatusResponse(name, instanceId, "OPERATIONAL", version, Instant.now());
    }
}
