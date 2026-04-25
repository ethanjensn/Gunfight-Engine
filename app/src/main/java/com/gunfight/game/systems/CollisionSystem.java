package com.gunfight.game.systems;

import com.gunfight.engine.ecs.*;
import com.gunfight.engine.ecs.components.Transform;
import com.gunfight.game.components.*;

import java.util.ArrayList;
import java.util.List;

public class CollisionSystem extends EcsSystem {

    private static final float HIT_RADIUS = 2.0f;

    @Override
    public void update(World world) {

        List<Entity> toRemove = new ArrayList<>();

        List<Entity> entities = new ArrayList<>(world.getEntities());

        for (Entity projectile : entities) {

            ProjectileTag tag = world.getComponent(projectile, ProjectileTag.class);
            if (tag == null) continue;

            Transform pt = world.getComponent(projectile, Transform.class);
            if (pt == null) continue;

            for (Entity target : entities) {

                if (projectile == target) continue;

                HealthComponent health = world.getComponent(target, HealthComponent.class);
                Transform tt = world.getComponent(target, Transform.class);

                if (health == null || tt == null) continue;

                float dx = pt.x - tt.x;
                float dy = pt.y - tt.y;
                float distSq = dx * dx + dy * dy;

                if (distSq < HIT_RADIUS * HIT_RADIUS) {

                    health.hp -= 10;
                    toRemove.add(projectile);

                    System.out.println("HIT! Target HP: " + health.hp);
                }
            }
        }

        for (Entity e : toRemove) {
            world.removeEntity(e);
        }
    }
}
