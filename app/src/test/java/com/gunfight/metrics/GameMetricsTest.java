package com.gunfight.metrics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameMetricsTest {

    @Test
    void recordsTickMetrics() {
        GameMetrics metrics = new GameMetrics();
        metrics.recordTickNs(1_000_000L);
        metrics.recordTickNs(2_000_000L);

        assertEquals(2, metrics.getTickCount());
        assertEquals(1.5, metrics.getAverageTickMs(), 0.001);
        assertEquals(2.0, metrics.getMaxTickMs(), 0.001);
    }

    @Test
    void recordsBroadcastMetrics() {
        GameMetrics metrics = new GameMetrics();
        metrics.recordTickNs(1_000_000L);
        metrics.recordBroadcastNs(500_000L);
        metrics.recordTickNs(1_000_000L);
        metrics.recordBroadcastNs(1_500_000L);

        assertEquals(1.0, metrics.getAverageBroadcastMs(), 0.001);
        assertEquals(1.5, metrics.getMaxBroadcastMs(), 0.001);
    }

    @Test
    void resetClearsAllStats() {
        GameMetrics metrics = new GameMetrics();
        metrics.recordTickNs(1_000_000L);
        metrics.recordBroadcastNs(500_000L);
        metrics.reset();

        assertEquals(0, metrics.getTickCount());
        assertEquals(0.0, metrics.getAverageTickMs(), 0.001);
        assertEquals(0.0, metrics.getMaxTickMs(), 0.001);
    }
}
