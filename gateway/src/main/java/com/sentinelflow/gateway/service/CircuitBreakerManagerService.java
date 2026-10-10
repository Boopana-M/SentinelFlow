package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.dto.CircuitBreakerStatusDto;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class CircuitBreakerManagerService {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerManagerService.class);
    public static final String CIRCUIT_BREAKER_HEADER = "X-Circuit-Breaker-State";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public CircuitBreakerManagerService(CircuitBreakerRegistry circuitBreakerRegistry,
                                        RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    public ResponseEntity<byte[]> executeWithProtection(String serviceName,
                                                        HttpMethod method,
                                                        Supplier<ResponseEntity<byte[]>> supplier) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);

        Supplier<ResponseEntity<byte[]>> protectedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);

        // Only retry safe / idempotent HTTP methods to prevent retry storms
        if (isSafeMethod(method)) {
            Retry retry = retryRegistry.retry(serviceName);
            protectedSupplier = Retry.decorateSupplier(retry, protectedSupplier);
        }

        try {
            ResponseEntity<byte[]> response = protectedSupplier.get();

            HttpHeaders headers = new HttpHeaders();
            if (response.getHeaders() != null) {
                headers.putAll(response.getHeaders());
            }
            headers.set(CIRCUIT_BREAKER_HEADER, circuitBreaker.getState().name());

            return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());

        } catch (CallNotPermittedException ex) {
            log.warn("Circuit breaker '{}' is OPEN. Call blocked.", serviceName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(CIRCUIT_BREAKER_HEADER, CircuitBreaker.State.OPEN.name());

            byte[] errorPayload = "{\"error\":\"Service Unavailable\",\"message\":\"Circuit breaker is OPEN. Upstream requests are temporarily paused.\",\"status\":503}".getBytes();
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public List<CircuitBreakerStatusDto> getAllCircuitBreakers() {
        return circuitBreakerRegistry.getAllCircuitBreakers().stream()
                .map(CircuitBreakerStatusDto::from)
                .toList();
    }

    public CircuitBreaker getCircuitBreaker(String name) {
        return circuitBreakerRegistry.circuitBreaker(name);
    }

    private boolean isSafeMethod(HttpMethod method) {
        return method == HttpMethod.GET || method == HttpMethod.HEAD;
    }
}
