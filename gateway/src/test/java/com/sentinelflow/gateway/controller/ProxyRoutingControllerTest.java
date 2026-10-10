package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.config.GatewayRoutingProperties;
import com.sentinelflow.gateway.service.ProxyRoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProxyRoutingController.class)
class ProxyRoutingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProxyRoutingService routingService;

    @MockBean
    private GatewayRoutingProperties routingProperties;

    @Test
    void proxyRequest_forwardsToBackendAndReturnsResponse() throws Exception {
        byte[] mockResponseBody = "{\"status\":\"UP\"}".getBytes();
        ResponseEntity<byte[]> mockResponse = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockResponseBody);

        given(routingProperties.getDefaultBackends())
                .willReturn(java.util.List.of("http://localhost:8081"));

        given(routingService.forwardRequest(
                eq("http://localhost:8081"),
                eq("/health"),
                eq(HttpMethod.GET),
                any(),
                any()
        )).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/proxy/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"UP\"}"));
    }
}
