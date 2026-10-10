package com.sentinelflow.gateway.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String role,
        long expiresInMs
) {
    public static AuthResponse bearer(String token, String username, String role, long expiresInMs) {
        return new AuthResponse(token, "Bearer", username, role, expiresInMs);
    }
}
