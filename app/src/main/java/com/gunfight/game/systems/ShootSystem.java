package com.gunfight.game.systems;

import com.gunfight.engine.ecs.*;
import com.gunfight.engine.ecs.EcsSystem;
import com.gunfight.engine.ecs.components.*;
import com.gunfight.game.components.DirectionComponent;
import java.util.ArrayList;
import java.util.List;

public class ShootSystem extends EcsSystem {

    private int tickCounter = 0;

    @Override
    public void update(World world) {
        tickCounter++;

        // shoot every 60 ticks (~1 second)
        if (tickCounter % 60 != 0) return;

        List<Entity> entities = new ArrayList<>(world.getEntities());

        for (Entity entity : entities) {

            Transform t = world.getComponent(entity, Transform.class);
            DirectionComponent d = world.getComponent(entity, DirectionComponent.class);

            if (t != null && d != null) {

                // create projectile
                Entity projectile = world.createEntity();

                Transform pt = new Transform(t.x, t.y);

                Velocity pv = new Velocity();
                pv.x = d.x * 2; // projectile speed
                pv.y = d.y * 2;

                world.addComponent(projectile, pt);
                world.addComponent(projectile, pv);

                System.out.println("SHOT FIRED at x=" + pt.x + " y=" + pt.y);
            }
        }
    }
}