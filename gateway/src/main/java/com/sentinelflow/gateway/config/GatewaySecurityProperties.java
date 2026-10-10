package com.sentinelflow.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

    private JwtConfig jwt = new JwtConfig();
    /**
     * Map of hashed API Key (SHA-256 in hex) -> Client Definition (clientId, role)
     */
    private Map<String, ApiKeyDefinition> apiKeys = new HashMap<>();

    public JwtConfig getJwt() {
        return jwt;
    }

    public void setJwt(JwtConfig jwt) {
        this.jwt = jwt;
    }

    public Map<String, ApiKeyDefinition> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(Map<String, ApiKeyDefinition> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public static class JwtConfig {
        // Minimum 256-bit HMAC key
        private String secret = "SentinelFlowDistributedGatewayUltraSecureSecretKey2026!@#$%^&*()_+";
        private long expirationMs = 86400000L; // 24 hours

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMs() {
            return expirationMs;
        }

        public void setExpirationMs(long expirationMs) {
            this.expirationMs = expirationMs;
        }
    }

    public static class ApiKeyDefinition {
        private String clientId;
        private String role = "PREMIUM";

        public ApiKeyDefinition() {}

        public ApiKeyDefinition(String clientId, String role) {
            this.clientId = clientId;
            this.role = role;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
