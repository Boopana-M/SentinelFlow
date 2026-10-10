import React from 'react';
import { BackendInstance } from '../types';
import { Server, CheckCircle2, AlertOctagon } from 'lucide-react';

interface Props {
  instances: BackendInstance[];
  onToggleInstance: (instance: BackendInstance) => void;
}

export const BackendHealthTable: React.FC<Props> = ({ instances, onToggleInstance }) => {
  return (
    <div className="panel-card">
      <div className="panel-title">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Server size={18} color="#6366f1" />
          <span>Backend Microservices Health & Load</span>
        </div>
        <span style={{ fontSize: '12px', color: '#64748b' }}>
          {instances.filter(i => i.status === 'UP').length}/{instances.length} Healthy
        </span>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Instance ID</th>
              <th>Endpoint</th>
              <th>Status</th>
              <th>Active Conns</th>
              <th>Avg Latency</th>
              <th>Error Rate</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {instances.map((inst) => {
              const isUp = inst.status === 'UP';
              return (
                <tr key={inst.id}>
                  <td style={{ fontWeight: 600 }}>{inst.id}</td>
                  <td style={{ color: '#94a3b8', fontFamily: 'JetBrains Mono' }}>
                    {inst.host}:{inst.port}
                  </td>
                  <td>
                    <span className={`badge ${isUp ? 'badge-up' : 'badge-down'}`}>
                      <span className="pulse-dot" />
                      {inst.status}
                    </span>
                  </td>
                  <td>{inst.activeRequests}</td>
                  <td>{inst.averageLatencyMs.toFixed(1)} ms</td>
                  <td>{(inst.errorRate * 100).toFixed(1)}%</td>
                  <td>
                    <button
                      className="btn"
                      style={{ padding: '4px 10px', fontSize: '11px' }}
                      onClick={() => onToggleInstance(inst)}
                      title="Simulate service failure or recovery"
                    >
                      {isUp ? (
                        <>
                          <AlertOctagon size={12} color="#ef4444" />
                          Simulate DOWN
                        </>
                      ) : (
                        <>
                          <CheckCircle2 size={12} color="#10b981" />
                          Simulate UP
                        </>
                      )}
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
