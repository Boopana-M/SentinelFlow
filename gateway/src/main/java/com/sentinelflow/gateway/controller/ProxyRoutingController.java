package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.config.GatewayRoutingProperties;
import com.sentinelflow.gateway.service.ProxyRoutingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/proxy")
public class ProxyRoutingController {

    private final ProxyRoutingService routingService;
    private final GatewayRoutingProperties routingProperties;

    public ProxyRoutingController(ProxyRoutingService routingService,
                                  GatewayRoutingProperties routingProperties) {
        this.routingService = routingService;
        this.routingProperties = routingProperties;
    }

    @RequestMapping(value = "/**")
    public ResponseEntity<byte[]> proxyRequest(HttpServletRequest request,
                                               @RequestBody(required = false) byte[] body) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String prefix = contextPath + "/api/v1/proxy";
        String upstreamPath = requestUri.substring(prefix.length());

        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            upstreamPath += "?" + request.getQueryString();
        }

        // Default backend target for Phase 4 (enhanced by LoadBalancer in Phase 9)
        String targetBaseUrl = routingProperties.getDefaultBackends().isEmpty()
                ? "http://localhost:8081"
                : routingProperties.getDefaultBackends().get(0);

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        return routingService.forwardRequest(targetBaseUrl, upstreamPath, method, body, request);
    }
}
