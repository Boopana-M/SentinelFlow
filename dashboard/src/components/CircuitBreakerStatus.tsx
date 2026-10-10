import React from 'react';
import { CircuitBreakerStatus as CBStatus } from '../types';
import { ShieldCheck, ShieldOff, AlertCircle } from 'lucide-react';

interface Props {
  circuitBreakers: CBStatus[];
  onTransitionState: (name: string, targetState: string) => void;
}

export const CircuitBreakerStatus: React.FC<Props> = ({ circuitBreakers, onTransitionState }) => {
  return (
    <div className="panel-card">
      <div className="panel-title">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ShieldCheck size={18} color="#10b981" />
          <span>Resilience4j Circuit Breakers</span>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {circuitBreakers.map((cb) => {
          const badgeClass =
            cb.state === 'CLOSED' ? 'badge-closed' :
            cb.state === 'OPEN' ? 'badge-open' : 'badge-half-open';

          return (
            <div
              key={cb.name}
              style={{
                backgroundColor: '#0e1424',
                padding: '14px',
                borderRadius: '8px',
                border: '1px solid #1e293b'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                <span style={{ fontWeight: 600 }}>{cb.name}</span>
                <span className={`badge ${badgeClass}`}>
                  <span className="pulse-dot" />
                  {cb.state}
                </span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '12px', color: '#94a3b8', marginBottom: '12px' }}>
                <div>Failure Rate: <strong style={{ color: '#fff' }}>{cb.failureRate.toFixed(1)}%</strong></div>
                <div>Slow Calls: <strong style={{ color: '#fff' }}>{cb.slowCallRate.toFixed(1)}%</strong></div>
                <div>Calls Buffered: <strong style={{ color: '#fff' }}>{cb.numberOfBufferedCalls}</strong></div>
                <div>Calls Blocked: <strong style={{ color: '#fff' }}>{cb.numberOfNotPermittedCalls}</strong></div>
              </div>

              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  className="btn"
                  style={{ flex: 1, padding: '5px 8px', fontSize: '11px', justifyContent: 'center' }}
                  onClick={() => onTransitionState(cb.name, 'CLOSED')}
                >
                  <ShieldCheck size={12} color="#10b981" />
                  Force CLOSED
                </button>
                <button
                  className="btn"
                  style={{ flex: 1, padding: '5px 8px', fontSize: '11px', justifyContent: 'center' }}
                  onClick={() => onTransitionState(cb.name, 'OPEN')}
                >
                  <ShieldOff size={12} color="#ef4444" />
                  Trip OPEN
                </button>
                <button
                  className="btn"
                  style={{ flex: 1, padding: '5px 8px', fontSize: '11px', justifyContent: 'center' }}
                  onClick={() => onTransitionState(cb.name, 'HALF_OPEN')}
                >
                  <AlertCircle size={12} color="#f59e0b" />
                  HALF-OPEN
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
