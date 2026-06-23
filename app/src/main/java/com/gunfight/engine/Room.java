package com.gunfight.engine;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.gunfight.data.HealthComponent;
import com.gunfight.data.VisionComponent;
import com.gunfight.data.InputComponent;
import com.gunfight.data.PlayerSlotComponent;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.RoundStateComponent;
import com.gunfight.data.ScoreComponent;
import com.gunfight.data.SpawnPointComponent;
import com.gunfight.data.StaticMapComponent;
import com.gunfight.data.UsernameComponent;
import com.gunfight.data.WeaponComponent;
import com.gunfight.net.GameServer;
import com.gunfight.net.InputPacket;

public class Room {
    private static final Logger log = LoggerFactory.getLogger(Room.class);
    private static final Map<String, Integer> PLAYERS_PER_MODE = Map.of(
        "1v1", 2,
        "2v2", 4,
        "3v3", 6
    );
    
    private final int maxPlayers;

    private final GameWorld world;
    private final GameLoop loop;
    private final Map<WebSocket, Integer> connectionToEntity;
    private int nextSlot = 0;

    private Room(GameWorld world, GameLoop loop, Map<WebSocket, Integer> connectionToEntity, int maxPlayers) {
        this.world = world;
        this.loop = loop;
        this.connectionToEntity = connectionToEntity;
        this.maxPlayers = maxPlayers;
    }

    // Legacy create for backwards compatibility (creates 1v1 room)
    public static Room create(GameServer server) {
        return create(server, "1v1", Map.of());
    }

    public static Room create(GameServer server, String gameMode, Map<WebSocket, String> playerUsernames) {
        int maxPlayers = PLAYERS_PER_MODE.getOrDefault(gameMode, 2);
        
        GameWorld world = new GameWorld();
        Queue<InputPacket> inputQueue = server.getInputQueue();
        Map<WebSocket, Integer> connMap = new ConcurrentHashMap<>();
        GameLoop loop = new GameLoop(world, inputQueue, server, connMap);

        Room room = new Room(world, loop, connMap, maxPlayers);

        // Create the static map entity (persists forever)
        int mapEntity = world.createEntity();
        StaticMapComponent map = new StaticMapComponent(new String[]{
            "##############",
            "#......2.....#",
            "#.##......##.#",
            "#..##....##..#",
            "##....##....##",
            "##....##....##",
            "#..##....##..#",
            "#.##......##.#",
            "#......1.....#",
            "##############"
        });
        world.addComponent(StaticMapComponent.class, mapEntity, map);

        // Create the singleton match-state entity
        int matchEntity = world.createEntity();
        world.addComponent(RoundStateComponent.class, matchEntity, new RoundStateComponent());

        // Add initial players
        for (Map.Entry<WebSocket, String> entry : playerUsernames.entrySet()) {
            room.addPlayer(entry.getKey(), entry.getValue());
        }

        loop.start();
        return room;
    }

    public void addPlayer(WebSocket conn) {
        addPlayer(conn, null);
    }

    public void addPlayer(WebSocket conn, String username) {
        if (nextSlot >= maxPlayers) return;

        int slot = nextSlot++;
        // Get spawn points from static map
        Set<Integer> mapEntities = world.getAllEntitiesWithComponent(StaticMapComponent.class);
        StaticMapComponent map = null;
        if (!mapEntities.isEmpty()) {
            map = world.getComponent(StaticMapComponent.class, mapEntities.iterator().next());
        }
        
        // Calculate spawn position - spread players across the two team spawns
        float spawnX, spawnY;
        if (map != null) {
            // Team 1: slots 0,2,4 spawn near p1
            // Team 2: slots 1,3,5 spawn near p2
            boolean isTeam1 = (slot % 2) == 0;
            float baseX = isTeam1 ? map.p1SpawnX : map.p2SpawnX;
            float baseY = isTeam1 ? map.p1SpawnY : map.p2SpawnY;
            
            // Add small offset based on slot index within team (0, 1, 2)
            int teamSlot = slot / 2;
            float offsetX = (teamSlot - 1) * 40f; // -40, 0, +40
            float offsetY = (teamSlot - 1) * 30f; // -30, 0, +30
            
            spawnX = baseX + offsetX;
            spawnY = baseY + offsetY;
        } else {
            spawnX = 384f;
            spawnY = 300f;
        }

        int entityId = world.createEntity();

        world.addComponent(PlayerSlotComponent.class, entityId, new PlayerSlotComponent(slot));
        world.addComponent(UsernameComponent.class, entityId, new UsernameComponent(username));
        world.addComponent(SpawnPointComponent.class, entityId, new SpawnPointComponent(spawnX, spawnY));
        world.addComponent(PositionComponent.class, entityId, new PositionComponent(spawnX, spawnY));
        world.addComponent(HealthComponent.class, entityId, new HealthComponent(100));
        world.addComponent(InputComponent.class, entityId, new InputComponent());
        world.addComponent(WeaponComponent.class, entityId, new WeaponComponent(10, (short) 50, (short) 50, 3, 90));
        world.addComponent(ScoreComponent.class, entityId, new ScoreComponent());
        world.addComponent(VisionComponent.class, entityId, new VisionComponent(400f, 90f));

        connectionToEntity.put(conn, entityId);

        log.info("Player joined room — slot {}, entityId {}", slot, entityId);
    }
    
    // Handle input from lobby - converts JSON to InputPacket and adds to queue
    public void handleInput(int entityId, JsonObject packet) {
        // This will be called from LobbyManager to forward player inputs
        // The actual processing happens via InputSystem in the game loop
    }

    public void removePlayer(WebSocket conn) {
        Integer entityId = connectionToEntity.remove(conn);
        if (entityId != null) {
            world.destroyEntity(entityId);
            nextSlot--;
            log.info("Player left room — entityId {}", entityId);
        }
    }

    public boolean isFull() {
        return nextSlot >= maxPlayers;
    }

    public boolean isEmpty() {
        return nextSlot == 0;
    }

    public Integer getEntityId(WebSocket conn) {
        return connectionToEntity.get(conn);
    }

    public GameWorld getWorld() {
        return world;
    }

    public Map<WebSocket, Integer> getConnectionToEntity() {
        return connectionToEntity;
    }

    public void shutdown() {
        log.info("Shutting down room");
        loop.stop();
    }
}
