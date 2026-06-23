package com.gunfight.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {

    @Test
    void createEntityReturnsSequentialIds() {
        EntityManager manager = new EntityManager();
        assertEquals(0, manager.createEntity());
        assertEquals(1, manager.createEntity());
        assertEquals(2, manager.createEntity());
    }

    @Test
    void destroyedEntityIdIsReused() {
        EntityManager manager = new EntityManager();
        int id0 = manager.createEntity();
        int id1 = manager.createEntity();
        manager.destroyEntity(id0);
        assertFalse(manager.isActive(id0));
        assertEquals(id0, manager.createEntity(), "destroyed ID should be reused");
    }

    @Test
    void isActiveReflectsEntityState() {
        EntityManager manager = new EntityManager();
        int id = manager.createEntity();
        assertTrue(manager.isActive(id));
        manager.destroyEntity(id);
        assertFalse(manager.isActive(id));
        assertFalse(manager.isActive(999));
    }
}
