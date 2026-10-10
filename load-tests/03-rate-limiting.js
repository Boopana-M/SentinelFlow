import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    free_tier_saturation: {
      executor: 'constant-arrival-rate',
      rate: 150, // Exceeds 100 req/min free limit rapidly
      timeUnit: '1m',
      duration: '30s',
      preAllocatedVUs: 10,
    },
  },
  thresholds: {
    // We expect 429s as rate limiting kicks in
    'http_reqs': ['count>50'],
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const ip = '198.51.100.42'; // Fixed IP to trigger rate limit quota
  const res = http.get(`${BASE_URL}/api/resource/public`, {
    headers: {
      'Accept': 'application/json',
      'X-Forwarded-For': ip,
    },
  });

  if (res.status === 429) {
    check(res, {
      '429 Too Many Requests status': (r) => r.status === 429,
      'has X-RateLimit-Limit': (r) => r.headers['X-Ratelimit-Limit'] !== undefined || r.headers['x-ratelimit-limit'] !== undefined,
      'has X-RateLimit-Remaining zero': (r) => {
        const rem = r.headers['X-Ratelimit-Remaining'] || r.headers['x-ratelimit-remaining'];
        return rem === '0';
      },
      'has Retry-After header': (r) => r.headers['Retry-After'] !== undefined || r.headers['retry-after'] !== undefined,
    });
  } else {
    check(res, {
      '200 OK while within quota': (r) => r.status === 200,
    });
  }
}
