package com.sentinelflow.gateway.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GatewayMetricsService {

    private final MeterRegistry meterRegistry;

    // Gauges
    private final Map<String, AtomicInteger> backendHealthGauges = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> backendActiveRequestsGauges = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> circuitBreakerStateGauges = new ConcurrentHashMap<>();

    public GatewayMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordGatewayRequest(String method, int status, String route, long durationMs) {
        String statusGroup = (status / 100) + "xx";

        Counter.builder("gateway_requests_total")
                .description("Total requests processed by the API gateway")
                .tag("method", method)
                .tag("status", String.valueOf(status))
                .tag("status_group", statusGroup)
                .tag("route", route)
                .register(meterRegistry)
                .increment();

        Timer.builder("gateway_request_latency")
                .description("Request latency for API gateway calls")
                .tag("method", method)
                .tag("status", String.valueOf(status))
                .tag("route", route)
                .register(meterRegistry)
                .record(Duration.ofMillis(durationMs));

        if (status >= 400) {
            Counter.builder("gateway_errors_total")
                    .description("Total error responses from the API gateway")
                    .tag("method", method)
                    .tag("status", String.valueOf(status))
                    .tag("route", route)
                    .register(meterRegistry)
                    .increment();
        }
    }

    public void recordRateLimitAllowed(String tier) {
        Counter.builder("rate_limit_allowed_total")
                .description("Total requests allowed through rate limiting")
                .tag("tier", tier)
                .register(meterRegistry)
                .increment();
    }

    public void recordRateLimitBlocked(String tier) {
        Counter.builder("rate_limit_blocked_total")
                .description("Total requests blocked by rate limiting")
                .tag("tier", tier)
                .register(meterRegistry)
                .increment();
    }

    public void recordBackendLatency(String instanceId, long durationMs) {
        Timer.builder("backend_request_latency")
                .description("Latency of requests forwarded to backend instances")
                .tag("instance_id", instanceId)
                .register(meterRegistry)
                .record(Duration.ofMillis(durationMs));
    }

    public void updateBackendHealth(String instanceId, boolean isUp) {
        AtomicInteger gauge = backendHealthGauges.computeIfAbsent(instanceId, id -> {
            AtomicInteger val = new AtomicInteger(isUp ? 1 : 0);
            meterRegistry.gauge("backend_health_status",
                    java.util.List.of(io.micrometer.core.instrument.Tag.of("instance_id", id)),
                    val);
            return val;
        });
        gauge.set(isUp ? 1 : 0);
    }

    public void updateBackendActiveRequests(String instanceId, int activeRequests) {
        AtomicInteger gauge = backendActiveRequestsGauges.computeIfAbsent(instanceId, id -> {
            AtomicInteger val = new AtomicInteger(activeRequests);
            meterRegistry.gauge("backend_active_requests",
                    java.util.List.of(io.micrometer.core.instrument.Tag.of("instance_id", id)),
                    val);
            return val;
        });
        gauge.set(activeRequests);
    }

    public void updateCircuitBreakerState(String name, String state) {
        // 0 = CLOSED, 1 = HALF_OPEN, 2 = OPEN, 3 = OTHER
        int stateCode = switch (state.toUpperCase()) {
            case "CLOSED" -> 0;
            case "HALF_OPEN" -> 1;
            case "OPEN" -> 2;
            default -> 3;
        };

        AtomicInteger gauge = circuitBreakerStateGauges.computeIfAbsent(name, cbName -> {
            AtomicInteger val = new AtomicInteger(stateCode);
            meterRegistry.gauge("circuit_breaker_state",
                    java.util.List.of(io.micrometer.core.instrument.Tag.of("name", cbName)),
                    val);
            return val;
        });
        gauge.set(stateCode);
    }
}
