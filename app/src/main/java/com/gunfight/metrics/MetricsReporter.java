package com.gunfight.metrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically samples and logs {@link GameMetrics}.
 *
 * <p>Intended to run alongside a {@link com.gunfight.engine.GameLoop} and report
 * average/max tick and broadcast times every reporting interval.
 */
public class MetricsReporter {
    private static final Logger log = LoggerFactory.getLogger(MetricsReporter.class);
    private static final long DEFAULT_INTERVAL_SECONDS = 60;

    private final GameMetrics metrics;
    private final ScheduledExecutorService executor;
    private final long intervalSeconds;

    public MetricsReporter(GameMetrics metrics) {
        this(metrics, DEFAULT_INTERVAL_SECONDS);
    }

    public MetricsReporter(GameMetrics metrics, long intervalSeconds) {
        this.metrics = metrics;
        this.intervalSeconds = intervalSeconds;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MetricsReporter");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(this::report, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdown();
    }

    private void report() {
        long tickCount = metrics.getTickCount();
        if (tickCount == 0) {
            return;
        }
        long windowSeconds = intervalSeconds == 0 ? 60 : intervalSeconds;
        double packetsPerSecond = metrics.getPacketCount() / (double) windowSeconds;
        double bytesPerSecond = metrics.getByteCount() / (double) windowSeconds;
        log.info("Game metrics — ticks: {}, avg tick: {}ms, max tick: {}ms, avg broadcast: {}ms, max broadcast: {}ms, avg serialize: {}ms, max serialize: {}ms, packets/s: {:.1f}, bytes/s: {:.0f}, avg packet: {:.0f}B",
            tickCount,
            String.format("%.3f", metrics.getAverageTickMs()),
            String.format("%.3f", metrics.getMaxTickMs()),
            String.format("%.3f", metrics.getAverageBroadcastMs()),
            String.format("%.3f", metrics.getMaxBroadcastMs()),
            String.format("%.3f", metrics.getAverageSerializationMs()),
            String.format("%.3f", metrics.getMaxSerializationMs()),
            packetsPerSecond,
            bytesPerSecond,
            metrics.getAveragePacketBytes()
        );
        metrics.reset();
    }
}
