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
     * This is the physics update step - moves entities based on their velocity.
     * @param world the ECS world containing entities and components
     */
    public void update(World world, float deltaTime) {
        // Iterate over all entities in the world
        // Each entry maps: entity ID → map of component types → component instances
        for (var entry : world.getEntityData().entrySet()) {
            var components = entry.getValue();

            // Check if this entity has BOTH Transform AND Velocity components
            // Only entities with both components should be moved by this system
            if (components.containsKey(Transform.class) &&
                components.containsKey(Velocity.class)) {

                // Retrieve the Transform component (holds position: x, y, z)
                Transform t = (Transform) components.get(Transform.class);
                // Retrieve the Velocity component (holds speed: vx, vy, vz)
                Velocity v = (Velocity) components.get(Velocity.class);

                // Apply velocity to position (physics: position += velocity)
                // This moves the entity by its velocity amount each update
                t.x += v.x * deltaTime;  // Move along x-axis
                t.y += v.y * deltaTime;  // Move along y-axis
                t.z += v.z * deltaTime;  // Move along z-axis
            }
        }
    }
}
