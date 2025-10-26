package com.alphapay.payEngine.integration.dto;

import java.util.Deque;
import java.util.LinkedList;

public class TimeWindowMetrics {
    private final Deque<Long> successTimestamps = new LinkedList<>();
    private final Deque<Long> failureTimestamps = new LinkedList<>();
    private final long windowSizeMillis;

    public TimeWindowMetrics(long windowSizeMillis) {
        this.windowSizeMillis = windowSizeMillis;
    }

    public synchronized void recordSuccess() {
        long now = System.currentTimeMillis();
        successTimestamps.addLast(now);
        evictOld(successTimestamps, now);
    }

    public synchronized void recordFailure() {
        long now = System.currentTimeMillis();
        failureTimestamps.addLast(now);
        evictOld(failureTimestamps, now);
    }

    public synchronized double getSuccessRate() {
        long now = System.currentTimeMillis();
        evictOld(successTimestamps, now);
        evictOld(failureTimestamps, now);

        long successCount = successTimestamps.size();
        long failureCount = failureTimestamps.size();
        long total = successCount + failureCount;

        return total == 0 ? 1.0 : ((double) successCount / total);
    }

    private void evictOld(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > windowSizeMillis) {
            timestamps.removeFirst();
        }
    }
}

