# SentinelFlow — Benchmarking & Performance Analysis 📊

## 1. Benchmark Methodology

All performance evaluations for SentinelFlow are structured to test resilience, throughput, and latency percentiles under multi-replica topologies.

### Test Environment Specifications:
- **JVM**: Java 21 LTS (Eclipse Temurin)
- **Framework**: Spring Boot 3.3.4
- **State Store**: Redis 7 Alpine
- **Persistence**: PostgreSQL 16 Alpine
- **Load Generation**: k6 v0.50+ / Node.js 22 Multi-Worker Benchmark Harness

---

## 2. Load Testing Scenarios

### Scenario 1: Normal Traffic Baseline
- **Concurrency**: 20 virtual users ramped over 10s, sustained for 20s.
- **Target**: `GET /api/resource/public`
- **Goal**: Evaluate single vs multi-gateway overhead, request correlation injection, and baseline latency.

### Scenario 2: High Concurrency Spike / Burst
- **Concurrency**: 200 virtual users burst in 5 seconds.
- **Target**: `GET /api/resource/public`
- **Goal**: Measure queue depth, buffer behavior, and P95 / P99 latency degradation under sudden 10x traffic spikes.

### Scenario 3: Token-Bucket Saturation & Tiering
- **Target**: FREE (100 req/min), PREMIUM (1,000 req/min), ADMIN (5,000 req/min)
- **Goal**: Verify atomicity of Redis Lua script execution without race conditions; verify accurate `HTTP 429` responses with `X-RateLimit-*` and `Retry-After` headers.

### Scenario 4: Multi-Gateway Distributed Coordination
- **Setup**: Traffic distributed across `gateway-1` (:8080), `gateway-2` (:8084), and `gateway-3` (:8085).
- **Goal**: Verify that rate-limiting quotas are correctly shared in Redis across multiple gateway nodes rather than counted independently.

### Scenario 5: Upstream Backend Failure & Circuit Tripping
- **Setup**: 3 Upstream Backends active. Kill or toggle one backend (`POST /health/toggle`).
- **Goal**: Measure failure detection time (3 failed probes), transition from `CLOSED` to `OPEN`, and automatic failover to remaining 2 backends.

### Scenario 6: Upstream Backend Recovery
- **Setup**: Reactivate the failed backend.
- **Goal**: Verify 3 consecutive successful probes transition the node back to `UP` and traffic resumes gracefully.

---

## 3. How to Run the Benchmark Harness

### Option A: Using k6
```bash
# Run baseline throughput & latency test
k6 run load-tests/01-normal-traffic.js

# Run burst test
k6 run load-tests/02-burst-traffic.js

# Run distributed multi-gateway test
k6 run load-tests/04-multi-instance.js
```

### Option B: Using Node.js Automated Harness
```bash
node load-tests/run-benchmarks.mjs
```

---

## 4. Key Metrics Measured

| Metric | Source | Target SLI / SLA |
| :--- | :--- | :--- |
| **Throughput (req/sec)** | `gateway_requests_total` | > 1,500 req/s per gateway instance |
| **P50 Latency** | `gateway_request_latency` | < 10ms proxy overhead |
| **P95 Latency** | `gateway_request_latency` | < 50ms |
| **P99 Latency** | `gateway_request_latency` | < 150ms under peak burst |
| **Failover Convergence** | Health Check Monitor | < 15s (3 intervals × 5s) |
| **Rate Limiter Precision** | Redis Lua Engine | 100% atomic token decrement |
