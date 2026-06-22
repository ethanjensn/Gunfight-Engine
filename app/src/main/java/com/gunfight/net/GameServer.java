package com.gunfight.net;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.gunfight.lobby.LobbyManager;
import com.google.gson.Gson;

public class GameServer extends WebSocketServer {
    // Mailbox for incoming game inputs - passed to ECS rooms
    private Queue<InputPacket> inputQueue = new ConcurrentLinkedQueue<>();

    // Lobby manager handles matchmaking and room lifecycle
    private LobbyManager lobbyManager;

    private final Gson gson = new Gson();

    public GameServer(int port) {
        super(new InetSocketAddress("0.0.0.0", port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
        lobbyManager.onConnect(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        lobbyManager.onDisconnect(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Route all messages through lobby manager
        lobbyManager.onMessage(conn, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Game server started on port " + getPort());
        // Initialize and start lobby manager
        this.lobbyManager = new LobbyManager(this);
        lobbyManager.start();
    }
    
    public void shutdown() {
        if (lobbyManager != null) {
            lobbyManager.stop();
        }
    }

    public Queue<InputPacket> getInputQueue() {
        return inputQueue;
    }

    public void broadcast(String message) {
        // Broadcast to all connected clients via lobby manager
        if (lobbyManager != null) {
            // This will be handled by the appropriate system
        }
    }
    
    // For direct game input from players in rooms
    public void addGameInput(InputPacket packet) {
        inputQueue.add(packet);
    }
}
