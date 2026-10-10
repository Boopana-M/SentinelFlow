package com.sentinelflow.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getResource_returnsValidResponse() throws Exception {
        mockMvc.perform(get("/api/v1/resource")
                        .header("X-Request-ID", "req-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instanceId").value("backend-1"))
                .andExpect(jsonPath("$.requestId").value("req-12345"))
                .andExpect(jsonPath("$.message").value("Processed successfully by backend instance"));
    }

    @Test
    void getSlow_returnsDelayedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/slow?delayMs=10")
                        .header("X-Request-ID", "req-slow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-slow"));
    }

    @Test
    void getFault_returnsInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/fault"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Simulated backend internal server error"));
    }
}
