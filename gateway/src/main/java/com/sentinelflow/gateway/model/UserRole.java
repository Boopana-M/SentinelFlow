package com.sentinelflow.gateway.model;

public enum UserRole {
    USER,
    PREMIUM,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
