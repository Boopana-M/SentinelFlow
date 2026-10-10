package com.sentinelflow.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "gateway.health")
public class BackendHealthProperties {

    private boolean enabled = true;
    private long checkIntervalMs = 5000;
    private int timeoutMs = 2000;
    private int failureThreshold = 3;
    private int recoveryThreshold = 3;
    private List<InstanceConfig> instances = new ArrayList<>(List.of(
            new InstanceConfig("backend-1", "localhost", 8081),
            new InstanceConfig("backend-2", "localhost", 8082),
            new InstanceConfig("backend-3", "localhost", 8083)
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCheckIntervalMs() {
        return checkIntervalMs;
    }

    public void setCheckIntervalMs(long checkIntervalMs) {
        this.checkIntervalMs = checkIntervalMs;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public int getRecoveryThreshold() {
        return recoveryThreshold;
    }

    public void setRecoveryThreshold(int recoveryThreshold) {
        this.recoveryThreshold = recoveryThreshold;
    }

    public List<InstanceConfig> getInstances() {
        return instances;
    }

    public void setInstances(List<InstanceConfig> instances) {
        this.instances = instances;
    }

    public static class InstanceConfig {
        private String id;
        private String host;
        private int port;

        public InstanceConfig() {}

        public InstanceConfig(String id, String host, int port) {
            this.id = id;
            this.host = host;
            this.port = port;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
