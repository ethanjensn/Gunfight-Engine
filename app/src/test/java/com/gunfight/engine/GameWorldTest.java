package com.gunfight.engine;

import com.gunfight.data.PositionComponent;
import com.gunfight.data.HealthComponent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameWorldTest {

    @Test
    void createEntityAndAddComponent() {
        GameWorld world = new GameWorld();
        int entity = world.createEntity();
        world.addComponent(PositionComponent.class, entity, new PositionComponent(5f, 5f));

        PositionComponent pos = world.getComponent(PositionComponent.class, entity);
        assertEquals(5f, pos.x);
        assertEquals(5f, pos.y);
    }

    @Test
    void destroyEntityRemovesAllComponents() {
        GameWorld world = new GameWorld();
        int entity = world.createEntity();
        world.addComponent(PositionComponent.class, entity, new PositionComponent(0f, 0f));
        world.addComponent(HealthComponent.class, entity, new HealthComponent(100));

        world.destroyEntity(entity);
        assertNull(world.getComponent(PositionComponent.class, entity));
        assertNull(world.getComponent(HealthComponent.class, entity));
    }

    @Test
    void getAllEntitiesWithComponentFiltersByType() {
        GameWorld world = new GameWorld();
        int e1 = world.createEntity();
        int e2 = world.createEntity();
        world.addComponent(PositionComponent.class, e1, new PositionComponent(0f, 0f));
        world.addComponent(HealthComponent.class, e2, new HealthComponent(100));

        assertEquals(1, world.getAllEntitiesWithComponent(PositionComponent.class).size());
        assertEquals(1, world.getAllEntitiesWithComponent(HealthComponent.class).size());
    }
}
