package com.gunfight.engine;

import com.gunfight.metrics.GameMetrics;
import com.gunfight.net.GameServer;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomMetricsTest {

    @Test
    void roomRunsAtSixtyTicksPerSecondAndCollectsMetrics() throws InterruptedException {
        GameServer server = new GameServer(18080);

        Map<WebSocket, String> players = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            WebSocket conn = mock(WebSocket.class);
            when(conn.isOpen()).thenReturn(true);
            players.put(conn, "player" + i);
        }

        Room room = Room.create(server, "3v3", players);

        // Warm up for 3 seconds, then reset metrics for a clean steady-state window
        Thread.sleep(3000);
        room.getMetrics().reset();

        // Collect steady-state metrics over the next 5 seconds at 60 TPS
        Thread.sleep(5000);

        room.shutdown();

        GameMetrics metrics = room.getMetrics();
        assertTrue(metrics.getTickCount() >= 250, "Expected at least 250 ticks in 5 seconds");
        assertTrue(metrics.getAverageTickMs() < 16.667, "Average tick should fit inside 60 TPS budget");
        assertTrue(metrics.getPacketCount() > 0, "Should have broadcast packets");

        System.out.println("Room metrics — ticks: " + metrics.getTickCount()
            + ", avg tick: " + String.format("%.3f", metrics.getAverageTickMs()) + "ms"
            + ", max tick: " + String.format("%.3f", metrics.getMaxTickMs()) + "ms"
            + ", avg broadcast: " + String.format("%.3f", metrics.getAverageBroadcastMs()) + "ms"
            + ", max broadcast: " + String.format("%.3f", metrics.getMaxBroadcastMs()) + "ms"
            + ", avg serialize: " + String.format("%.3f", metrics.getAverageSerializationMs()) + "ms"
            + ", packets: " + metrics.getPacketCount()
            + ", bytes: " + metrics.getByteCount());
    }
}
