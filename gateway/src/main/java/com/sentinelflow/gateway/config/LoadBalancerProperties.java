package com.sentinelflow.gateway.config;

import com.sentinelflow.gateway.model.LoadBalancingAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway.load-balancer")
public class LoadBalancerProperties {

    private LoadBalancingAlgorithm algorithm = LoadBalancingAlgorithm.ROUND_ROBIN;

    public LoadBalancingAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(LoadBalancingAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
}
