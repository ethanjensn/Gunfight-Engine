package com.gunfight.logic;

import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.HealthComponent;
import com.gunfight.data.ProjectileComponent;

public class CombatSystem {
    private static final float PLAYER_RADIUS = 16f;
    private static final float PROJECTILE_RADIUS = 4f;
    private static final float HIT_DISTANCE = PLAYER_RADIUS + PROJECTILE_RADIUS;
    private static final float HIT_DISTANCE_SQ = HIT_DISTANCE * HIT_DISTANCE;

    private final ProjectilePool projectilePool;

    public CombatSystem(ProjectilePool projectilePool) {
        this.projectilePool = projectilePool;
    }

    public void update(GameWorld world) {
        Set<Integer> projectileEntities = world.getAllEntitiesWithComponent(ProjectileComponent.class);
        Set<Integer> playerEntities = world.getAllEntitiesWithComponent(HealthComponent.class);

        for (int bulletId : projectileEntities) {
            ProjectileComponent proj = world.getComponent(ProjectileComponent.class, bulletId);
            if (proj == null || !proj.active) continue;

            PositionComponent bulletPos = world.getComponent(PositionComponent.class, bulletId);
            if (bulletPos == null) continue;

            for (int playerId : playerEntities) {
                // Don't hit the player who fired this bullet
                if (playerId == proj.ownerId) continue;

                PositionComponent playerPos = world.getComponent(PositionComponent.class, playerId);
                if (playerPos == null) continue;

                // Circle-vs-circle: compare squared distance to avoid sqrt
                float dx = bulletPos.x - (playerPos.x + PLAYER_RADIUS);
                float dy = bulletPos.y - (playerPos.y + PLAYER_RADIUS);
                float distSq = dx * dx + dy * dy;

                if (distSq <= HIT_DISTANCE_SQ) {
                    // Hit! Apply damage and release bullet
                    HealthComponent health = world.getComponent(HealthComponent.class, playerId);
                    if (health != null) {
                        health.health -= proj.damage;
                    }
                    projectilePool.release(bulletId);
                    break; // Bullet can only hit one player
                }
            }
        }
    }
}
