package com.sentinelflow.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
public class RedisHealthService {

    private static final Logger log = LoggerFactory.getLogger(RedisHealthService.class);

    private final RedisConnectionFactory connectionFactory;

    public RedisHealthService(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public boolean isConnected() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            String pingResult = connection.ping();
            return "PONG".equalsIgnoreCase(pingResult);
        } catch (Exception ex) {
            log.warn("Redis connectivity check failed: {}", ex.getMessage());
            return false;
        }
    }
}
