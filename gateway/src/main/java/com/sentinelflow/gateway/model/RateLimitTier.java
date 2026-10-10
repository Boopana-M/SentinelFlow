package com.sentinelflow.gateway.model;

public enum RateLimitTier {
    FREE(100, 60),
    PREMIUM(1000, 60),
    ADMIN(5000, 60);

    private final long capacity;
    private final long windowSeconds;

    RateLimitTier(long capacity, long windowSeconds) {
        this.capacity = capacity;
        this.windowSeconds = windowSeconds;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public double getRefillRatePerMs() {
        return (double) capacity / (windowSeconds * 1000.0);
    }

    public static RateLimitTier fromRoleOrHeader(String role) {
        if (role == null) {
            return FREE;
        }
        String normalized = role.trim().toUpperCase().replace("ROLE_", "");
        try {
            return RateLimitTier.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return FREE;
        }
    }
}
