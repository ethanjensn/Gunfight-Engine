package com.gunfight.logic;

import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.VelocityComponent;
import com.gunfight.data.ProjectileComponent;
import com.gunfight.data.StaticMapComponent;

public class ProjectileSystem {
    private static final float CANVAS_WIDTH = 800f;
    private static final float CANVAS_HEIGHT = 600f;

    private final ProjectilePool projectilePool;

    public ProjectileSystem(ProjectilePool projectilePool) {
        this.projectilePool = projectilePool;
    }

    public void update(GameWorld world) {
        Set<Integer> entities = world.getAllEntitiesWithComponent(ProjectileComponent.class);
        
        for (int entityId : entities) {
            ProjectileComponent projectile = world.getComponent(ProjectileComponent.class, entityId);
            
            if (projectile == null || !projectile.active) {
                continue;
            }
            
            PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
            VelocityComponent vel = world.getComponent(VelocityComponent.class, entityId);
            
            if (pos != null && vel != null) {
                pos.x += vel.vx;
                pos.y += vel.vy;

                // Check bounds
                if (pos.x < 0 || pos.x > CANVAS_WIDTH || pos.y < 0 || pos.y > CANVAS_HEIGHT) {
                    projectilePool.release(entityId);
                    continue;
                }

                // Check wall collisions
                if (hitsWall(world, pos.x, pos.y)) {
                    projectilePool.release(entityId);
                    continue;
                }
            }

            // Always decrement life and check it
            projectile.lifeTicks--;
            if (projectile.lifeTicks <= 0) {
                projectilePool.release(entityId);
            }
        }
    }

    private boolean hitsWall(GameWorld world, float bx, float by) {
        // Get static map component (singleton per room)
        Set<Integer> mapEntities = world.getAllEntitiesWithComponent(StaticMapComponent.class);
        if (mapEntities.isEmpty()) return false;
        StaticMapComponent map = world.getComponent(StaticMapComponent.class, mapEntities.iterator().next());
        if (map == null) return false;

        // Convert position to tile coordinates
        int tx = (int)(bx / StaticMapComponent.TILE_W);
        int ty = (int)(by / StaticMapComponent.TILE_H);

        // Check if within bounds and solid
        return map.isWall(tx, ty);
    }
}
