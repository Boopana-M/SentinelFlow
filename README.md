# SentinelFlow 🛡️

> Distributed API Traffic & Security Gateway built with Java 21, Spring Boot 3.x, Redis, and React.

## Overview
**SentinelFlow** is a production-grade distributed API gateway and traffic management platform designed for high-throughput, fault-tolerant, and secure service communication.

### Key Capabilities
- **Distributed Token-Bucket Rate Limiter**: Redis-backed atomic rate limiting with tiering (`FREE`, `PREMIUM`, `ADMIN`) across multiple gateway replicas.
- **Health-Aware Load Balancing**: Dynamic Round-Robin and Least-Connections routing with periodic health probes and configurable failure/recovery thresholds.
- **Resilience & Circuit Breaking**: Circuit breaking with Resilience4j (CLOSED, OPEN, HALF-OPEN states) and failover mechanisms.
- **Security & Authorization**: JWT token verification, Role-Based Access Control (RBAC), and hashed API-key support.
- **Full Observability**: Request correlation via `X-Request-ID`, structured JSON logging, Prometheus metrics, and a React dark-mode dashboard.
- **Cloud-Native Deployment**: Multi-instance Docker Compose architecture with Redis, PostgreSQL, Prometheus, and Grafana.

---

## Architecture
```
[ React Dashboard ]
        │
        ▼
[ API Gateway (Replicas 1..N) ] ─── [ Redis Cache / Distributed Rate Limiter ]
        │
        ├── Authentication & Authorization (JWT / API Key)
        ├── Health-Aware Load Balancer (Round Robin / Least Connections)
        ├── Resilience4j Circuit Breaker
        │
        ▼
[ Backend Services (1..N) ] ─── [ PostgreSQL Database ]
```

---

## Monorepo Structure
```
sentinelflow/
├── gateway/           # Spring Boot 3.x API Gateway
├── backend-service/   # Sample upstream microservices
├── dashboard/         # React + TypeScript + Vite Monitoring Dashboard
├── infrastructure/    # Docker Compose, Prometheus, Grafana configs
├── load-tests/        # k6 load testing scripts & benchmarks
├── docs/              # Architectural diagrams and API documentation
├── .gitignore
├── README.md
└── AGENTS.md          # Architectural standards & implementation roadmap
```

---

## Development Phases
- [x] **Phase 1: Project Setup & Repository Scaffolding**
- [x] **Phase 2: Basic Spring Boot Gateway**
- [x] **Phase 3: Backend Service**
- [x] **Phase 4: Gateway → Backend Routing**
- [x] **Phase 5: Redis Integration**
- [x] **Phase 6: Distributed Rate Limiter**
- [x] **Phase 7: Multiple Gateway Instances**
- [x] **Phase 8: Health Monitoring**
- [x] **Phase 9: Load Balancing**
- [x] **Phase 10: Circuit Breaker**
- [x] **Phase 11: JWT Authentication**
- [x] **Phase 12: Authorization (RBAC & API Keys)**
- [x] **Phase 13: Observability & Prometheus Metrics**
- [x] **Phase 14: React Dashboard**
- [x] **Phase 15: Docker Compose Orchestration**
- [x] **Phase 16: Load Testing with k6**
- [x] **Phase 17: Fault Tolerance & Chaos Testing**
- [ ] Phase 18: Documentation & Benchmarks

---

## Quick Start with Docker Compose
Spin up the complete 11-node distributed cluster with a single command:
```bash
docker compose up --build
```

### Cluster Components
| Service | Role | Internal Port | Host Port |
| :--- | :--- | :--- | :--- |
| `gateway-1` | Distributed API Gateway (Leader) | 8080 | 8080 |
| `gateway-2` | Distributed API Gateway Replica | 8084 | 8084 |
| `gateway-3` | Distributed API Gateway Replica | 8085 | 8085 |
| `backend-1` | Upstream Microservice 1 | 8081 | 8081 |
| `backend-2` | Upstream Microservice 2 | 8082 | 8082 |
| `backend-3` | Upstream Microservice 3 | 8083 | 8083 |
| `redis` | Distributed State & Rate Limiter | 6379 | 6379 |
| `postgres` | Audit & Relational Store | 5432 | 5432 |
| `prometheus` | Metrics Scraper & TSDB | 9090 | 9090 |
| `grafana` | Metrics Dashboard | 3000 | 3001 |
| `dashboard` | React Monitoring Dashboard | 80 | 3000 |

