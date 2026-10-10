package com.sentinelflow.gateway.config;

import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.model.InstanceStatus;
import com.sentinelflow.gateway.model.UserRole;
import com.sentinelflow.gateway.service.CircuitBreakerManagerService;
import com.sentinelflow.gateway.service.JwtTokenService;
import com.sentinelflow.gateway.service.LoadBalancerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private LoadBalancerService loadBalancerService;

    @MockBean
    private CircuitBreakerManagerService circuitBreakerService;

    @BeforeEach
    void setUp() {
        BackendInstance instance = new BackendInstance("backend-1", "localhost", 8081);
        instance.setStatus(InstanceStatus.UP);
        given(loadBalancerService.selectInstance()).willReturn(Optional.of(instance));

        ResponseEntity<byte[]> mockResponse = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"OK\"}".getBytes());
        given(circuitBreakerService.executeWithProtection(any(), any(), any()))
                .willReturn(mockResponse);
    }

    @Test
    void unauthenticatedRequestToProxy_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/proxy/resource"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void requestWithValidJwtToken_isAuthorized() throws Exception {
        String token = jwtTokenService.generateToken("testuser", UserRole.USER);

        mockMvc.perform(get("/api/v1/proxy/resource")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithValidApiKey_isAuthorized() throws Exception {
        mockMvc.perform(get("/api/v1/proxy/resource")
                        .header("X-API-Key", "sf-dev-m2m-key-2026"))
                .andExpect(status().isOk());
    }
}
