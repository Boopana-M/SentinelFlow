package com.sentinelflow.gateway.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiGatewayInstanceTest {

    @Test
    void canConfigureMultipleDistinctGatewayInstances() {
        GatewayProperties gw1 = new GatewayProperties();
        gw1.setInstanceId("gateway-1");

        GatewayProperties gw2 = new GatewayProperties();
        gw2.setInstanceId("gateway-2");

        GatewayProperties gw3 = new GatewayProperties();
        gw3.setInstanceId("gateway-3");

        assertThat(gw1.getInstanceId()).isEqualTo("gateway-1");
        assertThat(gw2.getInstanceId()).isEqualTo("gateway-2");
        assertThat(gw3.getInstanceId()).isEqualTo("gateway-3");
        assertThat(gw1.getInstanceId()).isNotEqualTo(gw2.getInstanceId());
        assertThat(gw2.getInstanceId()).isNotEqualTo(gw3.getInstanceId());
    }
}
