import React, { useState } from 'react';
import axios from 'axios';
import { Gauge, Play, Check, AlertCircle } from 'lucide-react';

export const RateLimitControl: React.FC = () => {
  const [selectedTier, setSelectedTier] = useState<'FREE' | 'PREMIUM' | 'ADMIN'>('FREE');
  const [lastResult, setLastResult] = useState<string | null>(null);
  const [statusColor, setStatusColor] = useState<string>('#10b981');
  const [loading, setLoading] = useState<boolean>(false);

  const sendTestRequest = async () => {
    setLoading(true);
    try {
      const response = await axios.get('/api/v1/proxy/resource', {
        headers: {
          'X-User-Role': selectedTier,
          'X-User-Id': `demo-${selectedTier.toLowerCase()}`
        }
      });
      const limit = response.headers['x-ratelimit-limit'] || '100';
      const remaining = response.headers['x-ratelimit-remaining'] || '99';
      const backend = response.headers['x-backend-instance'] || 'backend-1';
      setLastResult(`HTTP 200 OK — Allowed (Remaining: ${remaining}/${limit}, Backend: ${backend})`);
      setStatusColor('#10b981');
    } catch (err: any) {
      if (err.response && err.response.status === 429) {
        const retryAfter = err.response.headers['retry-after'] || 'N/A';
        setLastResult(`HTTP 429 Too Many Requests — Blocked (Retry-After: ${retryAfter}s)`);
        setStatusColor('#ef4444');
      } else {
        setLastResult(`Error: ${err.message}`);
        setStatusColor('#f59e0b');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="panel-card" style={{ marginTop: '20px' }}>
      <div className="panel-title">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Gauge size={18} color="#8b5cf6" />
          <span>Interactive Rate Limit & Routing Simulator</span>
        </div>
      </div>

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'center' }}>
        <div style={{ display: 'flex', gap: '6px' }}>
          {(['FREE', 'PREMIUM', 'ADMIN'] as const).map((tier) => (
            <button
              key={tier}
              className={`btn ${selectedTier === tier ? 'btn-primary' : ''}`}
              onClick={() => setSelectedTier(tier)}
            >
              {tier} {tier === 'FREE' ? '(100/m)' : tier === 'PREMIUM' ? '(1k/m)' : '(5k/m)'}
            </button>
          ))}
        </div>

        <button
          className="btn btn-primary"
          onClick={sendTestRequest}
          disabled={loading}
          style={{ marginLeft: 'auto' }}
        >
          {loading ? <Play size={14} className="spin" /> : <Play size={14} />}
          Send Test Traffic
        </button>
      </div>

      {lastResult && (
        <div
          style={{
            marginTop: '14px',
            padding: '10px 14px',
            borderRadius: '6px',
            backgroundColor: '#0d1322',
            border: `1px solid ${statusColor}`,
            fontSize: '12px',
            fontFamily: 'JetBrains Mono',
            color: statusColor,
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}
        >
          {statusColor === '#10b981' ? <Check size={14} /> : <AlertCircle size={14} />}
          {lastResult}
        </div>
      )}
    </div>
  );
};
