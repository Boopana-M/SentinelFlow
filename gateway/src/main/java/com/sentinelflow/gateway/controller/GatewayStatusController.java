package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.dto.GatewayStatusResponse;
import com.sentinelflow.gateway.service.GatewayStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayStatusController {

    private final GatewayStatusService statusService;

    public GatewayStatusController(GatewayStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    public ResponseEntity<GatewayStatusResponse> getStatus() {
        return ResponseEntity.ok(statusService.getStatus());
    }
}
