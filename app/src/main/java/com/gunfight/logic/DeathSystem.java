package com.gunfight.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.HealthComponent;
import com.gunfight.data.InputComponent;
import com.gunfight.data.WeaponComponent;
import com.gunfight.data.RespawnComponent;

public class DeathSystem {
    private static final int RESPAWN_TICKS = 180; // 3 seconds at 60 tps

    // Reusable list to avoid allocating every tick
    private final List<Integer> deadEntities = new ArrayList<>();

    public void update(GameWorld world) {
        Set<Integer> entities = world.getAllEntitiesWithComponent(HealthComponent.class);
        deadEntities.clear();

        // Collect dead entities first to avoid modifying sets during iteration
        for (int entityId : entities) {
            HealthComponent hp = world.getComponent(HealthComponent.class, entityId);
            if (hp == null) continue;

            // Already pending respawn — skip
            RespawnComponent respawn = world.getComponent(RespawnComponent.class, entityId);
            if (respawn != null) continue;

            if (hp.health <= 0) {
                deadEntities.add(entityId);
            }
        }

        // Process deaths
        for (int entityId : deadEntities) {
            // Strip capability — entity disappears from movement/weapon systems
            world.removeComponent(InputComponent.class, entityId);
            world.removeComponent(WeaponComponent.class, entityId);

            // Attach respawn state — spawn at top-left (0, 0)
            world.addComponent(RespawnComponent.class, entityId,
                new RespawnComponent(RESPAWN_TICKS, 0f, 0f));
        }
    }
}
