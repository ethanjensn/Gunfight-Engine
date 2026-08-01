package com.gunfight.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class LatencyTest {

    @Test
    void websocketPingPongRoundTripLatencyIsLow() throws Exception {
        GameServer server = new GameServer(0);
        server.start();

        try {
            // Wait for server bind and LobbyManager startup
            int port;
            for (int i = 0; i < 50; i++) {
                port = server.getPort();
                if (port > 0) break;
                Thread.sleep(100);
            }

            int actualPort = server.getPort();
            assertTrue(actualPort > 0, "Server should have bound to a port");

            URI uri = new URI("ws://localhost:" + actualPort);
            Gson gson = new Gson();
            CountDownLatch openLatch = new CountDownLatch(1);
            CountDownLatch pongLatch = new CountDownLatch(1);
            AtomicLong latency = new AtomicLong(-1);

            WebSocketClient client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    openLatch.countDown();
                }

                @Override
                public void onMessage(String message) {
                    JsonObject data = gson.fromJson(message, JsonObject.class);
                    if ("pong".equals(data.get("type").getAsString())) {
                        long sent = data.get("t").getAsLong();
                        latency.set(System.currentTimeMillis() - sent);
                        pongLatch.countDown();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            client.connectBlocking(5, TimeUnit.SECONDS);
            assertTrue(openLatch.await(5, TimeUnit.SECONDS), "Client should connect");

            long pingTime = System.currentTimeMillis();
            JsonObject ping = new JsonObject();
            ping.addProperty("type", "ping");
            ping.addProperty("t", pingTime);
            client.send(gson.toJson(ping));

            assertTrue(pongLatch.await(5, TimeUnit.SECONDS), "Should receive pong");
            long rtt = latency.get();
            assertTrue(rtt >= 0, "RTT should be non-negative");
            assertTrue(rtt < 50, "Local RTT should be under 50 ms");

            System.out.println("WebSocket ping/pong RTT: " + rtt + " ms");

            client.closeBlocking();
        } finally {
            server.shutdown();
        }
    }
}
