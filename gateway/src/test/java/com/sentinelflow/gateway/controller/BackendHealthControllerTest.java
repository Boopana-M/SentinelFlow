package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.model.InstanceStatus;
import com.sentinelflow.gateway.service.BackendHealthMonitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BackendHealthController.class)
class BackendHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BackendHealthMonitorService healthMonitorService;

    @Test
    void getAllBackends_returnsInstanceList() throws Exception {
        BackendInstance instance = new BackendInstance("backend-1", "localhost", 8081);
        instance.setStatus(InstanceStatus.UP);
        given(healthMonitorService.getAllInstances()).willReturn(List.of(instance));

        mockMvc.perform(get("/api/v1/gateway/backends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("backend-1"))
                .andExpect(jsonPath("$[0].status").value("UP"))
                .andExpect(jsonPath("$[0].port").value(8081));
    }

    @Test
    void getBackendById_whenNotFound_returns404() throws Exception {
        given(healthMonitorService.getInstance("unknown-id")).willReturn(null);

        mockMvc.perform(get("/api/v1/gateway/backends/unknown-id"))
                .andExpect(status().isNotFound());
    }
}
