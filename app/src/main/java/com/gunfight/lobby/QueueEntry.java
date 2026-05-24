package com.gunfight.lobby;

import org.java_websocket.WebSocket;

public class QueueEntry {
    public final WebSocket connection;
    public final String username;
    public final String gameMode;
    public final long joinTime;

    public QueueEntry(WebSocket connection, String username, String gameMode) {
        this.connection = connection;
        this.username = username;
        this.gameMode = gameMode;
        this.joinTime = System.currentTimeMillis();
    }
}
