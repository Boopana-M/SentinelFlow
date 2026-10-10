package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.dto.RateLimitResult;
import com.sentinelflow.gateway.model.RateLimitTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
class DistributedRateLimiterServiceTest {

    private StringRedisTemplate stringRedisTemplate;
    private RedisScript<List> script;
    private DistributedRateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        script = mock(RedisScript.class);
        rateLimiterService = new DistributedRateLimiterService(stringRedisTemplate, script);
    }

    @Test
    void checkRateLimit_whenAllowed_returnsAllowedResult() {
        List<?> scriptOutput = List.of(1L, 99L, 0L, 100L);
        given(stringRedisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(), any(), any(), any(), any()
        )).willReturn(scriptOutput);

        RateLimitResult result = rateLimiterService.checkRateLimit("ip:127.0.0.1", RateLimitTier.FREE);

        assertThat(result.allowed()).isTrue();
        assertThat(result.limit()).isEqualTo(100);
        assertThat(result.remaining()).isEqualTo(99);
        assertThat(result.retryAfterSeconds()).isEqualTo(0);
    }

    @Test
    void checkRateLimit_whenDenied_returnsDeniedResult() {
        List<?> scriptOutput = List.of(0L, 0L, 15L, 100L);
        given(stringRedisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(), any(), any(), any(), any()
        )).willReturn(scriptOutput);

        RateLimitResult result = rateLimiterService.checkRateLimit("ip:127.0.0.1", RateLimitTier.FREE);

        assertThat(result.allowed()).isFalse();
        assertThat(result.limit()).isEqualTo(100);
        assertThat(result.remaining()).isEqualTo(0);
        assertThat(result.retryAfterSeconds()).isEqualTo(15);
    }

    @Test
    void checkRateLimit_whenRedisFails_failsOpenGracefully() {
        given(stringRedisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(), any(), any(), any(), any()
        )).willThrow(new RuntimeException("Redis connection timeout"));

        RateLimitResult result = rateLimiterService.checkRateLimit("ip:127.0.0.1", RateLimitTier.FREE);

        assertThat(result.allowed()).isTrue();
        assertThat(result.limit()).isEqualTo(100);
    }
}
