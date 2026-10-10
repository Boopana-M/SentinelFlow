package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.service.LoadBalancerService;
import com.sentinelflow.gateway.service.ProxyRoutingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/proxy")
public class ProxyRoutingController {

    public static final String BACKEND_INSTANCE_HEADER = "X-Backend-Instance";

    private final ProxyRoutingService routingService;
    private final LoadBalancerService loadBalancerService;

    public ProxyRoutingController(ProxyRoutingService routingService,
                                  LoadBalancerService loadBalancerService) {
        this.routingService = routingService;
        this.loadBalancerService = loadBalancerService;
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

        // Select healthy backend using Load Balancer (Round Robin / Least Connections)
        Optional<BackendInstance> selectedInstanceOpt = loadBalancerService.selectInstance();

        if (selectedInstanceOpt.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            byte[] errorPayload = "{\"error\":\"Service Unavailable\",\"message\":\"No healthy backend instances available\",\"status\":503}".getBytes();
            return new ResponseEntity<>(errorPayload, headers, HttpStatus.SERVICE_UNAVAILABLE);
        }

        BackendInstance instance = selectedInstanceOpt.get();
        loadBalancerService.acquireConnection(instance);

        long start = System.currentTimeMillis();
        boolean success = false;

        try {
            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            ResponseEntity<byte[]> response = routingService.forwardRequest(
                    instance.getBaseUrl(),
                    upstreamPath,
                    method,
                    body,
                    request
            );

            HttpHeaders headers = new HttpHeaders();
            if (response.getHeaders() != null) {
                headers.putAll(response.getHeaders());
            }
            headers.set(BACKEND_INSTANCE_HEADER, instance.getId());

            success = response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection();
            return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());

        } finally {
            long latency = System.currentTimeMillis() - start;
            loadBalancerService.releaseConnection(instance, latency, success);
        }
    }
}
