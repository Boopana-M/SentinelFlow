import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { duration: '15s', target: 30 },
    { duration: '30s', target: 30 },
    { duration: '15s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<800'],
  },
};

const GATEWAYS = [
  __ENV.GATEWAY_1_URL || 'http://localhost:8080',
  __ENV.GATEWAY_2_URL || 'http://localhost:8084',
  __ENV.GATEWAY_3_URL || 'http://localhost:8085',
];

export default function () {
  // Round-robin or random distribution across gateway replicas
  const targetGateway = GATEWAYS[Math.floor(Math.random() * GATEWAYS.length)];
  
  // Shared client IP across all requests to test distributed rate limiting in Redis
  const sharedClientIp = '203.0.113.88';

  const res = http.get(`${targetGateway}/api/resource/public`, {
    headers: {
      'Accept': 'application/json',
      'X-Forwarded-For': sharedClientIp,
    },
  });

  check(res, {
    'valid status 200 or 429': (r) => r.status === 200 || r.status === 429,
    'has gateway instance header': (r) => r.headers['X-Gateway-Instance'] !== undefined || r.headers['x-gateway-instance'] !== undefined,
    'has backend instance header': (r) => r.headers['X-Backend-Instance'] !== undefined || r.headers['x-backend-instance'] !== undefined,
  });
}
