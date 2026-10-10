package com.sentinelflow.gateway.filter;

import com.sentinelflow.gateway.config.GatewaySecurityProperties;
import com.sentinelflow.gateway.model.UserRole;
import com.sentinelflow.gateway.service.ApiKeyAuthenticationService;
import com.sentinelflow.gateway.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final ApiKeyAuthenticationService apiKeyAuthService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   ApiKeyAuthenticationService apiKeyAuthService) {
        this.jwtTokenService = jwtTokenService;
        this.apiKeyAuthService = apiKeyAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String apiKeyHeader = request.getHeader("X-API-Key");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtTokenService.validateToken(token)) {
                String username = jwtTokenService.extractUsername(token);
                UserRole role = jwtTokenService.extractRole(token);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } else if (StringUtils.hasText(apiKeyHeader)) {
            Optional<GatewaySecurityProperties.ApiKeyDefinition> apiKeyOpt =
                    apiKeyAuthService.authenticateApiKey(apiKeyHeader);

            if (apiKeyOpt.isPresent()) {
                GatewaySecurityProperties.ApiKeyDefinition def = apiKeyOpt.get();
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        def.getClientId(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + def.getRole()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
