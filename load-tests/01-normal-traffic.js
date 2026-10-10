import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 20 },  // Ramp-up to 20 virtual users
    { duration: '20s', target: 20 },  // Sustained steady load
    { duration: '10s', target: 0 },   // Graceful ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // Latency thresholds
    http_req_failed: ['rate<0.01'],                  // < 1% error rate
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${BASE_URL}/api/resource/public`, {
    headers: {
      'Accept': 'application/json',
      'X-Forwarded-For': '192.168.1.100',
    },
  });

  check(res, {
    'status is 200 OK': (r) => r.status === 200,
    'has X-Request-ID': (r) => r.headers['X-Request-Id'] !== undefined || r.headers['x-request-id'] !== undefined,
    'has X-Backend-Instance': (r) => r.headers['X-Backend-Instance'] !== undefined || r.headers['x-backend-instance'] !== undefined,
    'has rate limit header': (r) => r.headers['X-Ratelimit-Limit'] !== undefined || r.headers['x-ratelimit-limit'] !== undefined,
  });

  sleep(0.05);
}
