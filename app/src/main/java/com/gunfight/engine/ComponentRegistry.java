package com.gunfight.engine;

import java.util.Map;
import java.util.HashMap;

// Think of it like an Excel sheet where the Rows are the IDs and the Columns are the "Attachments" (Health, Position, etc.).

public class ComponentRegistry {
    // (The Outer Key, The Inner Key, and The Value)
    private Map<Class<?>, Map<Integer, Object>> components = new HashMap<>();
    // ^      ^             ^
    // |      |             └── the VALUE: another map (entity ID → component data)
    // |      └── the KEY: which component type? (HealthComponent.class, etc.)
    // └── outer map

    public <T> void addComponent(Class<T> classType, int entityId, T component) {
        // If the component type doesn't exist, create a new map for it
        components.computeIfAbsent(classType, k -> new HashMap<>()).put(entityId, component);
    }
    
    public <T> T getComponent(Class<T> classType, int entityId) {
        // Cast the component to the requested type
        return classType.cast(components.get(classType).get(entityId));
    }

    public void removeAllComponents(int entityId) {
        for (Map<Integer, Object> componentMap : components.values()) {
            componentMap.remove(entityId);
        }
    }

    public <T> java.util.Set<Integer> getAllEntitiesWithComponent(Class<T> classType) {
        // Return an empty set if the component type doesn't exist
        return components.getOrDefault(classType, java.util.Collections.emptyMap()).keySet();
    }
}
