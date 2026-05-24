package com.gunfight.engine;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;

import com.gunfight.data.HealthComponent;
import com.gunfight.data.VisionComponent;
import com.gunfight.data.InputComponent;
import com.gunfight.data.PlayerSlotComponent;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.RoundStateComponent;
import com.gunfight.data.ScoreComponent;
import com.gunfight.data.SpawnPointComponent;
import com.gunfight.data.StaticMapComponent;
import com.gunfight.data.WeaponComponent;
import com.gunfight.net.GameServer;
import com.gunfight.net.InputPacket;

public class Room {
    private static final int MAX_PLAYERS = 2;

    private final GameWorld world;
    private final GameLoop loop;
    private final Map<WebSocket, Integer> connectionToEntity;
    private int nextSlot = 0;

    private Room(GameWorld world, GameLoop loop, Map<WebSocket, Integer> connectionToEntity) {
        this.world = world;
        this.loop = loop;
        this.connectionToEntity = connectionToEntity;
    }

    public static Room create(GameServer server) {
        GameWorld world = new GameWorld();
        Queue<InputPacket> inputQueue = server.getInputQueue();
        Map<WebSocket, Integer> connMap = new ConcurrentHashMap<>();
        GameLoop loop = new GameLoop(world, inputQueue, server, connMap);

        Room room = new Room(world, loop, connMap);

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

        loop.start();
        return room;
    }

    public void addPlayer(WebSocket conn) {
        if (nextSlot >= MAX_PLAYERS) return;

        int slot = nextSlot++;
        // Get spawn points from static map
        Set<Integer> mapEntities = world.getAllEntitiesWithComponent(StaticMapComponent.class);
        StaticMapComponent map = null;
        if (!mapEntities.isEmpty()) {
            map = world.getComponent(StaticMapComponent.class, mapEntities.iterator().next());
        }
        float spawnX = (slot == 0 && map != null) ? map.p1SpawnX : 
                       (slot == 1 && map != null) ? map.p2SpawnX : 384f;
        float spawnY = (slot == 0 && map != null) ? map.p1SpawnY : 
                       (slot == 1 && map != null) ? map.p2SpawnY : 300f;

        int entityId = world.createEntity();

        world.addComponent(PlayerSlotComponent.class, entityId, new PlayerSlotComponent(slot));
        world.addComponent(SpawnPointComponent.class, entityId, new SpawnPointComponent(spawnX, spawnY));
        world.addComponent(PositionComponent.class, entityId, new PositionComponent(spawnX, spawnY));
        world.addComponent(HealthComponent.class, entityId, new HealthComponent(100));
        world.addComponent(InputComponent.class, entityId, new InputComponent());
        world.addComponent(WeaponComponent.class, entityId, new WeaponComponent(10, (short) 50, (short) 50, 3, 90));
        world.addComponent(ScoreComponent.class, entityId, new ScoreComponent());
        world.addComponent(VisionComponent.class, entityId, new VisionComponent(400f, 90f));

        connectionToEntity.put(conn, entityId);

        System.out.println("Player joined room — slot " + slot + ", entityId " + entityId);
    }

    public void removePlayer(WebSocket conn) {
        Integer entityId = connectionToEntity.remove(conn);
        if (entityId != null) {
            world.destroyEntity(entityId);
            nextSlot--;
            System.out.println("Player left room — entityId " + entityId);
        }
    }

    public boolean isFull() {
        return nextSlot >= MAX_PLAYERS;
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
}
