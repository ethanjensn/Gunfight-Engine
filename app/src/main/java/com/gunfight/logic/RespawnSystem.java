package com.gunfight.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.HealthComponent;
import com.gunfight.data.InputComponent;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.WeaponComponent;
import com.gunfight.data.RespawnComponent;

public class RespawnSystem {
    // Reusable list to avoid allocating every tick
    private final List<Integer> readyEntities = new ArrayList<>();

    public void update(GameWorld world) {
        Set<Integer> entities = world.getAllEntitiesWithComponent(RespawnComponent.class);
        readyEntities.clear();

        for (int entityId : entities) {
            RespawnComponent respawn = world.getComponent(RespawnComponent.class, entityId);
            if (respawn == null) continue;

            respawn.respawnTimer--;

            if (respawn.respawnTimer <= 0) {
                readyEntities.add(entityId);
            }
        }

        // Process respawns
        for (int entityId : readyEntities) {
            RespawnComponent respawn = world.getComponent(RespawnComponent.class, entityId);

            // Reset position to spawn point
            PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
            if (pos != null) {
                pos.x = respawn.spawnX;
                pos.y = respawn.spawnY;
            }

            // Reset health to full
            HealthComponent hp = world.getComponent(HealthComponent.class, entityId);
            if (hp != null) {
                hp.health = hp.maxHealth;
            }

            // Restore capability
            world.addComponent(InputComponent.class, entityId, new InputComponent());
            world.addComponent(WeaponComponent.class, entityId,
                new WeaponComponent(10, (short) 50, (short) 50, 3, 90));

            // Clean up — entity leaves the respawn system
            world.removeComponent(RespawnComponent.class, entityId);
        }
    }
}
