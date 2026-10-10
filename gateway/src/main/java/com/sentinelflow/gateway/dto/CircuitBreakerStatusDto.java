package com.sentinelflow.gateway.dto;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public record CircuitBreakerStatusDto(
        String name,
        String state,
        float failureRate,
        float slowCallRate,
        int numberOfBufferedCalls,
        int numberOfFailedCalls,
        int numberOfSuccessfulCalls,
        long numberOfNotPermittedCalls
) {
    public static CircuitBreakerStatusDto from(CircuitBreaker cb) {
        CircuitBreaker.Metrics m = cb.getMetrics();
        return new CircuitBreakerStatusDto(
                cb.getName(),
                cb.getState().name(),
                m.getFailureRate(),
                m.getSlowCallRate(),
                m.getNumberOfBufferedCalls(),
                m.getNumberOfFailedCalls(),
                m.getNumberOfSuccessfulCalls(),
                m.getNumberOfNotPermittedCalls()
        );
    }
}
