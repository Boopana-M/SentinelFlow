import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 20 }, // Sustained load as node recovers
    { duration: '20s', target: 30 }, // Verification phase
    { duration: '10s', target: 0 },  // Ramp-down
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<400'],
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${BASE_URL}/api/resource/public`, {
    headers: {
      'Accept': 'application/json',
      'X-Forwarded-For': `172.16.1.${__VU}`,
    },
  });

  check(res, {
    '200 OK after full recovery': (r) => r.status === 200,
    'backend instance routed': (r) => r.headers['X-Backend-Instance'] !== undefined || r.headers['x-backend-instance'] !== undefined,
  });

  sleep(0.05);
}
