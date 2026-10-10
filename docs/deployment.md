# SentinelFlow — Deployment & Operations Guide 🚀

## 1. Quick Start with Docker Compose

To deploy the full 11-node distributed environment:

```bash
docker compose up --build
```

### Deployed Services:
| Container Name | Service | Port | Description |
| :--- | :--- | :--- | :--- |
| `sentinelflow-gateway-1` | Gateway Replica 1 | `8080` | Distributed Gateway Leader |
| `sentinelflow-gateway-2` | Gateway Replica 2 | `8084` | Gateway Worker Replica |
| `sentinelflow-gateway-3` | Gateway Replica 3 | `8085` | Gateway Worker Replica |
| `sentinelflow-backend-1` | Microservice 1 | `8081` | Upstream service replica |
| `sentinelflow-backend-2` | Microservice 2 | `8082` | Upstream service replica |
| `sentinelflow-backend-3` | Microservice 3 | `8083` | Upstream service replica |
| `sentinelflow-redis` | Redis 7 | `6379` | Distributed State Store & Token Bucket |
| `sentinelflow-postgres` | PostgreSQL 16 | `5432` | Relational Store & Audit Logging |
| `sentinelflow-prometheus` | Prometheus | `9090` | Metrics collection engine |
| `sentinelflow-grafana` | Grafana | `3001` | Preconfigured metrics visualization |
| `sentinelflow-dashboard` | React Dashboard | `3000` | Real-time Dark-Mode UI |

---

## 2. Environment Variables Configuration

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | Port for Gateway or Backend |
| `GATEWAY_INSTANCE_ID` | `gateway-1` | Unique replica node identifier |
| `REDIS_HOST` | `localhost` / `redis` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `JWT_SECRET` | `SentinelFlowDistributed...` | HMAC-SHA256 signing secret |
| `BACKEND_1_HOST` | `backend-1` / `localhost`| Upstream instance 1 host |
| `BACKEND_1_PORT` | `8081` | Upstream instance 1 port |
| `BACKEND_2_HOST` | `backend-2` / `localhost`| Upstream instance 2 host |
| `BACKEND_2_PORT` | `8082` | Upstream instance 2 port |
| `BACKEND_3_HOST` | `backend-3` / `localhost`| Upstream instance 3 host |
| `BACKEND_3_PORT` | `8083` | Upstream instance 3 port |

---

## 3. Local Development (Without Docker)

### Run Backend Microservices:
```bash
# Terminal 1: Backend 1
mvn spring-boot:run -pl backend-service -Dspring-boot.run.arguments="--server.port=8081 --backend.instance-id=backend-1"

# Terminal 2: Backend 2
mvn spring-boot:run -pl backend-service -Dspring-boot.run.arguments="--server.port=8082 --backend.instance-id=backend-2"

# Terminal 3: Backend 3
mvn spring-boot:run -pl backend-service -Dspring-boot.run.arguments="--server.port=8083 --backend.instance-id=backend-3"
```

### Run Redis:
```bash
docker run -p 6379:6379 redis:7-alpine
```

### Run Gateway:
```bash
mvn spring-boot:run -pl gateway
```

### Run React Dashboard:
```bash
cd dashboard
npm install
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.
