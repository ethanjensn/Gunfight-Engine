package com.gunfight.logic;

import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.InputComponent; 

public class MovementSystem {
    private static final float MOVE_SPEED = 5.0f;

    public void update(GameWorld world) {
        // Get all entities with Position components
        Set<Integer> entities = world.getAllEntitiesWithComponent(PositionComponent.class);
        
        for (int entityId : entities) {
            PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
            InputComponent input = world.getComponent(InputComponent.class, entityId);
            
            if (pos != null && input != null) {
                // Apply movement based on input
                if (input.moveUp) pos.y -= MOVE_SPEED;
                if (input.moveDown) pos.y += MOVE_SPEED;
                if (input.moveLeft) pos.x -= MOVE_SPEED;
                if (input.moveRight) pos.x += MOVE_SPEED;
                
                // Print position after movement
                System.out.println("Entity " + entityId + " position: (" + pos.x + ", " + pos.y + ")");
            }
        }
    }
}
