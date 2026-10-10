package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.LoadBalancerProperties;
import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.model.InstanceStatus;
import com.sentinelflow.gateway.model.LoadBalancingAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LoadBalancerServiceTest {

    private BackendHealthMonitorService healthMonitorService;
    private LoadBalancerProperties properties;
    private LoadBalancerService loadBalancerService;

    @BeforeEach
    void setUp() {
        healthMonitorService = mock(BackendHealthMonitorService.class);
        properties = new LoadBalancerProperties();
        loadBalancerService = new LoadBalancerService(healthMonitorService, properties);
    }

    @Test
    void roundRobin_cyclesThroughHealthyInstances() {
        BackendInstance b1 = new BackendInstance("backend-1", "localhost", 8081);
        BackendInstance b2 = new BackendInstance("backend-2", "localhost", 8082);
        b1.setStatus(InstanceStatus.UP);
        b2.setStatus(InstanceStatus.UP);

        given(healthMonitorService.getHealthyInstances()).willReturn(List.of(b1, b2));

        Optional<BackendInstance> first = loadBalancerService.selectInstance(LoadBalancingAlgorithm.ROUND_ROBIN);
        Optional<BackendInstance> second = loadBalancerService.selectInstance(LoadBalancingAlgorithm.ROUND_ROBIN);
        Optional<BackendInstance> third = loadBalancerService.selectInstance(LoadBalancingAlgorithm.ROUND_ROBIN);

        assertThat(first).isPresent().contains(b1);
        assertThat(second).isPresent().contains(b2);
        assertThat(third).isPresent().contains(b1);
    }

    @Test
    void leastConnections_selectsInstanceWithLowestActiveRequests() {
        BackendInstance b1 = new BackendInstance("backend-1", "localhost", 8081);
        BackendInstance b2 = new BackendInstance("backend-2", "localhost", 8082);
        b1.setStatus(InstanceStatus.UP);
        b2.setStatus(InstanceStatus.UP);

        // b1 has 5 active requests, b2 has 1
        b1.incrementActiveRequests();
        b1.incrementActiveRequests();
        b1.incrementActiveRequests();
        b1.incrementActiveRequests();
        b1.incrementActiveRequests();

        b2.incrementActiveRequests();

        given(healthMonitorService.getHealthyInstances()).willReturn(List.of(b1, b2));

        Optional<BackendInstance> selected = loadBalancerService.selectInstance(LoadBalancingAlgorithm.LEAST_CONNECTIONS);
        assertThat(selected).isPresent().contains(b2);
    }

    @Test
    void selectInstance_whenNoHealthyBackends_returnsEmpty() {
        given(healthMonitorService.getHealthyInstances()).willReturn(Collections.emptyList());

        Optional<BackendInstance> selected = loadBalancerService.selectInstance();
        assertThat(selected).isEmpty();
    }
}
