package com.sentinelflow.gateway.filter;

import com.sentinelflow.gateway.controller.ProxyRoutingController;
import com.sentinelflow.gateway.service.GatewayMetricsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

public class StructuredLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("SentinelFlowAccessLogger");

    private final GatewayMetricsService metricsService;

    public StructuredLoggingFilter(GatewayMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        Instant timestamp = Instant.now();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long latencyMs = System.currentTimeMillis() - startTime;
            String requestId = MDC.get(RequestIdFilter.MDC_KEY);
            if (requestId == null) {
                requestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
            }

            String backendInstance = response.getHeader(ProxyRoutingController.BACKEND_INSTANCE_HEADER);
            if (backendInstance == null) {
                backendInstance = "gateway";
            }

            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isBlank()) {
                clientIp = request.getRemoteAddr();
            }

            String userId = "anonymous";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                userId = auth.getName();
            }

            int statusCode = response.getStatus();
            String method = request.getMethod();
            String path = request.getRequestURI();
            String rateLimitRemaining = response.getHeader(RateLimiterFilter.HEADER_REMAINING);
            String rateLimitStatus = (statusCode == 429) ? "BLOCKED" : (rateLimitRemaining != null ? "ALLOWED" : "N/A");

            // Structured access log
            log.info("ts=\"{}\" reqId=\"{}\" method=\"{}\" path=\"{}\" client=\"{}\" user=\"{}\" backend=\"{}\" status={} latency={}ms rateLimit=\"{}\"",
                    timestamp, requestId, method, path, clientIp, userId, backendInstance, statusCode, latencyMs, rateLimitStatus);

            // Record Prometheus metrics
            metricsService.recordGatewayRequest(method, statusCode, path, latencyMs);
        }
    }
}
