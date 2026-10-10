package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.BackendHealthProperties;
import com.sentinelflow.gateway.model.BackendInstance;
import com.sentinelflow.gateway.model.InstanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackendHealthMonitorServiceTest {

    private BackendHealthMonitorService monitorService;
    private BackendHealthProperties properties;

    @BeforeEach
    void setUp() {
        properties = new BackendHealthProperties();
        properties.setFailureThreshold(3);
        properties.setRecoveryThreshold(3);
        properties.setTimeoutMs(500);
        properties.setInstances(List.of(
                new BackendHealthProperties.InstanceConfig("backend-1", "127.0.0.1", 59991)
        ));

        monitorService = new BackendHealthMonitorService(properties);
        monitorService.init();
    }

    @Test
    void failureThreshold_marksInstanceDownAfterThreeFailures() {
        BackendInstance instance = monitorService.getInstance("backend-1");
        instance.setStatus(InstanceStatus.UP);

        // Probe 1 fails (unreachable port 59991)
        monitorService.probeInstance(instance);
        assertThat(instance.getConsecutiveFailures()).isEqualTo(1);
        assertThat(instance.getStatus()).isEqualTo(InstanceStatus.UP); // not yet DOWN

        // Probe 2 fails
        monitorService.probeInstance(instance);
        assertThat(instance.getConsecutiveFailures()).isEqualTo(2);
        assertThat(instance.getStatus()).isEqualTo(InstanceStatus.UP); // not yet DOWN

        // Probe 3 fails -> threshold reached
        monitorService.probeInstance(instance);
        assertThat(instance.getConsecutiveFailures()).isEqualTo(3);
        assertThat(instance.getStatus()).isEqualTo(InstanceStatus.DOWN); // Now DOWN
    }

    @Test
    void getHealthyInstances_filtersOutDownInstances() {
        BackendInstance instance = monitorService.getInstance("backend-1");
        instance.setStatus(InstanceStatus.DOWN);

        List<BackendInstance> healthy = monitorService.getHealthyInstances();
        assertThat(healthy).isEmpty();

        instance.setStatus(InstanceStatus.UP);
        healthy = monitorService.getHealthyInstances();
        assertThat(healthy).hasSize(1);
        assertThat(healthy.get(0).getId()).isEqualTo("backend-1");
    }

    @Test
    void latencyMovingAverage_updatesAccurately() {
        BackendInstance instance = new BackendInstance("test-1", "localhost", 8081);
        instance.updateLatency(100);
        assertThat(instance.getAverageLatencyMs()).isEqualTo(100.0);

        instance.updateLatency(200);
        // EMA: (0.2 * 200) + (0.8 * 100) = 40 + 80 = 120
        assertThat(instance.getAverageLatencyMs()).isEqualTo(120.0);
    }
}
