package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.model.InstanceStatus;
import com.sentinelflow.gateway.service.CircuitBreakerManagerService;
import com.sentinelflow.gateway.service.LoadBalancerService;
import com.sentinelflow.gateway.service.ProxyRoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(ProxyRoutingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProxyRoutingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProxyRoutingService routingService;

    @MockBean
    private LoadBalancerService loadBalancerService;

    @MockBean
    private CircuitBreakerManagerService circuitBreakerService;

    @Test
    void proxyRequest_routesToHealthyBackendAndSetsHeader() throws Exception {
        BackendInstance instance = new BackendInstance("backend-1", "localhost", 8081);
        instance.setStatus(InstanceStatus.UP);
        given(loadBalancerService.selectInstance()).willReturn(Optional.of(instance));

        byte[] mockResponseBody = "{\"status\":\"UP\"}".getBytes();
        ResponseEntity<byte[]> mockResponse = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockResponseBody);

        given(circuitBreakerService.executeWithProtection(
                eq("backendService"),
                eq(HttpMethod.GET),
                any()
        )).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/proxy/health"))
                .andExpect(status().isOk())
                .andExpect(header().string(ProxyRoutingController.BACKEND_INSTANCE_HEADER, "backend-1"))
                .andExpect(content().json("{\"status\":\"UP\"}"));
    }

    @Test
    void proxyRequest_whenNoHealthyBackends_returns503ServiceUnavailable() throws Exception {
        given(loadBalancerService.selectInstance()).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/proxy/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("No healthy backend instances available"));
    }
}
