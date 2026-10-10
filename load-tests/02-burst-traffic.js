import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    traffic_spike: {
      executor: 'ramping-arrival-rate',
      startRate: 20,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 300,
      stages: [
        { duration: '10s', target: 20 },   // Warm-up baseline: 20 req/s
        { duration: '5s', target: 200 },   // Sudden 10x traffic spike: 200 req/s
        { duration: '15s', target: 200 },  // Sustained burst window
        { duration: '10s', target: 20 },   // Return to baseline: 20 req/s
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1000', 'p(99)<2500'],
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${BASE_URL}/api/resource/public`, {
    headers: {
      'Accept': 'application/json',
      'X-Forwarded-For': `10.0.0.${Math.floor(Math.random() * 250) + 1}`,
    },
  });

  check(res, {
    'valid gateway response (200 or 429)': (r) => r.status === 200 || r.status === 429,
    'has request ID': (r) => r.headers['X-Request-Id'] !== undefined || r.headers['x-request-id'] !== undefined,
  });
}
