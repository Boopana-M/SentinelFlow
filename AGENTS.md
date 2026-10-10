# AGENTS.md
# SentinelFlow — Distributed API Traffic & Security Gateway

## 1. Project Objective
Build a production-style distributed API gateway and traffic management platform.

### Core Goals:
- Distributed rate limiting
- Health-aware load balancing
- Authentication and authorization
- Backend service health monitoring
- Automatic failure detection and failover
- Circuit breaking
- Request logging and observability
- Real-time monitoring dashboard
- Performance/load testing
- Docker-based multi-instance deployment

Prioritize correctness, clean architecture, distributed-systems concepts, fault tolerance, measurable performance, security, testability, and documentation.

---

## 2. Technology Stack
- **Backend**: Java 21, Spring Boot 3.x, Spring Web, Spring Security, Spring Data JPA, Spring Data Redis, PostgreSQL, Resilience4j, Maven.
- **Frontend**: React, TypeScript, Vite, Axios, Recharts.
- **Infrastructure**: Docker, Docker Compose, Redis, PostgreSQL, Prometheus, Grafana.
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers where appropriate, k6 or JMeter.

---

## 3. Core Architecture
```
React Dashboard
      ↓
API Gateway
      ↓
Authentication / Rate Limiter / Load Balancer
      ↓
Redis + Service Registry
      ↓
Backend-1 / Backend-2 / Backend-3
      ↓
PostgreSQL
```
The gateway is the main component and must handle:
- Authentication & Authorization
- Rate limiting
- Routing & Load balancing
- Health checking
- Circuit breaking
- Request IDs & Structured logging
- Metrics
- Failure handling

---

## 4. Distributed Rate Limiting
Implement a distributed token-bucket rate limiter using Redis.
Support limits by:
- User ID
- API key
- IP address
- Endpoint

### Example Tiers:
- `FREE`: 100 requests/minute
- `PREMIUM`: 1000 requests/minute
- `ADMIN`: 5000 requests/minute

All gateway instances must share Redis. Do not use an in-memory Java Map for distributed rate limiting.
Use atomic Redis operations or Lua scripts where necessary to prevent race conditions.
Return HTTP 429 with:
- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `X-RateLimit-Reset`
- `Retry-After`

---

## 5. Backend Services & Health Monitoring
Run at least three backend instances:
- backend-1 → 8081
- backend-2 → 8082
- backend-3 → 8083

Each exposes `GET /health`.
Maintain:
- Instance ID
- Host & Port
- Status (`UP`, `DOWN`)
- Last heartbeat
- Average latency
- Error rate

Use periodic health checks. Do not remove an instance after one failure. Use configurable thresholds such as 3 consecutive failures → `DOWN` and 3 successful checks → `UP`.

---

## 6. Load Balancing & Failure Handling
Implement:
1. Round Robin
2. Least Connections

Only route to healthy instances.

### When a backend fails:
1. Detect failure
2. Mark it `DOWN`
3. Stop routing traffic to it
4. Continue with healthy instances
5. Record the failure
6. Detect recovery
7. Re-register the instance
8. Resume traffic gradually

Use Resilience4j circuit breakers with `CLOSED`, `OPEN`, and `HALF_OPEN` states.
Retry only safe/idempotent requests by default. Use exponential backoff and prevent retry storms.

---

## 7. Security
- Implement JWT authentication and role-based authorization (`USER`, `PREMIUM`, `ADMIN`).
- Support API keys for machine-to-machine requests. Store only hashed API keys if persistent storage is used.
- Never log passwords, JWT tokens, API keys, or sensitive personal information.
- Use environment variables for secrets. Never commit secrets to Git.

---

## 8. Observability
- Every request must have `X-Request-ID`.
- Structured logs containing: timestamp, requestId, method, path, client, backendInstance, statusCode, latency, userId when appropriate, rate-limit status.
- Prometheus metrics:
  - `gateway_requests_total`
  - `gateway_request_latency`
  - `gateway_errors_total`
  - `rate_limit_allowed_total`
  - `rate_limit_blocked_total`
  - `backend_health_status`
  - `backend_request_latency`
  - `backend_active_requests`
  - `circuit_breaker_state`

---

## 9. Dashboard
Build a React dashboard showing:
- Requests/sec
- Total requests
- Error rate
- Average latency
- P95 latency
- Rate-limited requests
- Backend health
- Rate-limit activity
- Circuit-breaker states

The UI is for observability and administration (clean, dark mode, high readability).

---

## 10. Repository Structure
```
sentinelflow/
├── gateway/
├── backend-service/
├── dashboard/
├── infrastructure/
├── load-tests/
├── docs/
├── .gitignore
├── README.md
└── AGENTS.md
```

Docker Compose starts:
- gateway-1, gateway-2, gateway-3
- backend-1, backend-2, backend-3
- Redis
- PostgreSQL
- Prometheus
- Grafana
- Dashboard

Complete system starts with:
```bash
docker compose up --build
```

---

## 11. Testing & Benchmarking
Create k6 tests for:
1. Normal traffic
2. Burst traffic
3. Rate-limit behavior
4. Multi-instance behavior
5. Backend failure
6. Recovery

Measure:
- Throughput, average latency, P95/P99 latency, error rate, CPU, memory.
Compare 1 gateway instance vs 3 instances, and test behavior when a backend is killed.
Never fabricate benchmark results. Only report measurements actually obtained.

---

## 12. Implementation Order
- **PHASE 1**: Project setup
- **PHASE 2**: Basic Spring Boot gateway
- **PHASE 3**: Backend service
- **PHASE 4**: Gateway → backend routing
- **PHASE 5**: Redis integration
- **PHASE 6**: Distributed rate limiter
- **PHASE 7**: Multiple gateway instances
- **PHASE 8**: Health monitoring
- **PHASE 9**: Load balancing
- **PHASE 10**: Circuit breaker
- **PHASE 11**: JWT authentication
- **PHASE 12**: Authorization
- **PHASE 13**: Metrics
- **PHASE 14**: React dashboard
- **PHASE 15**: Docker Compose
- **PHASE 16**: Load testing
- **PHASE 17**: Failure testing
- **PHASE 18**: Documentation

*Do not move to the next phase until the previous phase works and has tests.*

---

## 13. Code Quality & Agent Rules
- Architecture: Controller → Service → Repository → Domain/Model → DTO → Configuration separation.
- Prefer constructor injection. Keep controllers thin.
- Avoid giant classes, duplicated logic, unnecessary abstractions, and hard-coded configuration.
- The coding agent must:
  1. Inspect the repository before modifying anything.
  2. Implement one phase at a time.
  3. Run tests after meaningful changes.
  4. Fix compilation errors immediately.
  5. Keep documentation synchronized.
  6. Never fabricate benchmark results.
  7. Never claim an untested feature is complete.
  8. Prefer simple, understandable solutions.

---

## 14. Definition of Done
The project is complete only when:
- Gateway works
- Backend services work
- Multiple gateway instances work
- Redis is shared across instances
- Distributed rate limiting works
- Concurrent rate-limit testing passes
- Health monitoring works
- Health-aware load balancing works
- Circuit breaker works
- Automatic failover and recovery work
- JWT authentication works
- RBAC works
- API keys work
- Request IDs and structured logging work
- Prometheus/Grafana metrics work
- React dashboard works
- Docker Compose works
- Load and failure tests exist
- Benchmark results are measured
- API and architecture documentation exist
- README is complete
- No secrets are committed
- Unit and integration tests pass
