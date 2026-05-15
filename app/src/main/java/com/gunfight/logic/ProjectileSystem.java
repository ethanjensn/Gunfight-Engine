package com.gunfight.logic;

import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.VelocityComponent;
import com.gunfight.data.ProjectileComponent;

public class ProjectileSystem {
    private static final float CANVAS_WIDTH = 800f;
    private static final float CANVAS_HEIGHT = 600f;

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

                // Check bounds only if we actually have a position
                if (pos.x < 0 || pos.x > CANVAS_WIDTH || pos.y < 0 || pos.y > CANVAS_HEIGHT) {
                    projectile.active = false;
                }
            }

            // Always decrement life and check it
            projectile.lifeTicks--;
            if (projectile.lifeTicks <= 0) {
                projectile.active = false;
            }
        }
    }
}
