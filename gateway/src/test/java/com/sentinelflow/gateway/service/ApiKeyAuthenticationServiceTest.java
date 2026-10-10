package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.GatewaySecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthenticationServiceTest {

    private ApiKeyAuthenticationService apiKeyService;

    @BeforeEach
    void setUp() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        apiKeyService = new ApiKeyAuthenticationService(properties);
    }

    @Test
    void authenticateApiKey_withValidDefaultKey_returnsClientDefinition() {
        Optional<GatewaySecurityProperties.ApiKeyDefinition> client =
                apiKeyService.authenticateApiKey("sf-dev-m2m-key-2026");

        assertThat(client).isPresent();
        assertThat(client.get().getClientId()).isEqualTo("m2m-client-default");
        assertThat(client.get().getRole()).isEqualTo("PREMIUM");
    }

    @Test
    void authenticateApiKey_withInvalidKey_returnsEmpty() {
        Optional<GatewaySecurityProperties.ApiKeyDefinition> client =
                apiKeyService.authenticateApiKey("non-existent-secret-key");

        assertThat(client).isEmpty();
    }

    @Test
    void hashApiKey_producesConsistentSha256Hex() {
        String hash1 = apiKeyService.hashApiKey("secret-123");
        String hash2 = apiKeyService.hashApiKey("secret-123");

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // SHA-256 is 64 hex chars
    }
}
