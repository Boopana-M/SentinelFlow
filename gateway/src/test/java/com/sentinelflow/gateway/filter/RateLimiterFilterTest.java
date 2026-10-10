package com.sentinelflow.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelflow.gateway.dto.RateLimitResult;
import com.sentinelflow.gateway.model.RateLimitTier;
import com.sentinelflow.gateway.service.DistributedRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RateLimiterFilterTest {

    private DistributedRateLimiterService rateLimiterService;
    private com.sentinelflow.gateway.service.GatewayMetricsService metricsService;
    private RateLimiterFilter filter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rateLimiterService = mock(DistributedRateLimiterService.class);
        metricsService = mock(com.sentinelflow.gateway.service.GatewayMetricsService.class);
        objectMapper = new ObjectMapper();
        filter = new RateLimiterFilter(rateLimiterService, objectMapper, metricsService);
    }

    @Test
    void doFilter_whenAllowed_passesThroughWithHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/proxy/resource");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        RateLimitResult allowedResult = RateLimitResult.allowed(100, 95, 60);
        given(rateLimiterService.checkRateLimit(eq("ip:192.168.1.100"), eq(RateLimitTier.FREE)))
                .willReturn(allowedResult);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(RateLimiterFilter.HEADER_LIMIT)).isEqualTo("100");
        assertThat(response.getHeader(RateLimiterFilter.HEADER_REMAINING)).isEqualTo("95");
        assertThat(response.getHeader(RateLimiterFilter.HEADER_RESET)).isEqualTo("60");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenDenied_returns429WithRetryAfterHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/proxy/resource");
        request.addHeader("X-API-Key", "api-secret-key-1");
        request.addHeader("X-User-Role", "PREMIUM");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        RateLimitResult deniedResult = RateLimitResult.denied(1000, 0, 12, 60);
        given(rateLimiterService.checkRateLimit(eq("apikey:api-secret-key-1"), eq(RateLimitTier.PREMIUM)))
                .willReturn(deniedResult);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader(RateLimiterFilter.HEADER_LIMIT)).isEqualTo("1000");
        assertThat(response.getHeader(RateLimiterFilter.HEADER_REMAINING)).isEqualTo("0");
        assertThat(response.getHeader(RateLimiterFilter.HEADER_RETRY_AFTER)).isEqualTo("12");
        assertThat(response.getContentAsString()).contains("Rate limit exceeded for tier PREMIUM");
    }

    @Test
    void shouldNotFilter_actuatorAndStatusEndpoints() {
        MockHttpServletRequest actuatorReq = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletRequest statusReq = new MockHttpServletRequest("GET", "/api/v1/gateway/status");

        assertThat(filter.shouldNotFilter(actuatorReq)).isTrue();
        assertThat(filter.shouldNotFilter(statusReq)).isTrue();
    }
}
