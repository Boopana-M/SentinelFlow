package com.sentinelflow.gateway.service;

import com.sentinelflow.gateway.config.GatewaySecurityProperties;
import com.sentinelflow.gateway.dto.AuthRequest;
import com.sentinelflow.gateway.dto.AuthResponse;
import com.sentinelflow.gateway.model.UserRole;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final GatewaySecurityProperties securityProperties;

    // Hardened demo credentials with encrypted hashes
    private final Map<String, UserCredentials> users;

    public AuthService(JwtTokenService jwtTokenService,
                       PasswordEncoder passwordEncoder,
                       GatewaySecurityProperties securityProperties) {
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;

        this.users = Map.of(
                "user", new UserCredentials(passwordEncoder.encode("password"), UserRole.USER),
                "premium", new UserCredentials(passwordEncoder.encode("password"), UserRole.PREMIUM),
                "admin", new UserCredentials(passwordEncoder.encode("admin123"), UserRole.ADMIN)
        );
    }

    public AuthResponse login(AuthRequest request) {
        UserCredentials credentials = users.get(request.username().toLowerCase());
        if (credentials == null || !passwordEncoder.matches(request.password(), credentials.passwordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtTokenService.generateToken(request.username(), credentials.role());
        return AuthResponse.bearer(
                token,
                request.username(),
                credentials.role().name(),
                securityProperties.getJwt().getExpirationMs()
        );
    }

    private record UserCredentials(String passwordHash, UserRole role) {}
}
