-- PostgreSQL initial schema for SentinelFlow API Gateway
CREATE TABLE IF NOT EXISTS api_keys (
    id SERIAL PRIMARY KEY,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS request_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(64) NOT NULL,
    gateway_instance VARCHAR(32) NOT NULL,
    backend_instance VARCHAR(32),
    client_ip VARCHAR(45),
    user_id VARCHAR(64),
    http_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(255) NOT NULL,
    status_code INT NOT NULL,
    latency_ms BIGINT NOT NULL,
    rate_limit_status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_req_id ON request_audit_logs(request_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON request_audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_api_key_hash ON api_keys(key_hash);
