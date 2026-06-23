package com.gunfight.lobby;

import com.gunfight.engine.Room;
import com.gunfight.net.GameServer;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomManager {
    private static final Logger log = LoggerFactory.getLogger(RoomManager.class);
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> playerToRoom = new ConcurrentHashMap<>();
    private final GameServer gameServer;
    private int nextRoomId = 1;

    // Players per game mode
    private static final Map<String, Integer> PLAYERS_PER_MODE = Map.of(
        "1v1", 2,
        "2v2", 4,
        "3v3", 6
    );

    public RoomManager(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    public Room createRoom(String gameMode, List<QueueEntry> players) {
        int requiredPlayers = PLAYERS_PER_MODE.getOrDefault(gameMode, 2);
        if (players.size() != requiredPlayers) {
            throw new IllegalArgumentException("Wrong player count for " + gameMode + 
                ": expected " + requiredPlayers + ", got " + players.size());
        }

        // Build WebSocket-to-username map for room creation
        Map<WebSocket, String> playerUsernames = new HashMap<>();
        for (QueueEntry entry : players) {
            playerUsernames.put(entry.connection, entry.username);
        }

        String roomId = "room-" + (nextRoomId++);
        Room room = Room.create(gameServer, gameMode, playerUsernames);
        rooms.put(roomId, room);

        // Track which room each player is in
        for (WebSocket conn : playerUsernames.keySet()) {
            playerToRoom.put(conn, roomId);
        }

        log.info("Created {} room {} with {} players", gameMode, roomId, players.size());
        return room;
    }

    public void removeRoom(String roomId) {
        Room room = rooms.remove(roomId);
        if (room != null) {
            room.shutdown();
            // Remove player mappings
            playerToRoom.values().removeIf(rid -> rid.equals(roomId));
            log.info("Removed room {}", roomId);
        }
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room getRoomByPlayer(WebSocket connection) {
        String roomId = playerToRoom.get(connection);
        if (roomId == null) return null;
        return rooms.get(roomId);
    }

    public void onPlayerDisconnect(WebSocket connection) {
        String roomId = playerToRoom.remove(connection);
        if (roomId != null) {
            Room room = rooms.get(roomId);
            if (room != null) {
                room.removePlayer(connection);
                if (room.isEmpty()) {
                    removeRoom(roomId);
                }
            }
        }
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public int getActiveRoomCount() {
        return rooms.size();
    }
}
