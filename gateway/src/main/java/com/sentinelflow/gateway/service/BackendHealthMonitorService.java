package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.BackendHealthProperties;
import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.model.InstanceStatus;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BackendHealthMonitorService {

    private static final Logger log = LoggerFactory.getLogger(BackendHealthMonitorService.class);

    private final BackendHealthProperties healthProperties;
    private final RestClient restClient;
    private final Map<String, BackendInstance> instances = new ConcurrentHashMap<>();

    public BackendHealthMonitorService(BackendHealthProperties healthProperties) {
        this.healthProperties = healthProperties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(healthProperties.getTimeoutMs());
        requestFactory.setReadTimeout(healthProperties.getTimeoutMs());

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @PostConstruct
    public void init() {
        if (healthProperties.getInstances() != null) {
            for (BackendHealthProperties.InstanceConfig config : healthProperties.getInstances()) {
                BackendInstance instance = new BackendInstance(config.getId(), config.getHost(), config.getPort());
                instances.put(config.getId(), instance);
            }
        }
    }

    @Scheduled(fixedDelayString = "${gateway.health.check-interval-ms:5000}")
    public void runHealthChecks() {
        if (!healthProperties.isEnabled()) {
            return;
        }

        for (BackendInstance instance : instances.values()) {
            probeInstance(instance);
        }
    }

    public void probeInstance(BackendInstance instance) {
        String healthUrl = instance.getBaseUrl() + "/health";
        long start = System.currentTimeMillis();

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(URI.create(healthUrl))
                    .retrieve()
                    .toEntity(String.class);

            long elapsed = System.currentTimeMillis() - start;

            if (response.getStatusCode().is2xxSuccessful()) {
                handleSuccess(instance, elapsed);
            } else {
                handleFailure(instance, "HTTP status " + response.getStatusCode());
            }

        } catch (Exception ex) {
            handleFailure(instance, ex.getMessage());
        }
    }

    private void handleSuccess(BackendInstance instance, long latencyMs) {
        instance.setLastHeartbeat(Instant.now());
        instance.updateLatency(latencyMs);
        int successes = instance.incrementConsecutiveSuccesses();

        if (instance.getStatus() != InstanceStatus.UP) {
            if (successes >= healthProperties.getRecoveryThreshold()) {
                instance.setStatus(InstanceStatus.UP);
                log.info("Backend instance {} marked UP after {} successful probes",
                        instance.getId(), successes);
            }
        }
    }

    private void handleFailure(BackendInstance instance, String reason) {
        int failures = instance.incrementConsecutiveFailures();
        log.debug("Backend instance {} probe failed (count={}): {}",
                instance.getId(), failures, reason);

        if (instance.getStatus() != InstanceStatus.DOWN) {
            if (failures >= healthProperties.getFailureThreshold()) {
                instance.setStatus(InstanceStatus.DOWN);
                log.warn("Backend instance {} marked DOWN after {} consecutive failures: {}",
                        instance.getId(), failures, reason);
            }
        }
    }

    public List<BackendInstance> getAllInstances() {
        return new ArrayList<>(instances.values());
    }

    public List<BackendInstance> getHealthyInstances() {
        return instances.values().stream()
                .filter(i -> i.getStatus() == InstanceStatus.UP)
                .toList();
    }

    public BackendInstance getInstance(String id) {
        return instances.get(id);
    }

    public void registerInstance(BackendInstance instance) {
        instances.put(instance.getId(), instance);
    }
}
