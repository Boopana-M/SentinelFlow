package com.sentinelflow.gateway.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayMetricsServiceTest {

    private MeterRegistry meterRegistry;
    private GatewayMetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new GatewayMetricsService(meterRegistry);
    }

    @Test
    void recordGatewayRequest_incrementsCountersAndRecordsLatency() {
        metricsService.recordGatewayRequest("GET", 200, "/api/v1/proxy/resource", 45);

        Counter requestCounter = meterRegistry.find("gateway_requests_total")
                .tag("method", "GET")
                .tag("status", "200")
                .counter();
        assertThat(requestCounter).isNotNull();
        assertThat(requestCounter.count()).isEqualTo(1.0);

        Timer latencyTimer = meterRegistry.find("gateway_request_latency")
                .tag("method", "GET")
                .tag("status", "200")
                .timer();
        assertThat(latencyTimer).isNotNull();
        assertThat(latencyTimer.count()).isEqualTo(1);
    }

    @Test
    void recordRateLimitAllowedAndBlocked_updatesCounters() {
        metricsService.recordRateLimitAllowed("FREE");
        metricsService.recordRateLimitBlocked("PREMIUM");

        Counter allowed = meterRegistry.find("rate_limit_allowed_total")
                .tag("tier", "FREE")
                .counter();
        assertThat(allowed).isNotNull();
        assertThat(allowed.count()).isEqualTo(1.0);

        Counter blocked = meterRegistry.find("rate_limit_blocked_total")
                .tag("tier", "PREMIUM")
                .counter();
        assertThat(blocked).isNotNull();
        assertThat(blocked.count()).isEqualTo(1.0);
    }

    @Test
    void updateGauges_reflectsCurrentValues() {
        metricsService.updateBackendHealth("backend-1", true);
        metricsService.updateBackendActiveRequests("backend-1", 5);
        metricsService.updateCircuitBreakerState("backendService", "OPEN");

        assertThat(meterRegistry.find("backend_health_status").tag("instance_id", "backend-1").gauge().value())
                .isEqualTo(1.0);
        assertThat(meterRegistry.find("backend_active_requests").tag("instance_id", "backend-1").gauge().value())
                .isEqualTo(5.0);
        assertThat(meterRegistry.find("circuit_breaker_state").tag("name", "backendService").gauge().value())
                .isEqualTo(2.0); // 2 = OPEN
    }
}
