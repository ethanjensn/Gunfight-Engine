package com.gunfight.engine;

import java.util.Set;

public class GameWorld {
    private EntityManager entityManager = new EntityManager();
    private ComponentRegistry componentRegistry = new ComponentRegistry();

    // Creates an entity and returns its ID
    public int createEntity() {
        return entityManager.createEntity();
    }

    // Attaches a component to an entity
    public <T> void addComponent(Class<T> classType, int entityId, T component) {
        componentRegistry.addComponent(classType, entityId, component);
    }

    // Retrieves a component from an entity
    public <T> T getComponent(Class<T> classType, int entityId) {
        return componentRegistry.getComponent(classType, entityId);
    }

    // Destroys an entity and cleans up all its components
    public void destroyEntity(int entityId) {
        entityManager.destroyEntity(entityId);
        componentRegistry.removeAllComponents(entityId);
    }

    // Gets all entities with a specific component
    public <T> Set<Integer> getAllEntitiesWithComponent(Class<T> classType) {
        return componentRegistry.getAllEntitiesWithComponent(classType);
    }
}
