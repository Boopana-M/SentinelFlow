package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.dto.BackendInstanceDto;
import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.service.BackendHealthMonitorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gateway/backends")
public class BackendHealthController {

    private final BackendHealthMonitorService healthMonitorService;

    public BackendHealthController(BackendHealthMonitorService healthMonitorService) {
        this.healthMonitorService = healthMonitorService;
    }

    @GetMapping
    public ResponseEntity<List<BackendInstanceDto>> getAllBackends() {
        List<BackendInstanceDto> dtos = healthMonitorService.getAllInstances().stream()
                .map(BackendInstanceDto::from)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BackendInstanceDto> getBackendById(@PathVariable String id) {
        BackendInstance instance = healthMonitorService.getInstance(id);
        if (instance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BackendInstanceDto.from(instance));
    }
}
