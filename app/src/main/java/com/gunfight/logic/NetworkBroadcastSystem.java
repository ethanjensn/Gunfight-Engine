package com.gunfight.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.ProjectileComponent;
import com.gunfight.data.WeaponComponent;
import com.gunfight.data.HealthComponent;
import com.gunfight.net.GameServer;
import com.gunfight.net.GameStatePacket;
import com.gunfight.net.GameStatePacket.PlayerState;
import com.gunfight.net.GameStatePacket.ProjectileState;
import com.google.gson.Gson;

public class NetworkBroadcastSystem {
    private GameServer server;
    private Gson gson = new Gson();

    // Object pool: pre-allocated PlayerState objects that get reused
    private final List<PlayerState> playerPool = new ArrayList<>();
    private final List<ProjectileState> projectilePool = new ArrayList<>();
    
    // Active states for this tick
    private final List<PlayerState> activeStates = new ArrayList<>();
    private final List<ProjectileState> activeProjectiles = new ArrayList<>();
    
    // Permanent packet - Gson reads from active states each tick
    private final GameStatePacket packet = new GameStatePacket(activeStates, activeProjectiles);

    public NetworkBroadcastSystem(GameServer server) {
        this.server = server;
    }

    public void update(GameWorld world, int currentTick) {
        // activates exactly once per tick
        // Clear active lists (objects remain in pools for reuse)
        activeStates.clear();
        activeProjectiles.clear();

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

            // 2. If we reach here, proj == null. It MUST be a player.
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
                state.maxHealth = 100;
            }

            activeStates.add(state);
            playerIndex++;
        }

        // Reuse same packet - Gson serializes active states which now point to recycled objects
        String json = gson.toJson(packet);
        server.broadcast(json);
    }
}
