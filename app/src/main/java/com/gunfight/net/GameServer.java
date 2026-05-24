package com.gunfight.net;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.gunfight.engine.Room;
import com.google.gson.Gson;

public class GameServer extends WebSocketServer {
    // Mailbox for incoming requests — shared across all rooms for simplicity
    private Queue<InputPacket> inputQueue = new ConcurrentLinkedQueue<>();

    // All active rooms
    private final List<Room> rooms = new CopyOnWriteArrayList<>();

    // Map each connection to its room for fast lookup
    private final Map<WebSocket, Room> connectionToRoom = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    public GameServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress());

        // Find an open room or create a new one
        Room room = findOrCreateRoom();
        room.addPlayer(conn);
        connectionToRoom.put(conn, room);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Room room = connectionToRoom.remove(conn);
        if (room != null) {
            room.removePlayer(conn);
            if (room.isEmpty()) {
                rooms.remove(room);
                System.out.println("Empty room removed");
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Room room = connectionToRoom.get(conn);
        if (room == null) return;

        Integer playerId = room.getEntityId(conn);
        if (playerId == null) return;

        try {
            InputPacket packet = gson.fromJson(message, InputPacket.class);
            packet.setEntityId(playerId);
            inputQueue.add(packet);
        } catch (Exception e) {
            System.out.println("Failed to parse JSON from player " + playerId);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Game server started on port " + getPort());
    }

    public Queue<InputPacket> getInputQueue() {
        return inputQueue;
    }

    public void broadcast(String message) {
        for (WebSocket conn : connectionToRoom.keySet()) {
            conn.send(message);
        }
    }

    private Room findOrCreateRoom() {
        for (Room room : rooms) {
            if (!room.isFull()) return room;
        }
        Room newRoom = Room.create(this);
        rooms.add(newRoom);
        System.out.println("New room created — total rooms: " + rooms.size());
        return newRoom;
    }
}
