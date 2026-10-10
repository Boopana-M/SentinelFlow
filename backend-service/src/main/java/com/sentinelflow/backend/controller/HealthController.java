package com.sentinelflow.backend.controller;

import com.sentinelflow.backend.dto.HealthResponse;
import com.sentinelflow.backend.dto.ToggleStatusRequest;
import com.sentinelflow.backend.service.BackendHealthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final BackendHealthService healthService;

    public HealthController(BackendHealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = healthService.getHealth();
        if (!healthService.isHealthy()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/health/toggle")
    public ResponseEntity<HealthResponse> toggleStatus(@Valid @RequestBody ToggleStatusRequest request) {
        healthService.setStatus(request.status());
        return getHealth();
    }
}
