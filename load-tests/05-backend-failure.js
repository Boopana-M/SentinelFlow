import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 15 }, // Normal steady state
    { duration: '20s', target: 25 }, // Load during induced backend failure
    { duration: '10s', target: 0 },  // Ramp down
  ],
  thresholds: {
    // Under failure, requests to healthy backends should still succeed
    http_req_failed: ['rate<0.10'], // Less than 10% failure during failover convergence
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${BASE_URL}/api/resource/public`, {
    headers: {
      'Accept': 'application/json',
      'X-Forwarded-For': `172.16.0.${__VU}`,
    },
  });

  check(res, {
    'successful route or circuit breaker response': (r) => r.status === 200 || r.status === 503,
    'has request ID': (r) => r.headers['X-Request-Id'] !== undefined || r.headers['x-request-id'] !== undefined,
  });

  sleep(0.1);
}
