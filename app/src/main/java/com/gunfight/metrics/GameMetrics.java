package com.gunfight.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lightweight, lock-free metrics collector for the game loop.
 *
 * <p>Tracks per-tick timing and network broadcast timing. The counters are
 * updated with atomic operations so they can be safely sampled from a
 * background thread without affecting the tick loop.
 */
public class GameMetrics {
    private final LongAdder tickCount = new LongAdder();
    private final LongAdder totalTickNs = new LongAdder();
    private final LongAdder totalBroadcastNs = new LongAdder();
    private final AtomicLong maxTickNs = new AtomicLong();
    private final AtomicLong maxBroadcastNs = new AtomicLong();

    public void recordTickNs(long ns) {
        tickCount.increment();
        totalTickNs.add(ns);
        updateMax(maxTickNs, ns);
    }

    public void recordBroadcastNs(long ns) {
        totalBroadcastNs.add(ns);
        updateMax(maxBroadcastNs, ns);
    }

    public long getTickCount() {
        return tickCount.sum();
    }

    public double getAverageTickMs() {
        long count = tickCount.sum();
        return count == 0 ? 0.0 : (totalTickNs.sum() / (double) count) / 1_000_000.0;
    }

    public double getAverageBroadcastMs() {
        long count = tickCount.sum();
        return count == 0 ? 0.0 : (totalBroadcastNs.sum() / (double) count) / 1_000_000.0;
    }

    public double getMaxTickMs() {
        return maxTickNs.get() / 1_000_000.0;
    }

    public double getMaxBroadcastMs() {
        return maxBroadcastNs.get() / 1_000_000.0;
    }

    public void reset() {
        tickCount.reset();
        totalTickNs.reset();
        totalBroadcastNs.reset();
        maxTickNs.set(0);
        maxBroadcastNs.set(0);
    }

    private void updateMax(AtomicLong maxHolder, long value) {
        long current;
        while ((current = maxHolder.get()) < value) {
            if (maxHolder.compareAndSet(current, value)) {
                break;
            }
        }
    }
}
