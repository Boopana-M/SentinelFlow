package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.GatewayProperties;
import com.sentinelflow.gateway.dto.GatewayStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GatewayStatusService {

    private final String applicationName;
    private final String applicationVersion;
    private final GatewayProperties gatewayProperties;

    public GatewayStatusService(
            @Value("${spring.application.name:sentinelflow-gateway}") String applicationName,
            @Value("${sentinelflow.version:1.0.0-SNAPSHOT}") String applicationVersion,
            GatewayProperties gatewayProperties) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.gatewayProperties = gatewayProperties;
    }

    public GatewayStatusResponse getStatus() {
        return GatewayStatusResponse.operational(
                applicationName,
                gatewayProperties.getInstanceId(),
                applicationVersion
        );
    }
}
