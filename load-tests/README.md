# SentinelFlow Load Testing & Benchmark Suite 🚀

This directory contains comprehensive k6 and automated benchmark suites designed to evaluate:
- **Baseline Throughput & Latency**
- **Burst Traffic Resilience**
- **Distributed Token-Bucket Rate Limiter Quotas**
- **Multi-Instance Cluster Scalability**
- **Upstream Node Failure & Circuit Breaker Tripping**
- **Dynamic Health Recovery & Traffic Resumption**

---

## Benchmark Scripts

| Script | Purpose | Execution Target | Expected Behavior |
| :--- | :--- | :--- | :--- |
| [`01-normal-traffic.js`](file:///d:/SentinelFlow/load-tests/01-normal-traffic.js) | Baseline load test | Gateway (`:8080`) | 100% 200 OK, P95 < 500ms, low variance |
| [`02-burst-traffic.js`](file:///d:/SentinelFlow/load-tests/02-burst-traffic.js) | Spike & burst stress test | Gateway (`:8080`) | Absorbs 10x traffic spikes, P99 < 2000ms |
| [`03-rate-limiting.js`](file:///d:/SentinelFlow/load-tests/03-rate-limiting.js) | Rate-limit tier verification | Gateway (`:8080`) | Enforces 100 req/min, emits 429 & `Retry-After` headers |
| [`04-multi-instance.js`](file:///d:/SentinelFlow/load-tests/04-multi-instance.js) | Multi-replica distributed test | Replicas (`:8080`, `:8084`, `:8085`) | Shared Redis bucket atomic limiting across replicas |
| [`05-backend-failure.js`](file:///d:/SentinelFlow/load-tests/05-backend-failure.js) | Chaos & node failure test | Gateway (`:8080`) | Circuit breaker transitions CLOSED → OPEN, rerouting traffic |
| [`06-recovery.js`](file:///d:/SentinelFlow/load-tests/06-recovery.js) | Health recovery test | Gateway (`:8080`) | Detects UP after 3 healthy probes, resumes routing |

---

## Running with k6

To execute any script with [k6](https://k6.io/):

```bash
# 1. Normal traffic baseline
k6 run load-tests/01-normal-traffic.js

# 2. Burst traffic test
k6 run load-tests/02-burst-traffic.js

# 3. Rate limiting saturation
k6 run load-tests/03-rate-limiting.js

# 4. Multi-instance cluster test
k6 run load-tests/04-multi-instance.js

# 5. Upstream failure simulation
k6 run load-tests/05-backend-failure.js

# 6. Recovery verification
k6 run load-tests/06-recovery.js
```

---

## Running Automated Node.js Benchmark Runner

If k6 is not installed on your system, you can use the built-in Node.js runner:

```bash
# Run automated benchmark suite
node load-tests/run-benchmarks.mjs
```
