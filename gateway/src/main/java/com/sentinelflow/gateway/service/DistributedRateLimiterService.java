package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.dto.RateLimitResult;
import com.sentinelflow.gateway.model.RateLimitTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class DistributedRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(DistributedRateLimiterService.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<List> tokenBucketScript;

    public DistributedRateLimiterService(StringRedisTemplate stringRedisTemplate,
                                         RedisScript<List> tokenBucketScript) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenBucketScript = tokenBucketScript;
    }

    public RateLimitResult checkRateLimit(String clientIdentifier, RateLimitTier tier) {
        return checkRateLimit(clientIdentifier, tier, 1);
    }

    public RateLimitResult checkRateLimit(String clientIdentifier, RateLimitTier tier, int requestedTokens) {
        String key = "sentinelflow:ratelimit:" + clientIdentifier;
        long capacity = tier.getCapacity();
        double refillRatePerMs = tier.getRefillRatePerMs();
        long nowMs = Instant.now().toEpochMilli();
        long ttlSeconds = tier.getWindowSeconds() * 2; // Key expiry margin

        try {
            List<?> result = stringRedisTemplate.execute(
                    tokenBucketScript,
                    Collections.singletonList(key),
                    String.valueOf(capacity),
                    String.valueOf(refillRatePerMs),
                    String.valueOf(requestedTokens),
                    String.valueOf(nowMs),
                    String.valueOf(ttlSeconds)
            );

            if (result != null && result.size() >= 4) {
                long allowedNum = ((Number) result.get(0)).longValue();
                long remaining = ((Number) result.get(1)).longValue();
                long retryAfter = ((Number) result.get(2)).longValue();
                long resetSeconds = tier.getWindowSeconds();

                boolean isAllowed = allowedNum == 1;
                log.debug("Rate limit check for key={} allowed={} remaining={} retryAfter={}",
                        key, isAllowed, remaining, retryAfter);

                return isAllowed
                        ? RateLimitResult.allowed(capacity, remaining, resetSeconds)
                        : RateLimitResult.denied(capacity, remaining, retryAfter, resetSeconds);
            }
        } catch (Exception ex) {
            log.warn("Redis rate limiter failed for key={}, failing open: {}", key, ex.getMessage());
            // Fail open gracefully when Redis connection has issues
            return RateLimitResult.allowed(capacity, capacity - 1, tier.getWindowSeconds());
        }

        return RateLimitResult.allowed(capacity, capacity - 1, tier.getWindowSeconds());
    }
}
