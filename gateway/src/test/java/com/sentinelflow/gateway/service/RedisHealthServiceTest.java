package com.sentinelflow.gateway.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RedisHealthServiceTest {

    @Test
    void isConnected_returnsTrueWhenPingIsPong() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);

        given(connectionFactory.getConnection()).willReturn(connection);
        given(connection.ping()).willReturn("PONG");

        RedisHealthService service = new RedisHealthService(connectionFactory);
        assertThat(service.isConnected()).isTrue();
    }

    @Test
    void isConnected_returnsFalseWhenExceptionThrown() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        given(connectionFactory.getConnection()).willThrow(new RuntimeException("Connection refused"));

        RedisHealthService service = new RedisHealthService(connectionFactory);
        assertThat(service.isConnected()).isFalse();
    }
}
