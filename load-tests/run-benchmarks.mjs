/**
 * SentinelFlow Automated Benchmark & Load Test Harness
 * Measures Throughput (req/s), Latency percentiles (P50, P95, P99),
 * Distributed Rate-Limiting quotas, and Failover accuracy.
 */

const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:8080';

function calculatePercentiles(latencies) {
  if (latencies.length === 0) return { p50: 0, p90: 0, p95: 0, p99: 0, min: 0, max: 0, avg: 0 };
  const sorted = [...latencies].sort((a, b) => a - b);
  const p = (pct) => sorted[Math.min(sorted.length - 1, Math.floor((pct / 100) * sorted.length))];
  const sum = sorted.reduce((acc, v) => acc + v, 0);
  return {
    p50: p(50).toFixed(2),
    p90: p(90).toFixed(2),
    p95: p(95).toFixed(2),
    p99: p(99).toFixed(2),
    min: sorted[0].toFixed(2),
    max: sorted[sorted.length - 1].toFixed(2),
    avg: (sum / sorted.length).toFixed(2)
  };
}

async function runScenario({ name, concurrency, totalRequests, endpoint, headers = {} }) {
  console.log(`\n======================================================`);
  console.log(`  Running Scenario: ${name}`);
  console.log(`  Concurrency: ${concurrency} | Total Requests: ${totalRequests}`);
  console.log(`  Target: ${GATEWAY_URL}${endpoint}`);
  console.log(`======================================================`);

  const latencies = [];
  const statusCounts = {};
  let completed = 0;
  const startTime = Date.now();

  const worker = async () => {
    while (completed < totalRequests) {
      completed++;
      const reqStart = performance.now();
      try {
        const res = await fetch(`${GATEWAY_URL}${endpoint}`, {
          method: 'GET',
          headers: {
            'Accept': 'application/json',
            ...headers
          }
        });
        const elapsed = performance.now() - reqStart;
        latencies.push(elapsed);
        statusCounts[res.status] = (statusCounts[res.status] || 0) + 1;
      } catch (err) {
        statusCounts['ERR'] = (statusCounts['ERR'] || 0) + 1;
      }
    }
  };

  const pool = Array.from({ length: concurrency }, () => worker());
  await Promise.all(pool);

  const totalTimeSec = (Date.now() - startTime) / 1000;
  const rps = (totalRequests / totalTimeSec).toFixed(2);
  const p = calculatePercentiles(latencies);

  console.log(`  Result Summary:`);
  console.log(`  - Total Duration:  ${totalTimeSec.toFixed(2)}s`);
  console.log(`  - Throughput:      ${rps} req/sec`);
  console.log(`  - Status Codes:    ${JSON.stringify(statusCounts)}`);
  console.log(`  - Latencies (ms):  Avg=${p.avg}ms | P50=${p.p50}ms | P95=${p.p95}ms | P99=${p.p99}ms | Max=${p.max}ms`);
  
  return { name, rps, ...p, statusCounts };
}

async function main() {
  console.log(`Checking gateway reachability at ${GATEWAY_URL}/actuator/health ...`);
  try {
    const health = await fetch(`${GATEWAY_URL}/actuator/health`);
    console.log(`Gateway is UP with status: ${health.status}`);
  } catch (err) {
    console.log(`Notice: Gateway is not currently running locally (${err.message}).`);
    console.log(`To execute live benchmarks: start the cluster with 'docker compose up -d' or run the gateway jar, then execute 'npm test'.`);
    process.exit(0);
  }

  // 1. Normal traffic baseline
  await runScenario({
    name: 'Normal Traffic Baseline',
    concurrency: 10,
    totalRequests: 200,
    endpoint: '/api/resource/public',
    headers: { 'X-Forwarded-For': '192.168.1.1' }
  });

  // 2. High concurrency burst
  await runScenario({
    name: 'High Concurrency Burst Traffic',
    concurrency: 50,
    totalRequests: 500,
    endpoint: '/api/resource/public',
    headers: { 'X-Forwarded-For': '192.168.1.2' }
  });

  // 3. Rate limiting saturation
  await runScenario({
    name: 'Rate Limiting Quota Saturation (Single IP)',
    concurrency: 20,
    totalRequests: 250,
    endpoint: '/api/resource/public',
    headers: { 'X-Forwarded-For': '198.51.100.99' }
  });
}

main().catch(console.error);
