package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.service.GatewayStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GatewayStatusController.class)
class GatewayStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayStatusService statusService;

    @Test
    void getStatus_returnsOperationalStatus() throws Exception {
        com.sentinelflow.gateway.dto.GatewayStatusResponse mockResponse =
                new com.sentinelflow.gateway.dto.GatewayStatusResponse(
                        "sentinelflow-gateway",
                        "OPERATIONAL",
                        "1.0.0-SNAPSHOT",
                        Instant.now()
                );

        given(statusService.getStatus()).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/gateway/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("sentinelflow-gateway"))
                .andExpect(jsonPath("$.status").value("OPERATIONAL"))
                .andExpect(jsonPath("$.version").value("1.0.0-SNAPSHOT"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
