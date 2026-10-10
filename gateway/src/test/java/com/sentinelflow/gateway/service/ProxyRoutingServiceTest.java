package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.GatewayRoutingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyRoutingServiceTest {

    private ProxyRoutingService routingService;

    @BeforeEach
    void setUp() {
        GatewayRoutingProperties properties = new GatewayRoutingProperties();
        properties.setConnectTimeoutMs(500);
        properties.setReadTimeoutMs(500);
        routingService = new ProxyRoutingService(properties);
    }

    @Test
    void forwardRequest_whenUpstreamIsUnreachable_returnsBadGateway() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/proxy/test");
        request.setRemoteAddr("127.0.0.1");
        request.setScheme("http");

        // Port 59999 should not have anything listening
        ResponseEntity<byte[]> response = routingService.forwardRequest(
                "http://127.0.0.1:59999",
                "/test",
                HttpMethod.GET,
                null,
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(new String(response.getBody())).contains("Upstream backend unreachable");
    }
}
