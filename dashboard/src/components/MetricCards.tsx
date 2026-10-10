import React from 'react';
import { GatewayMetrics } from '../types';
import { Activity, Zap, AlertTriangle, Clock, ShieldAlert, BarChart3 } from 'lucide-react';

interface Props {
  metrics: GatewayMetrics;
}

export const MetricCards: React.FC<Props> = ({ metrics }) => {
  return (
    <div className="metrics-grid">
      <div className="metric-card">
        <div className="metric-header">
          <span>Throughput</span>
          <Activity size={16} color="#6366f1" />
        </div>
        <div className="metric-value">
          {metrics.requestsPerSec.toFixed(1)}
          <span className="metric-unit">req/s</span>
        </div>
      </div>

      <div className="metric-card">
        <div className="metric-header">
          <span>Total Requests</span>
          <BarChart3 size={16} color="#06b6d4" />
        </div>
        <div className="metric-value">
          {metrics.totalRequests.toLocaleString()}
        </div>
      </div>

      <div className="metric-card">
        <div className="metric-header">
          <span>Error Rate</span>
          <AlertTriangle size={16} color={metrics.errorRate > 0.05 ? '#ef4444' : '#10b981'} />
        </div>
        <div className="metric-value">
          {(metrics.errorRate * 100).toFixed(2)}
          <span className="metric-unit">%</span>
        </div>
      </div>

      <div className="metric-card">
        <div className="metric-header">
          <span>Avg Latency</span>
          <Clock size={16} color="#f59e0b" />
        </div>
        <div className="metric-value">
          {metrics.avgLatencyMs.toFixed(1)}
          <span className="metric-unit">ms</span>
        </div>
      </div>

      <div className="metric-card">
        <div className="metric-header">
          <span>P95 Latency</span>
          <Zap size={16} color="#ec4899" />
        </div>
        <div className="metric-value">
          {metrics.p95LatencyMs.toFixed(1)}
          <span className="metric-unit">ms</span>
        </div>
      </div>

      <div className="metric-card">
        <div className="metric-header">
          <span>Rate-Limited (429)</span>
          <ShieldAlert size={16} color="#8b5cf6" />
        </div>
        <div className="metric-value">
          {metrics.rateLimitedRequests.toLocaleString()}
        </div>
      </div>
    </div>
  );
};
