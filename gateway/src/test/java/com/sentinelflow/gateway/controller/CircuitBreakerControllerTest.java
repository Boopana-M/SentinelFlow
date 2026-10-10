package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.dto.CircuitBreakerStatusDto;
import com.sentinelflow.gateway.service.CircuitBreakerManagerService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CircuitBreakerController.class)
class CircuitBreakerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CircuitBreakerManagerService circuitBreakerService;

    @Test
    void getCircuitBreakers_returnsList() throws Exception {
        CircuitBreakerStatusDto dto = new CircuitBreakerStatusDto(
                "backendService", "CLOSED", 0.0f, 0.0f, 0, 0, 0, 0L
        );
        given(circuitBreakerService.getAllCircuitBreakers()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/gateway/circuit-breaker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("backendService"))
                .andExpect(jsonPath("$[0].state").value("CLOSED"));
    }

    @Test
    void transitionState_transitionsCircuitBreakerState() throws Exception {
        CircuitBreaker cb = mock(CircuitBreaker.class);
        given(circuitBreakerService.getCircuitBreaker("backendService")).willReturn(cb);
        given(cb.getName()).willReturn("backendService");
        given(cb.getState()).willReturn(CircuitBreaker.State.OPEN);
        given(cb.getMetrics()).willReturn(mock(CircuitBreaker.Metrics.class));

        mockMvc.perform(post("/api/v1/gateway/circuit-breaker/backendService/transition/OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("backendService"))
                .andExpect(jsonPath("$.state").value("OPEN"));
    }
}
