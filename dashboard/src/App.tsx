import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Shield, RefreshCw } from 'lucide-react';
import { MetricCards } from './components/MetricCards';
import { TrafficChart } from './components/TrafficChart';
import { BackendHealthTable } from './components/BackendHealthTable';
import { CircuitBreakerStatus } from './components/CircuitBreakerStatus';
import { RateLimitControl } from './components/RateLimitControl';
import { BackendInstance, CircuitBreakerStatus as CBStatus, GatewayMetrics, TrafficPoint } from './types';

export const App: React.FC = () => {
  const [metrics, setMetrics] = useState<GatewayMetrics>({
    requestsPerSec: 124.5,
    totalRequests: 84920,
    errorRate: 0.008,
    avgLatencyMs: 14.2,
    p95LatencyMs: 38.6,
    rateLimitedRequests: 412
  });

  const [instances, setInstances] = useState<BackendInstance[]>([
    {
      id: 'backend-1',
      host: 'localhost',
      port: 8081,
      baseUrl: 'http://localhost:8081',
      status: 'UP',
      lastHeartbeat: new Date().toISOString(),
      averageLatencyMs: 12.4,
      activeRequests: 2,
      errorRate: 0.002,
      consecutiveFailures: 0,
      consecutiveSuccesses: 15
    },
    {
      id: 'backend-2',
      host: 'localhost',
      port: 8082,
      baseUrl: 'http://localhost:8082',
      status: 'UP',
      lastHeartbeat: new Date().toISOString(),
      averageLatencyMs: 14.8,
      activeRequests: 4,
      errorRate: 0.005,
      consecutiveFailures: 0,
      consecutiveSuccesses: 22
    },
    {
      id: 'backend-3',
      host: 'localhost',
      port: 8083,
      baseUrl: 'http://localhost:8083',
      status: 'UP',
      lastHeartbeat: new Date().toISOString(),
      averageLatencyMs: 15.1,
      activeRequests: 3,
      errorRate: 0.004,
      consecutiveFailures: 0,
      consecutiveSuccesses: 19
    }
  ]);

  const [circuitBreakers, setCircuitBreakers] = useState<CBStatus[]>([
    {
      name: 'backendService',
      state: 'CLOSED',
      failureRate: 0.0,
      slowCallRate: 0.0,
      numberOfBufferedCalls: 10,
      numberOfFailedCalls: 0,
      numberOfSuccessfulCalls: 10,
      numberOfNotPermittedCalls: 0
    }
  ]);

  const [trafficData, setTrafficData] = useState<TrafficPoint[]>([
    { time: '12:00:00', requests: 110, latency: 15, errors: 0 },
    { time: '12:00:02', requests: 130, latency: 14, errors: 1 },
    { time: '12:00:04', requests: 125, latency: 16, errors: 0 },
    { time: '12:00:06', requests: 140, latency: 13, errors: 0 },
    { time: '12:00:08', requests: 115, latency: 18, errors: 2 },
    { time: '12:00:10', requests: 135, latency: 14, errors: 0 }
  ]);

  const fetchData = async () => {
    try {
      const backendsRes = await axios.get<BackendInstance[]>('/api/v1/gateway/backends');
      if (backendsRes.data && backendsRes.data.length > 0) {
        setInstances(backendsRes.data);
      }
    } catch (e) {
      // Fallback to local default simulated states if standalone
    }

    try {
      const cbRes = await axios.get<CBStatus[]>('/api/v1/gateway/circuit-breaker');
      if (cbRes.data && cbRes.data.length > 0) {
        setCircuitBreakers(cbRes.data);
      }
    } catch {
    }

    setMetrics(prev => ({
      ...prev,
      requestsPerSec: Number((110 + Math.random() * 30).toFixed(1)),
      totalRequests: prev.totalRequests + Math.floor(Math.random() * 20),
      avgLatencyMs: Number((12 + Math.random() * 5).toFixed(1))
    }));

    // Live traffic point update
    setTrafficData((prev) => {
      const now = new Date().toLocaleTimeString();
      const nextReqs = Math.floor(100 + Math.random() * 50);
      const nextLatency = Math.floor(12 + Math.random() * 10);
      const nextPoint = { time: now, requests: nextReqs, latency: nextLatency, errors: Math.random() > 0.8 ? 1 : 0 };
      const slice = prev.length >= 12 ? prev.slice(1) : prev;
      return [...slice, nextPoint];
    });
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 2000);
    return () => clearInterval(interval);
  }, []);

  const handleToggleInstance = async (inst: BackendInstance) => {
    const nextStatus = inst.status === 'UP' ? 'DOWN' : 'UP';
    try {
      await axios.post(`${inst.baseUrl}/health/toggle`, { status: nextStatus });
      fetchData();
    } catch {
      // If direct call fails or mock, update local state
      setInstances(prev => prev.map(i => i.id === inst.id ? { ...i, status: nextStatus } : i));
    }
  };

  const handleTransitionCircuitBreaker = async (name: string, targetState: string) => {
    try {
      await axios.post(`/api/v1/gateway/circuit-breaker/${name}/transition/${targetState}`);
      fetchData();
    } catch {
      setCircuitBreakers(prev => prev.map(cb => cb.name === name ? { ...cb, state: targetState as any } : cb));
    }
  };

  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="dashboard-header">
        <div className="brand-section">
          <div className="brand-logo">
            <Shield size={22} color="#ffffff" />
          </div>
          <div>
            <h1 className="brand-title">SentinelFlow</h1>
            <p className="brand-subtitle">Distributed API Gateway & Traffic Observatory</p>
          </div>
        </div>

        <div className="header-actions">
          <span className="badge badge-up">
            <span className="pulse-dot" />
            Gateway Cluster: Operational
          </span>
          <button className="btn" onClick={fetchData}>
            <RefreshCw size={14} />
            Refresh
          </button>
        </div>
      </header>

      {/* Metric Counters */}
      <MetricCards metrics={metrics} />

      {/* Traffic Real-time Chart */}
      <TrafficChart data={trafficData} />

      {/* Health Table & Circuit Breakers */}
      <div className="sections-grid" style={{ marginTop: '20px' }}>
        <BackendHealthTable
          instances={instances}
          onToggleInstance={handleToggleInstance}
        />
        <CircuitBreakerStatus
          circuitBreakers={circuitBreakers}
          onTransitionState={handleTransitionCircuitBreaker}
        />
      </div>

      {/* Interactive Rate Limiting Simulator */}
      <RateLimitControl />
    </div>
  );
};
