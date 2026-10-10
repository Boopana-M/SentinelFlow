package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.LoadBalancerProperties;
import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.model.LoadBalancingAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadBalancerService {

    private final BackendHealthMonitorService healthMonitorService;
    private final LoadBalancerProperties loadBalancerProperties;
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public LoadBalancerService(BackendHealthMonitorService healthMonitorService,
                               LoadBalancerProperties loadBalancerProperties) {
        this.healthMonitorService = healthMonitorService;
        this.loadBalancerProperties = loadBalancerProperties;
    }

    public Optional<BackendInstance> selectInstance() {
        return selectInstance(loadBalancerProperties.getAlgorithm());
    }

    public Optional<BackendInstance> selectInstance(LoadBalancingAlgorithm algorithm) {
        List<BackendInstance> healthy = healthMonitorService.getHealthyInstances();
        if (healthy.isEmpty()) {
            return Optional.empty();
        }

        if (algorithm == LoadBalancingAlgorithm.LEAST_CONNECTIONS) {
            // Find healthy instance with lowest active requests
            return healthy.stream()
                    .min(Comparator.comparingInt(BackendInstance::getActiveRequests)
                            .thenComparingDouble(BackendInstance::getAverageLatencyMs));
        }

        // Default: ROUND_ROBIN
        int index = Math.abs(roundRobinCounter.getAndIncrement() % healthy.size());
        return Optional.of(healthy.get(index));
    }

    public void acquireConnection(BackendInstance instance) {
        if (instance != null) {
            instance.incrementActiveRequests();
        }
    }

    public void releaseConnection(BackendInstance instance, long latencyMs, boolean success) {
        if (instance != null) {
            instance.decrementActiveRequests();
            instance.recordRequest(success);
            if (latencyMs > 0) {
                instance.updateLatency(latencyMs);
            }
        }
    }
}
