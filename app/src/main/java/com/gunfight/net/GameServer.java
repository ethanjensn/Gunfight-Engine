package com.gunfight.net;
 
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.java_websocket.WebSocket;  // Fixed import
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.gunfight.data.HealthComponent;
import com.gunfight.data.InputComponent;
import com.gunfight.data.PositionComponent;
import com.gunfight.engine.GameWorld;
import com.google.gson.Gson; // Make sure you import this at the top

public class GameServer extends WebSocketServer {
    private GameWorld world;
    // Mailbox for incoming requests
    private Queue<InputPacket> inputQueue = new ConcurrentLinkedQueue<>();

    // Map connections to player IDs
    private Map<WebSocket, Integer> connectionToEntity = new ConcurrentHashMap<>();
    
    public GameServer(int port, GameWorld world) {
        // super() simply says: "Run the parent's setup code first."
        super(new InetSocketAddress(port));
        this.world = world;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress());

        // create a new player entity when someone connects
        int newEntityId = world.createEntity();

        // add starting components
        world.addComponent(HealthComponent.class, newEntityId, new HealthComponent(100));
        world.addComponent(InputComponent.class, newEntityId, new InputComponent());
        world.addComponent(PositionComponent.class, newEntityId, new PositionComponent(0, 0));

        // map the connection to the entity ID
        connectionToEntity.put(conn, newEntityId);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Integer entityId = connectionToEntity.remove(conn);

        if (entityId != null) {
            world.destroyEntity(entityId);
            System.out.println("Player " + entityId + " disconnected");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Integer playerId = connectionToEntity.get(conn);

        if (playerId != null) {
            try {
                // 1. Turn the JSON text into a Java Object
                InputPacket packet = new Gson().fromJson(message, InputPacket.class);
                
                // 2. Set the entity ID
                packet.setEntityId(playerId);

                // 3. Add the packet to the mailbox
                inputQueue.add(packet);

            } catch (Exception e) {
                System.out.println("Failed to parse JSON from player " + playerId);
            }

            System.out.println("Received message from player " + playerId + ": " + message);
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
        for (WebSocket conn : connectionToEntity.keySet()) {
            conn.send(message);
        }
    }
}
