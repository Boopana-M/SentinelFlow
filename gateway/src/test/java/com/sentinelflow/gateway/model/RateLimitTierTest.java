package com.sentinelflow.gateway.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitTierTest {

    @Test
    void tierConfigurationsAreCorrect() {
        assertThat(RateLimitTier.FREE.getCapacity()).isEqualTo(100);
        assertThat(RateLimitTier.PREMIUM.getCapacity()).isEqualTo(1000);
        assertThat(RateLimitTier.ADMIN.getCapacity()).isEqualTo(5000);

        assertThat(RateLimitTier.FREE.getWindowSeconds()).isEqualTo(60);
        assertThat(RateLimitTier.PREMIUM.getWindowSeconds()).isEqualTo(60);
        assertThat(RateLimitTier.ADMIN.getWindowSeconds()).isEqualTo(60);

        assertThat(RateLimitTier.FREE.getRefillRatePerMs()).isGreaterThan(0);
    }

    @Test
    void fromRoleOrHeader_resolvesExpectedTiers() {
        assertThat(RateLimitTier.fromRoleOrHeader("ROLE_PREMIUM")).isEqualTo(RateLimitTier.PREMIUM);
        assertThat(RateLimitTier.fromRoleOrHeader("ADMIN")).isEqualTo(RateLimitTier.ADMIN);
        assertThat(RateLimitTier.fromRoleOrHeader("USER")).isEqualTo(RateLimitTier.FREE);
        assertThat(RateLimitTier.fromRoleOrHeader(null)).isEqualTo(RateLimitTier.FREE);
    }
}
