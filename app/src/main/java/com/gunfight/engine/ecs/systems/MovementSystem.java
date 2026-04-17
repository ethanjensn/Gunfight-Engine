package com.gunfight.engine.ecs.systems;

import com.gunfight.engine.ecs.World;
import com.gunfight.engine.ecs.components.Transform;
import com.gunfight.engine.ecs.components.Velocity;

/**
 * System that updates movement for entities with Transform and Velocity components.
 * Movement is now data-driven - each entity has its own velocity.
 */
public class MovementSystem {

    /**
     * Updates all entities with Transform and Velocity components by applying velocity to position.
     * @param world the ECS world containing entities and components
     */
    public void update(World world) {
        for (var entry : world.getEntityData().entrySet()) {
            var components = entry.getValue();

            if (components.containsKey(Transform.class) &&
                components.containsKey(Velocity.class)) {

                Transform t = (Transform) components.get(Transform.class);
                Velocity v = (Velocity) components.get(Velocity.class);

                // Apply velocity to position
                t.x += v.x;
                t.y += v.y;
                t.z += v.z;
            }
        }
    }
}
