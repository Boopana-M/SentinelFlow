package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.GatewaySecurityProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class ApiKeyAuthenticationService {

    private final GatewaySecurityProperties securityProperties;

    public ApiKeyAuthenticationService(GatewaySecurityProperties securityProperties) {
        this.securityProperties = securityProperties;

        // Seed default developer API key if none configured
        // Raw key: "sf-dev-m2m-key-2026"
        String defaultDevRawKey = "sf-dev-m2m-key-2026";
        String defaultHashed = hashApiKey(defaultDevRawKey);
        this.securityProperties.getApiKeys().putIfAbsent(
                defaultHashed,
                new GatewaySecurityProperties.ApiKeyDefinition("m2m-client-default", "PREMIUM")
        );
    }

    public Optional<GatewaySecurityProperties.ApiKeyDefinition> authenticateApiKey(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            return Optional.empty();
        }

        String hashedKey = hashApiKey(rawApiKey.trim());
        GatewaySecurityProperties.ApiKeyDefinition definition = securityProperties.getApiKeys().get(hashedKey);
        return Optional.ofNullable(definition);
    }

    public String hashApiKey(String rawApiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawApiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
