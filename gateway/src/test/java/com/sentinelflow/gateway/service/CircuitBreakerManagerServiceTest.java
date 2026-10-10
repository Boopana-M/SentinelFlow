package com.sentinelflow.gateway.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerManagerServiceTest {

    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RetryRegistry retryRegistry;
    private CircuitBreakerManagerService circuitBreakerService;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50.0f)
                .build();
        circuitBreakerRegistry = CircuitBreakerRegistry.of(config);
        retryRegistry = RetryRegistry.ofDefaults();
        circuitBreakerService = new CircuitBreakerManagerService(circuitBreakerRegistry, retryRegistry);
    }

    @Test
    void executeWithProtection_whenClosed_allowsCallAndSetsHeader() {
        ResponseEntity<byte[]> response = circuitBreakerService.executeWithProtection(
                "backendService",
                HttpMethod.GET,
                () -> ResponseEntity.ok("success".getBytes())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(CircuitBreakerManagerService.CIRCUIT_BREAKER_HEADER))
                .isEqualTo("CLOSED");
        assertThat(new String(response.getBody())).isEqualTo("success");
    }

    @Test
    void executeWithProtection_whenOpen_returns503Immediately() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("backendService");
        cb.transitionToOpenState();

        ResponseEntity<byte[]> response = circuitBreakerService.executeWithProtection(
                "backendService",
                HttpMethod.GET,
                () -> ResponseEntity.ok("never called".getBytes())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getHeaders().getFirst(CircuitBreakerManagerService.CIRCUIT_BREAKER_HEADER))
                .isEqualTo("OPEN");
        assertThat(new String(response.getBody())).contains("Circuit breaker is OPEN");
    }

    @Test
    void canTransitionToHalfOpenState() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("backendService");
        cb.transitionToOpenState();
        cb.transitionToHalfOpenState();

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
    }
}
