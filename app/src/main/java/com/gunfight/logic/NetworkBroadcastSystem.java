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
import com.gunfight.data.UsernameComponent;
import com.gunfight.data.VisionComponent;
import com.gunfight.data.PlayerSlotComponent;
import com.gunfight.net.GameServer;
import com.gunfight.net.GameStatePacket;
import com.gunfight.net.GameStatePacket.PlayerState;
import com.gunfight.net.GameStatePacket.ProjectileState;
import com.gunfight.net.GameStatePacket.WallState;
import com.gunfight.metrics.GameMetrics;
import com.gunfight.data.StaticMapComponent;
import com.google.gson.Gson;

public class NetworkBroadcastSystem {
    private final Map<WebSocket, Integer> connectionToEntity;
    private final GameMetrics metrics;
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

    // Reused winner-name lists for this tick
    private final List<String> roundWinners = new ArrayList<>();
    private final List<String> matchWinners = new ArrayList<>();

    // Permanent packet — Gson reads from filteredStates per client
    private final GameStatePacket packet = new GameStatePacket(filteredStates, activeProjectiles, activeWalls);

    public NetworkBroadcastSystem(GameServer server, Map<WebSocket, Integer> connectionToEntity, GameMetrics metrics) {
        this.connectionToEntity = connectionToEntity;
        this.metrics = metrics;
    }

    public void update(GameWorld world, int currentTick) {
        // activates exactly once per tick
        // Clear active lists (objects remain in pools for reuse)
        activeStates.clear();
        activeProjectiles.clear();
        activeWalls.clear();
        roundWinners.clear();
        matchWinners.clear();

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

            UsernameComponent username = world.getComponent(UsernameComponent.class, entityId);
            state.username = username != null ? username.username : null;

            PlayerSlotComponent slot = world.getComponent(PlayerSlotComponent.class, entityId);
            state.slot = slot != null ? slot.slot : -1;

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
        RoundStateComponent roundState = null;
        Set<Integer> matchEntities = world.getAllEntitiesWithComponent(RoundStateComponent.class);
        if (!matchEntities.isEmpty()) {
            roundState = world.getComponent(RoundStateComponent.class,
                    matchEntities.iterator().next());
            if (roundState != null) {
                packet.phase = roundState.phase.name();
                packet.roundNumber = roundState.roundNumber;

                // Derive winner usernames from the winning team and the username component
                if (roundState.roundWinnerTeam >= 0) {
                    collectUsernamesByTeam(world, roundState.roundWinnerTeam, roundWinners);
                }
                if (roundState.matchWinnerTeam >= 0) {
                    collectUsernamesByTeam(world, roundState.matchWinnerTeam, matchWinners);
                }
            }
        }
        packet.roundWinners = roundWinners;
        packet.matchWinners = matchWinners;

        // Send a per-client filtered packet — teammates always visible, enemies by vision
        for (Map.Entry<WebSocket, Integer> entry : connectionToEntity.entrySet()) {
            WebSocket conn = entry.getKey();
            int observerId = entry.getValue();

            VisionComponent vision = world.getComponent(VisionComponent.class, observerId);
            PlayerSlotComponent observerSlot = world.getComponent(PlayerSlotComponent.class, observerId);
            int observerTeam = observerSlot != null ? observerSlot.slot % 2 : -1;

            filteredStates.clear();
            if (vision == null) {
                // No vision component — fall back to sending all players
                filteredStates.addAll(activeStates);
            } else {
                for (PlayerState ps : activeStates) {
                    boolean isTeammate = observerTeam >= 0 && ps.slot % 2 == observerTeam;
                    if (isTeammate || vision.visibleIds.contains(ps.id)) {
                        filteredStates.add(ps);
                    }
                }
            }

            long serializeStart = System.nanoTime();
            String json = gson.toJson(packet);
            if (metrics != null) {
                metrics.recordSerializationNs(System.nanoTime() - serializeStart);
            }
            if (conn.isOpen()) {
                conn.send(json);
                if (metrics != null) {
                    metrics.recordPacket(json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
                }
            }
        }
    }

    private void collectUsernamesByTeam(GameWorld world, int team, List<String> out) {
        out.clear();
        Set<Integer> players = world.getAllEntitiesWithComponent(PlayerSlotComponent.class);
        for (int entityId : players) {
            PlayerSlotComponent slot = world.getComponent(PlayerSlotComponent.class, entityId);
            if (slot == null || slot.slot % 2 != team) continue;
            UsernameComponent username = world.getComponent(UsernameComponent.class, entityId);
            if (username != null && username.username != null) {
                out.add(username.username);
            }
        }
    }
}
