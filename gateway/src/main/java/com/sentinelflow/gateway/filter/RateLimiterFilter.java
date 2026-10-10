package com.sentinelflow.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelflow.gateway.dto.RateLimitResult;
import com.sentinelflow.gateway.model.RateLimitTier;
import com.sentinelflow.gateway.service.DistributedRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class RateLimiterFilter extends OncePerRequestFilter {

    public static final String HEADER_LIMIT = "X-RateLimit-Limit";
    public static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    public static final String HEADER_RESET = "X-RateLimit-Reset";
    public static final String HEADER_RETRY_AFTER = "Retry-After";

    private final DistributedRateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;
    private final com.sentinelflow.gateway.service.GatewayMetricsService metricsService;

    public RateLimiterFilter(DistributedRateLimiterService rateLimiterService,
                             ObjectMapper objectMapper,
                             com.sentinelflow.gateway.service.GatewayMetricsService metricsService) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.equals("/api/v1/gateway/status");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIdentifier = resolveClientIdentifier(request);
        RateLimitTier tier = resolveTier(request);

        RateLimitResult result = rateLimiterService.checkRateLimit(clientIdentifier, tier);

        // Always attach rate limit metadata headers
        response.setHeader(HEADER_LIMIT, String.valueOf(result.limit()));
        response.setHeader(HEADER_REMAINING, String.valueOf(result.remaining()));
        response.setHeader(HEADER_RESET, String.valueOf(result.resetSeconds()));

        if (!result.allowed()) {
            if (metricsService != null) {
                metricsService.recordRateLimitBlocked(tier.name());
            }
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HEADER_RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorBody = Map.of(
                    "error", "Too Many Requests",
                    "status", 429,
                    "message", "Rate limit exceeded for tier " + tier.name() + ". Please retry after " + result.retryAfterSeconds() + " seconds.",
                    "retryAfter", result.retryAfterSeconds(),
                    "tier", tier.name()
            );

            objectMapper.writeValue(response.getOutputStream(), errorBody);
            return;
        }

        if (metricsService != null) {
            metricsService.recordRateLimitAllowed(tier.name());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIdentifier(HttpServletRequest request) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }

        String apiKey = request.getHeader("X-API-Key");
        if (StringUtils.hasText(apiKey)) {
            return "apikey:" + apiKey.trim();
        }

        String userId = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userId)) {
            return "user:" + userId.trim();
        }

        String clientIp = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(clientIp)) {
            clientIp = request.getRemoteAddr();
        } else {
            // Pick first IP if multiple forwarded IPs are present
            clientIp = clientIp.split(",")[0].trim();
        }

        return "ip:" + clientIp;
    }

    private RateLimitTier resolveTier(HttpServletRequest request) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getAuthorities().isEmpty()) {
            for (org.springframework.security.core.GrantedAuthority authority : auth.getAuthorities()) {
                String authStr = authority.getAuthority();
                if (authStr.startsWith("ROLE_")) {
                    return RateLimitTier.fromRoleOrHeader(authStr.substring(5));
                }
            }
        }

        String roleHeader = request.getHeader("X-User-Role");
        return RateLimitTier.fromRoleOrHeader(roleHeader);
    }
}
