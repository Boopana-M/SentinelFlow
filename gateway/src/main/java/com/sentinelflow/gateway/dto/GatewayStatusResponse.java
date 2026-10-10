package com.sentinelflow.gateway.dto;

import java.time.Instant;

public record GatewayStatusResponse(
        String name,
        String status,
        String version,
        Instant timestamp
) {
    public static GatewayStatusResponse operational(String name, String version) {
        return new GatewayStatusResponse(name, "OPERATIONAL", version, Instant.now());
    }
}
