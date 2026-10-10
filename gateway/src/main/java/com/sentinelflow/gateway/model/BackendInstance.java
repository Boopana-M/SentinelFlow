package com.sentinelflow.gateway.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BackendInstance {

    private final String id;
    private final String host;
    private final int port;
    private volatile InstanceStatus status;
    private volatile Instant lastHeartbeat;
    private volatile double averageLatencyMs;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);

    public BackendInstance(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.status = InstanceStatus.UNKNOWN;
        this.lastHeartbeat = Instant.now();
        this.averageLatencyMs = 0.0;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }

    public InstanceStatus getStatus() {
        return status;
    }

    public void setStatus(InstanceStatus status) {
        this.status = status;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public synchronized void updateLatency(long latencyMs) {
        if (this.averageLatencyMs <= 0.0) {
            this.averageLatencyMs = latencyMs;
        } else {
            // Exponential moving average (alpha = 0.2)
            this.averageLatencyMs = (0.2 * latencyMs) + (0.8 * this.averageLatencyMs);
        }
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    public int incrementConsecutiveFailures() {
        consecutiveSuccesses.set(0);
        return consecutiveFailures.incrementAndGet();
    }

    public int getConsecutiveSuccesses() {
        return consecutiveSuccesses.get();
    }

    public int incrementConsecutiveSuccesses() {
        consecutiveFailures.set(0);
        return consecutiveSuccesses.incrementAndGet();
    }

    public void resetFailureCounters() {
        consecutiveFailures.set(0);
    }

    public int getActiveRequests() {
        return activeRequests.get();
    }

    public int incrementActiveRequests() {
        return activeRequests.incrementAndGet();
    }

    public int decrementActiveRequests() {
        return activeRequests.updateAndGet(curr -> Math.max(0, curr - 1));
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getFailedRequests() {
        return failedRequests.get();
    }

    public double getErrorRate() {
        long total = totalRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) failedRequests.get() / total;
    }

    public void recordRequest(boolean success) {
        totalRequests.incrementAndGet();
        if (!success) {
            failedRequests.incrementAndGet();
        }
    }
}
