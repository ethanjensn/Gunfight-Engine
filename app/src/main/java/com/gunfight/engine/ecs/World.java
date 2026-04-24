package com.gunfight.engine.ecs;

import java.util.*;

/**
 * World manages all entities and their components in the ECS architecture.
 * Stores the relationship: Entity ID → Map of Component Types → Component Instances
 */
public class World {

    // Maps entity IDs to their component maps
    // Each entity has a map of component types to component instances
    private Map<Integer, Map<Class<? extends Component>, Component>> entities = new HashMap<>();
    
    // List of systems that process entities
    private List<System> systems = new ArrayList<>();

    /**
     * Registers a new entity in the world.
     * Creates an empty component map for the entity.
     * @param entity the entity to add
     */
    public void addEntity(Entity entity) {
        entities.put(entity.getId(), new HashMap<>());
    }
    
    /**
     * Creates a new entity and registers it in the world.
     * @return the newly created entity
     */
    public Entity createEntity() {
        Entity entity = new Entity();
        addEntity(entity);
        return entity;
    }
    
    /**
     * Adds a system to the world.
     * @param system the system to add
     */
    public void addSystem(System system) {
        systems.add(system);
    }

    /**
     * Adds a component to an entity.
     * @param entity the entity to add the component to
     * @param component the component to add
     */
    public void addComponent(Entity entity, Component component) {
        entities.get(entity.getId()).put(component.getClass(), component);
    }

    /**
     * Retrieves a component of a specific type from an entity.
     * @param entity the entity to get the component from
     * @param type the component type
     * @return the component, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Entity entity, Class<T> type) {
        return (T) entities.get(entity.getId()).get(type);
    }

    /**
     * Returns all entity IDs in the world.
     * @return collection of entity IDs
     */
    public Collection<Integer> getEntities() {
        return entities.keySet();
    }

    /**
     * Returns the raw entity data map for iteration.
     * Maps entity IDs to their component maps.
     * @return the entities map
     */
    public Map<Integer, Map<Class<? extends Component>, Component>> getEntityData() {
        return entities;
    }
    
    /**
     * Updates all systems in the world.
     */
    public void update() {
        for (System system : systems) {
            system.update(this);
        }
    }
}
