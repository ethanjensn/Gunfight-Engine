package com.gunfight.engine.ecs.systems;

import com.gunfight.engine.ecs.Entity;
import com.gunfight.engine.ecs.EcsSystem;
import com.gunfight.engine.ecs.World;
import com.gunfight.engine.ecs.components.Transform;
import com.gunfight.engine.ecs.components.Velocity;

/**
 * System that updates movement for entities with Transform and Velocity components.
 * Movement is now data-driven - each entity has its own velocity.
 */
public class MovementSystem extends EcsSystem {

    /**
     * Updates all entities with Transform and Velocity components by applying velocity to position.
     * This is the physics update step - moves entities based on their velocity.
     * @param world the ECS world containing entities and components
     */
    @Override
    public void update(World world) {
        for (Entity entity : world.getEntities()) {
            Transform t = world.getComponent(entity, Transform.class);
            Velocity v = world.getComponent(entity, Velocity.class);

            if (t != null && v != null) {
                t.x += v.x;
                t.y += v.y;
            }
        }
    }
}
