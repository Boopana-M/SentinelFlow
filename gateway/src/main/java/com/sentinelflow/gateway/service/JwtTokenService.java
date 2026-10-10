package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.GatewaySecurityProperties;
import com.sentinelflow.gateway.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final GatewaySecurityProperties securityProperties;
    private final SecretKey signingKey;

    public JwtTokenService(GatewaySecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        byte[] keyBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, UserRole role) {
        long nowMs = System.currentTimeMillis();
        long expirationMs = securityProperties.getJwt().getExpirationMs();
        Date issuedAt = new Date(nowMs);
        Date expiration = new Date(nowMs + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public UserRole extractRole(String token) {
        String roleStr = extractClaims(token).get("role", String.class);
        if (roleStr != null) {
            try {
                return UserRole.valueOf(roleStr);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return UserRole.USER;
    }
}
