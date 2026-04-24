package com.gunfight.engine.ecs;

import java.util.*;

/**
 * World manages all entities and their components in the ECS architecture.
 * Stores the relationship: Entity ID → Map of Component Types → Component Instances
 */
public class World {

    // List of all entity objects
    private List<Entity> entities = new ArrayList<>();
    
    // Maps entity IDs to their component maps
    // Each entity has a map of component types to component instances
    private Map<Integer, Map<Class<? extends Component>, Component>> componentData = new HashMap<>();
    
    // List of systems that process entities
    private List<EcsSystem> systems = new ArrayList<>();

    /**
     * Registers a new entity in the world.
     * Creates an empty component map for the entity.
     * @param entity the entity to add
     */
    public void addEntity(Entity entity) {
        entities.add(entity);
        componentData.put(entity.getId(), new HashMap<>());
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
    public void addSystem(EcsSystem system) {
        systems.add(system);
    }

    /**
     * Adds a component to an entity.
     * @param entity the entity to add the component to
     * @param component the component to add
     */
    public void addComponent(Entity entity, Component component) {
        componentData.get(entity.getId()).put(component.getClass(), component);
    }

    /**
     * Retrieves a component of a specific type from an entity.
     * @param entity the entity to get the component from
     * @param type the component type
     * @return the component, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Entity entity, Class<T> type) {
        return (T) componentData.get(entity.getId()).get(type);
    }

    /**
     * Returns all entities in the world.
     * @return list of entity objects
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * Returns the raw component data map for iteration.
     * Maps entity IDs to their component maps.
     * @return the component data map
     */
    public Map<Integer, Map<Class<? extends Component>, Component>> getComponentData() {
        return componentData;
    }
    
    /**
     * Updates all systems in the world.
     */
    public void update() {
        for (EcsSystem system : systems) {
            system.update(this);
        }
    }
}
