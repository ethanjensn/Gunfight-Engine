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
        return (T) components.get(classType).get(entityId);
    }
}
