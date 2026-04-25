package com.gunfight.game.systems;

import com.gunfight.engine.ecs.*;
import com.gunfight.game.components.LifetimeComponent;

import java.util.ArrayList;
import java.util.List;

public class LifetimeSystem extends EcsSystem {

    @Override
    public void update(World world) {

        List<Entity> toRemove = new ArrayList<>();

        for (Entity entity : new ArrayList<>(world.getEntities())) {

            LifetimeComponent life = world.getComponent(entity, LifetimeComponent.class);

            if (life != null) {
                life.ticksRemaining--;

                if (life.ticksRemaining <= 0) {
                    toRemove.add(entity);
                }
            }
        }

        // remove after loop
        for (Entity entity : toRemove) {
            world.removeEntity(entity);
            System.out.println("Entity removed");
        }
    }
}
