package com.gunfight.engine.ecs;

/**
 * Represents an entity in the ECS (Entity Component System) architecture.
 * An entity is simply a unique ID that acts as a container for components.
 * Components hold the actual data (position, velocity, health, etc.).
 */
public class Entity {
    // Static counter for generating unique entity IDs
    private static int nextId = 0;
    // Unique identifier for this entity
    private int id;

    /**
     * Creates a new entity with a unique ID.
     * IDs are assigned sequentially from a static counter.
     */
    public Entity() {
        this.id = nextId++;
    }

    /**
     * Returns the unique ID of this entity.
     * @return the entity ID
     */
    public int getId() {
        return id;
    }
}
