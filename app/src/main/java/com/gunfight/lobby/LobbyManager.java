package com.gunfight.lobby;

import com.gunfight.engine.Room;
import com.gunfight.net.GameServer;
import com.gunfight.net.InputPacket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager {
    private static final Logger log = LoggerFactory.getLogger(LobbyManager.class);
    private final GameServer gameServer;
    private final QueueManager queueManager;
    private final RoomManager roomManager;
    private final Matchmaker matchmaker;
    private final Map<WebSocket, LobbyPlayer> players = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public LobbyManager(GameServer gameServer) {
        this.gameServer = gameServer;
        this.queueManager = new QueueManager();
        this.roomManager = new RoomManager(gameServer);
        this.matchmaker = new Matchmaker(queueManager, roomManager, this);
    }

    public void start() {
        matchmaker.start();
    }

    public void stop() {
        matchmaker.stop();
    }

    // Called when new WebSocket connects
    public void onConnect(WebSocket connection) {
        LobbyPlayer player = new LobbyPlayer(connection);
        players.put(connection, player);
        log.info("Player connected: {}", connection.getRemoteSocketAddress());
        
        // Send initial state - client should show main menu
        sendState(connection, "MENU");
    }

    // Called when WebSocket disconnects
    public void onDisconnect(WebSocket connection) {
        LobbyPlayer player = players.remove(connection);
        if (player != null) {
            // Clean up from queue if queued
            if (player.state == PlayerState.QUEUED) {
                queueManager.onDisconnect(connection);
            }
            // Clean up from room if in game
            if (player.state == PlayerState.IN_GAME && player.currentRoom != null) {
                player.currentRoom.removePlayer(connection);
                roomManager.onPlayerDisconnect(connection);
            }
            log.info("Player disconnected: {}", player.username);
        }
    }

    // Route incoming WebSocket messages
    public void onMessage(WebSocket connection, String message) {
        LobbyPlayer player = players.get(connection);
        if (player == null) return;

        try {
            JsonObject packet = gson.fromJson(message, JsonObject.class);
            String type = packet.has("type") ? packet.get("type").getAsString() : "";

            switch (type) {
                case "setUsername":
                    handleSetUsername(player, packet);
                    break;
                case "joinQueue":
                    handleJoinQueue(player, packet);
                    break;
                case "cancelQueue":
                    handleCancelQueue(player);
                    break;
                case "gameInput":
                    // Forward to room if in game
                    handleGameInput(player, packet);
                    break;
                case "ping":
                    handlePing(player, packet);
                    break;
                default:
                    // Unknown message type
                    break;
            }
        } catch (Exception e) {
            log.warn("Failed to parse message from {}", connection.getRemoteSocketAddress(), e);
        }
    }

    private void handlePing(LobbyPlayer player, JsonObject packet) {
        long clientTime = packet.has("t") ? packet.get("t").getAsLong() : 0L;
        JsonObject pong = new JsonObject();
        pong.addProperty("type", "pong");
        pong.addProperty("t", clientTime);
        player.connection.send(gson.toJson(pong));
    }

    private void handleSetUsername(LobbyPlayer player, JsonObject packet) {
        String username = packet.has("username") ? packet.get("username").getAsString() : null;
        if (username != null && !username.trim().isEmpty()) {
            player.username = username.trim();
            log.info("Username set: {}", player.username);
        }
    }

    private void handleJoinQueue(LobbyPlayer player, JsonObject packet) {
        // State machine guard - only MENU state can queue (prevents Double Queue)
        if (player.state != PlayerState.MENU) {
            sendError(player.connection, "Already in queue or game");
            return;
        }

        String gameMode = packet.has("gameMode") ? packet.get("gameMode").getAsString() : "1v1";
        if (!gameMode.matches("1v1|2v2|3v3")) {
            sendError(player.connection, "Invalid game mode");
            return;
        }

        if (player.username == null || player.username.isEmpty()) {
            sendError(player.connection, "Set username first");
            return;
        }

        boolean joined = queueManager.joinQueue(player.connection, gameMode, player.username);
        if (joined) {
            player.state = PlayerState.QUEUED;
            player.currentQueueMode = gameMode;
            player.queueJoinTime = System.currentTimeMillis();
            
            // Send initial queue status
            sendQueueStatus(player);
            log.info("{} joined {} queue", player.username, gameMode);
        } else {
            sendError(player.connection, "Failed to join queue");
        }
    }

    private void handleCancelQueue(LobbyPlayer player) {
        if (player.state != PlayerState.QUEUED) {
            return; // Not in queue, ignore
        }

        queueManager.cancelQueue(player.connection);
        player.state = PlayerState.MENU;
        player.currentQueueMode = null;
        player.queueJoinTime = 0;
        
        sendState(player.connection, "MENU");
        log.info("{} cancelled queue", player.username);
    }

    private void handleGameInput(LobbyPlayer player, JsonObject packet) {
        if (player.state != PlayerState.IN_GAME || player.currentRoom == null) {
            return;
        }
        
        Room room = player.currentRoom;
        Integer entityId = room.getEntityId(player.connection);
        if (entityId == null) return;
        
        // Convert JsonObject to InputPacket and add to queue for InputSystem
        try {
            InputPacket inputPacket = gson.fromJson(packet, InputPacket.class);
            inputPacket.setEntityId(entityId);
            gameServer.addGameInput(inputPacket);
        } catch (Exception e) {
            log.warn("Failed to parse game input from {}", player.connection.getRemoteSocketAddress(), e);
        }
    }

    // Called by Matchmaker when match is found
    public void onMatchFound(String gameMode, java.util.List<QueueEntry> matchedPlayers, Room room) {
        for (QueueEntry entry : matchedPlayers) {
            LobbyPlayer player = players.get(entry.connection);
            if (player != null) {
                player.state = PlayerState.IN_GAME;
                player.currentRoom = room;
                player.currentQueueMode = null;
                player.queueJoinTime = 0;
                
                // Notify client to switch to game view
                JsonObject msg = new JsonObject();
                msg.addProperty("type", "gameStart");
                msg.addProperty("gameMode", gameMode);
                msg.addProperty("roomId", room.toString());
                player.connection.send(gson.toJson(msg));
            }
        }
    }

    // Broadcast queue status to all queued players periodically
    public void broadcastQueueStatus() {
        for (LobbyPlayer player : players.values()) {
            if (player.state == PlayerState.QUEUED) {
                sendQueueStatus(player);
            }
        }
    }

    private void sendQueueStatus(LobbyPlayer player) {
        int position = queueManager.getPosition(player.connection);
        int estimatedSeconds = queueManager.estimateWaitSeconds(player.currentQueueMode);
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "queueStatus");
        msg.addProperty("position", position);
        msg.addProperty("estimatedSeconds", estimatedSeconds);
        msg.addProperty("gameMode", player.currentQueueMode);
        msg.addProperty("queueSize", queueManager.getQueueSize(player.currentQueueMode));
        player.connection.send(gson.toJson(msg));
    }

    private void sendState(WebSocket connection, String state) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "state");
        msg.addProperty("state", state);
        connection.send(gson.toJson(msg));
    }

    private void sendError(WebSocket connection, String error) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "error");
        msg.addProperty("message", error);
        connection.send(gson.toJson(msg));
    }

    // Getters for internal use
    QueueManager getQueueManager() { return queueManager; }
    RoomManager getRoomManager() { return roomManager; }
    LobbyPlayer getPlayer(WebSocket connection) { return players.get(connection); }
}
