package com.gunfight.lobby;

import org.java_websocket.WebSocket;
import com.gunfight.engine.Room;

public class LobbyPlayer {
    public final WebSocket connection;
    public String username;
    public PlayerState state;
    public String currentQueueMode; // null if not queued
    public Room currentRoom;        // null if not in game
    public long queueJoinTime;      // timestamp for wait time calc

    public LobbyPlayer(WebSocket connection) {
        this.connection = connection;
        this.username = null;
        this.state = PlayerState.MENU;
        this.currentQueueMode = null;
        this.currentRoom = null;
        this.queueJoinTime = 0;
    }
}
