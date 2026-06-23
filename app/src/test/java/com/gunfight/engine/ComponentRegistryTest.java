package com.gunfight.engine;

import com.gunfight.data.PositionComponent;
import com.gunfight.data.HealthComponent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComponentRegistryTest {

    @Test
    void addAndRetrieveComponent() {
        ComponentRegistry registry = new ComponentRegistry();
        PositionComponent pos = new PositionComponent(10f, 20f);
        registry.addComponent(PositionComponent.class, 0, pos);

        PositionComponent retrieved = registry.getComponent(PositionComponent.class, 0);
        assertNotNull(retrieved);
        assertEquals(10f, retrieved.x);
        assertEquals(20f, retrieved.y);
    }

    @Test
    void missingComponentReturnsNull() {
        ComponentRegistry registry = new ComponentRegistry();
        assertNull(registry.getComponent(PositionComponent.class, 0));
    }

    @Test
    void removeComponent() {
        ComponentRegistry registry = new ComponentRegistry();
        registry.addComponent(PositionComponent.class, 0, new PositionComponent(0f, 0f));
        registry.removeComponent(PositionComponent.class, 0);
        assertNull(registry.getComponent(PositionComponent.class, 0));
    }

    @Test
    void removeAllComponentsForEntity() {
        ComponentRegistry registry = new ComponentRegistry();
        registry.addComponent(PositionComponent.class, 0, new PositionComponent(0f, 0f));
        registry.addComponent(HealthComponent.class, 0, new HealthComponent(100));

        registry.removeAllComponents(0);
        assertNull(registry.getComponent(PositionComponent.class, 0));
        assertNull(registry.getComponent(HealthComponent.class, 0));
    }

    @Test
    void getAllEntitiesWithComponent() {
        ComponentRegistry registry = new ComponentRegistry();
        registry.addComponent(PositionComponent.class, 0, new PositionComponent(0f, 0f));
        registry.addComponent(PositionComponent.class, 2, new PositionComponent(1f, 1f));

        assertEquals(2, registry.getAllEntitiesWithComponent(PositionComponent.class).size());
        assertTrue(registry.getAllEntitiesWithComponent(PositionComponent.class).contains(0));
        assertTrue(registry.getAllEntitiesWithComponent(PositionComponent.class).contains(2));
    }
}
