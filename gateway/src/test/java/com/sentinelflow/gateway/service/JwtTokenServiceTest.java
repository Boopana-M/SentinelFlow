package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.GatewaySecurityProperties;
import com.sentinelflow.gateway.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    void generateAndValidateToken_success() {
        String token = jwtTokenService.generateToken("alice", UserRole.PREMIUM);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenService.validateToken(token)).isTrue();
        assertThat(jwtTokenService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtTokenService.extractRole(token)).isEqualTo(UserRole.PREMIUM);
    }

    @Test
    void validateToken_rejectsInvalidOrTamperedToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload.invalidsignature";
        assertThat(jwtTokenService.validateToken(invalidToken)).isFalse();
    }
}
