package com.sentinelflow.gateway.dto;

public record RateLimitResult(
        boolean allowed,
        long limit,
        long remaining,
        long retryAfterSeconds,
        long resetSeconds
) {
    public static RateLimitResult allowed(long limit, long remaining, long resetSeconds) {
        return new RateLimitResult(true, limit, remaining, 0, resetSeconds);
    }

    public static RateLimitResult denied(long limit, long remaining, long retryAfterSeconds, long resetSeconds) {
        return new RateLimitResult(false, limit, remaining, retryAfterSeconds, resetSeconds);
    }
}
