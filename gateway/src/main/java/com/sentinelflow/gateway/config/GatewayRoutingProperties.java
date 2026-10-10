package com.sentinelflow.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "gateway.routing")
public class GatewayRoutingProperties {

    /**
     * Default upstream backend URLs when dynamic registry is not yet populated.
     */
    private List<String> defaultBackends = new ArrayList<>(List.of("http://localhost:8081"));

    /**
     * Request connection timeout in milliseconds.
     */
    private int connectTimeoutMs = 3000;

    /**
     * Request read timeout in milliseconds.
     */
    private int readTimeoutMs = 5000;

    public List<String> getDefaultBackends() {
        return defaultBackends;
    }

    public void setDefaultBackends(List<String> defaultBackends) {
        this.defaultBackends = defaultBackends;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}
