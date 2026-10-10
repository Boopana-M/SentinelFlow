package com.sentinelflow.gateway.controller;

import com.sentinelflow.gateway.dto.CircuitBreakerStatusDto;
import com.sentinelflow.gateway.service.CircuitBreakerManagerService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gateway/circuit-breaker")
public class CircuitBreakerController {

    private final CircuitBreakerManagerService circuitBreakerService;

    public CircuitBreakerController(CircuitBreakerManagerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    @GetMapping
    public ResponseEntity<List<CircuitBreakerStatusDto>> getCircuitBreakers() {
        return ResponseEntity.ok(circuitBreakerService.getAllCircuitBreakers());
    }

    @PostMapping("/{name}/transition/{state}")
    public ResponseEntity<CircuitBreakerStatusDto> transitionState(@PathVariable String name,
                                                                   @PathVariable String state) {
        CircuitBreaker cb = circuitBreakerService.getCircuitBreaker(name);
        String upperState = state.toUpperCase();

        switch (upperState) {
            case "OPEN" -> cb.transitionToOpenState();
            case "CLOSED" -> cb.transitionToClosedState();
            case "HALF_OPEN" -> cb.transitionToHalfOpenState();
            case "DISABLED" -> cb.transitionToDisabledState();
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(CircuitBreakerStatusDto.from(cb));
    }
}
