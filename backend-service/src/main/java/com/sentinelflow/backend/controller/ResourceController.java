package com.sentinelflow.backend.controller;

import com.sentinelflow.backend.dto.ResourceResponse;
import com.sentinelflow.backend.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/resource")
    public ResponseEntity<ResourceResponse> getResource(
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        return ResponseEntity.ok(resourceService.processResource(requestId));
    }

    @GetMapping("/slow")
    public ResponseEntity<ResourceResponse> getSlow(
            @RequestParam(value = "delayMs", defaultValue = "500") long delayMs,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        return ResponseEntity.ok(resourceService.processSlow(delayMs, requestId));
    }

    @GetMapping("/fault")
    public ResponseEntity<String> triggerFault() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Simulated backend internal server error");
    }
}
