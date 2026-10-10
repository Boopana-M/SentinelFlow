export interface GatewayMetrics {
  requestsPerSec: number;
  totalRequests: number;
  errorRate: number;
  avgLatencyMs: number;
  p95LatencyMs: number;
  rateLimitedRequests: number;
}

export interface BackendInstance {
  id: string;
  host: string;
  port: number;
  baseUrl: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  lastHeartbeat: string;
  averageLatencyMs: number;
  activeRequests: number;
  errorRate: number;
  consecutiveFailures: number;
  consecutiveSuccesses: number;
}

export interface CircuitBreakerStatus {
  name: string;
  state: 'CLOSED' | 'OPEN' | 'HALF_OPEN' | 'DISABLED';
  failureRate: number;
  slowCallRate: number;
  numberOfBufferedCalls: number;
  numberOfFailedCalls: number;
  numberOfSuccessfulCalls: number;
  numberOfNotPermittedCalls: number;
}

export interface TrafficPoint {
  time: string;
  requests: number;
  latency: number;
  errors: number;
}
