package com.gunfight.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java_websocket.WebSocket;

import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.ProjectileComponent;
import com.gunfight.data.RoundStateComponent;
import com.gunfight.data.ScoreComponent;
import com.gunfight.data.WeaponComponent;
import com.gunfight.data.HealthComponent;
import com.gunfight.data.RespawnComponent;
import com.gunfight.data.VisionComponent;
import com.gunfight.net.GameServer;
import com.gunfight.net.GameStatePacket;
import com.gunfight.net.GameStatePacket.PlayerState;
import com.gunfight.net.GameStatePacket.ProjectileState;
import com.gunfight.net.GameStatePacket.WallState;
import com.gunfight.data.StaticMapComponent;
import com.google.gson.Gson;

public class NetworkBroadcastSystem {
    private final Map<WebSocket, Integer> connectionToEntity;
    private Gson gson = new Gson();

    // Object pool: pre-allocated PlayerState objects that get reused
    private final List<PlayerState> playerPool = new ArrayList<>();
    private final List<ProjectileState> projectilePool = new ArrayList<>();
    private final List<WallState> wallPool = new ArrayList<>();

    // Active states for this tick (full world view, built once)
    private final List<PlayerState> activeStates = new ArrayList<>();
    private final List<ProjectileState> activeProjectiles = new ArrayList<>();
    private final List<WallState> activeWalls = new ArrayList<>();

    // Scratch list reused per-client — no allocation each tick
    private final List<PlayerState> filteredStates = new ArrayList<>();

    // Permanent packet — Gson reads from filteredStates per client
    private final GameStatePacket packet = new GameStatePacket(filteredStates, activeProjectiles, activeWalls);

    public NetworkBroadcastSystem(GameServer server, Map<WebSocket, Integer> connectionToEntity) {
        this.connectionToEntity = connectionToEntity;
    }

    public void update(GameWorld world, int currentTick) {
        // activates exactly once per tick
        // Clear active lists (objects remain in pools for reuse)
        activeStates.clear();
        activeProjectiles.clear();
        activeWalls.clear();

        // Collect player states (entities with Position but no ProjectileComponent)
        Set<Integer> entities = world.getAllEntitiesWithComponent(PositionComponent.class);

        int playerIndex = 0;
        int projectileIndex = 0;
        
        for (int entityId : entities) {
            PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
            if (pos == null) continue;

            ProjectileComponent proj = world.getComponent(ProjectileComponent.class, entityId);
            
            // 1. Is it a bullet?
            if (proj != null) {
                if (proj.active) {
                    // It's an ACTIVE bullet, add to projectile pool
                    if (projectileIndex >= projectilePool.size()) {
                        projectilePool.add(new ProjectileState(0, 0, 0));
                    }
                    ProjectileState state = projectilePool.get(projectileIndex);
                    state.id = entityId;
                    state.x = pos.x;
                    state.y = pos.y;
                    activeProjectiles.add(state);
                    projectileIndex++;
                }
                // If it IS a bullet, but NOT active, just skip it!
                continue;
            }

            // 2. Walls are no longer entities — handled via StaticMapComponent below

            // 3. If we reach here, it MUST be a player.
            if (playerIndex >= playerPool.size()) {
                playerPool.add(new PlayerState(0, 0, 0));
            }
            PlayerState state = playerPool.get(playerIndex);
            state.id = entityId;
            state.x = pos.x;
            state.y = pos.y;

            WeaponComponent weapon = world.getComponent(WeaponComponent.class, entityId);
            if (weapon != null) {
                state.ammo = weapon.ammo;
                state.maxAmmo = weapon.maxAmmo;
                state.reloading = weapon.reloading;
                if (weapon.reloading && weapon.reloadTicks > 0) {
                    int elapsed = currentTick - weapon.reloadStartTick;
                    state.reloadProgress = Math.min(1.0f, (float) elapsed / weapon.reloadTicks);
                } else {
                    state.reloadProgress = 0f;
                }
            }

            HealthComponent hp = world.getComponent(HealthComponent.class, entityId);
            if (hp != null) {
                state.health = hp.health;
                state.maxHealth = hp.maxHealth;
            }

            RespawnComponent respawn = world.getComponent(RespawnComponent.class, entityId);
            state.dead = (respawn != null);

            ScoreComponent score = world.getComponent(ScoreComponent.class, entityId);
            if (score != null) {
                state.wins = score.wins;
                state.readyForRematch = score.readyForRematch;
            }

            activeStates.add(state);
            playerIndex++;
        }

        // Collect wall states from static map
        Set<Integer> mapEntities = world.getAllEntitiesWithComponent(StaticMapComponent.class);
        int wallIndex = 0;
        if (!mapEntities.isEmpty()) {
            StaticMapComponent map = world.getComponent(StaticMapComponent.class, mapEntities.iterator().next());
            if (map != null) {
                for (int tx = 0; tx < StaticMapComponent.COLS; tx++) {
                    for (int ty = 0; ty < StaticMapComponent.ROWS; ty++) {
                        if (!map.solid[tx][ty]) continue;
                        if (wallIndex >= wallPool.size()) {
                            wallPool.add(new WallState(0, 0, 0, 0));
                        }
                        WallState ws = wallPool.get(wallIndex);
                        ws.x = tx * StaticMapComponent.TILE_W;
                        ws.y = ty * StaticMapComponent.TILE_H;
                        ws.w = StaticMapComponent.TILE_W;
                        ws.h = StaticMapComponent.TILE_H;
                        activeWalls.add(ws);
                        wallIndex++;
                    }
                }
            }
        }

        // Populate round state from the singleton match entity
        Set<Integer> matchEntities = world.getAllEntitiesWithComponent(RoundStateComponent.class);
        if (!matchEntities.isEmpty()) {
            RoundStateComponent roundState = world.getComponent(RoundStateComponent.class,
                    matchEntities.iterator().next());
            if (roundState != null) {
                packet.phase = roundState.phase.name();
                packet.roundNumber = roundState.roundNumber;
            }
        }

        // Send a per-client filtered packet — only include players visible to that client
        for (Map.Entry<WebSocket, Integer> entry : connectionToEntity.entrySet()) {
            WebSocket conn = entry.getKey();
            int observerId = entry.getValue();

            VisionComponent vision = world.getComponent(VisionComponent.class, observerId);

            filteredStates.clear();
            if (vision == null) {
                // No vision component — fall back to sending all players
                filteredStates.addAll(activeStates);
            } else {
                for (PlayerState ps : activeStates) {
                    if (vision.visibleIds.contains(ps.id)) {
                        filteredStates.add(ps);
                    }
                }
            }

            String json = gson.toJson(packet);
            if (conn.isOpen()) {
                conn.send(json);
            }
        }
    }
}
