package com.gunfight.logic;

import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.VelocityComponent;
import com.gunfight.data.ProjectileComponent;
import com.gunfight.data.WallComponent;

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
        Set<Integer> walls = world.getAllEntitiesWithComponent(WallComponent.class);
        for (int wallId : walls) {
            PositionComponent wp = world.getComponent(PositionComponent.class, wallId);
            WallComponent wc = world.getComponent(WallComponent.class, wallId);
            if (wp == null || wc == null) continue;
            if (bx >= wp.x && bx <= wp.x + wc.width &&
                by >= wp.y && by <= wp.y + wc.height) {
                return true;
            }
        }
        return false;
    }
}
