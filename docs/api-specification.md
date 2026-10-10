# SentinelFlow — API Specification & Endpoints 📋

This document details the REST API specifications for SentinelFlow Gateway, Upstream Backend Microservices, Authentication, and Health Monitoring.

---

## 1. Gateway Authentication Endpoints

### Generate JWT Token
- **Method**: `POST`
- **Path**: `/api/auth/token`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "userId": "user-123",
  "role": "PREMIUM"
}
```
- **Response**: `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "user-123",
  "role": "PREMIUM",
  "expiresInMs": 86400000
}
```

### Create / Register API Key
- **Method**: `POST`
- **Path**: `/api/auth/api-key`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "userId": "app-service-1",
  "role": "ADMIN"
}
```
- **Response**: `200 OK`
```json
{
  "apiKey": "sf_live_a1b2c3d4e5f6...",
  "userId": "app-service-1",
  "role": "ADMIN",
  "keyHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
}
```

---

## 2. Protected Gateway Routes (Load Balanced to Upstreams)

### Public Resource
- **Method**: `GET`
- **Path**: `/api/resource/public`
- **Auth**: None required (evaluated under `FREE` IP-based rate limiting).
- **Response**: `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Public resource accessed successfully",
  "instance": "backend-1",
  "timestamp": "2026-10-10T16:00:00.000Z"
}
```

### User Resource
- **Method**: `GET`
- **Path**: `/api/resource/user`
- **Auth**: Bearer JWT or API Key with role `USER`, `PREMIUM`, or `ADMIN`.
- **Response**: `200 OK` or `401 Unauthorized` / `403 Forbidden`.

### Premium Resource
- **Method**: `GET`
- **Path**: `/api/resource/premium`
- **Auth**: Role `PREMIUM` or `ADMIN`.

### Admin Resource
- **Method**: `GET`
- **Path**: `/api/resource/admin`
- **Auth**: Role `ADMIN`.

---

## 3. Rate Limit Response Contract

When a client exceeds tier quota, SentinelFlow responds with `HTTP 429 Too Many Requests`:

### Response Headers:
- `X-Request-ID`: e.g. `c7b0e1d8-32a1-4ef4-9e73-b26a11e4f201`
- `X-Gateway-Instance`: e.g. `gateway-1`
- `X-RateLimit-Limit`: Maximum bucket capacity (e.g. `100`)
- `X-RateLimit-Remaining`: `0`
- `X-RateLimit-Reset`: Milliseconds remaining until next token replenishment
- `Retry-After`: Seconds to wait before retry (e.g. `60`)

### Response Body:
```json
{
  "error": "TOO_MANY_REQUESTS",
  "message": "Rate limit exceeded for client: 198.51.100.42. Please try again later.",
  "status": 429,
  "timestamp": 1791628800000,
  "retryAfterSeconds": 60
}
```

---

## 4. Upstream Backend Diagnostics & Chaos Endpoints

Each backend microservice (`backend-1`, `backend-2`, `backend-3`) exposes:

### Health Probe
- **Method**: `GET`
- **Path**: `/health`
- **Response**: `200 OK` (if healthy) or `503 Service Unavailable` (if simulated failure toggled).
```json
{
  "instanceId": "backend-1",
  "status": "UP",
  "port": 8081,
  "timestamp": "2026-10-10T16:00:00.000Z"
}
```

### Chaos: Health Toggle
- **Method**: `POST`
- **Path**: `/health/toggle`
- **Description**: Inverts status between `UP` and `DOWN` for chaos and failover testing.

### Fault: Injected Latency
- **Method**: `GET`
- **Path**: `/resource/fault/latency?delayMs=1500`
- **Description**: Simulates delayed response to test circuit breaker timeouts and slow upstream handling.

### Fault: Injected Error
- **Method**: `GET`
- **Path**: `/resource/fault/error`
- **Description**: Injects HTTP 500 error to test circuit breaker failure rate accumulation.

---

## 5. Gateway Actuator & Observability Endpoints

- `GET /actuator/health` — Full health details including Redis and circuit breaker status.
- `GET /actuator/prometheus` — Prometheus scrape target exposing metrics.
- `GET /api/status` — Gateway summary including replica instance ID and load balancer state.
- `GET /api/admin/backends` — Health status table of all backend instances.
- `POST /api/admin/backends/check-now` — Trigger immediate health probe.
