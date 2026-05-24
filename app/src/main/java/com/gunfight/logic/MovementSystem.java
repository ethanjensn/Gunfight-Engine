package com.gunfight.logic;

import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.InputComponent;
import com.gunfight.data.RoundStateComponent;
import com.gunfight.data.RoundStateComponent.RoundPhase;
import com.gunfight.data.WallComponent;

public class MovementSystem {
    private static final float MOVE_SPEED = 5.0f;
    private static final float CANVAS_WIDTH = 800f;
    private static final float CANVAS_HEIGHT = 600f;
    private static final float PLAYER_SIZE = 32f;

    public void update(GameWorld world) {
        // Only allow movement when a round is actively in progress
        Set<Integer> matchEntities = world.getAllEntitiesWithComponent(RoundStateComponent.class);
        if (matchEntities.isEmpty()) return;
        RoundStateComponent roundState = world.getComponent(RoundStateComponent.class, matchEntities.iterator().next());
        if (roundState == null || roundState.phase != RoundPhase.IN_ROUND) return;

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

                // Clamp to canvas bounds (keep 32x32 square fully visible)
                pos.x = Math.max(0, Math.min(pos.x, CANVAS_WIDTH - PLAYER_SIZE));
                pos.y = Math.max(0, Math.min(pos.y, CANVAS_HEIGHT - PLAYER_SIZE));

                // Push player out of any wall they overlap
                resolveWallCollision(world, pos);

                // Print position after movement
                System.out.println("Entity " + entityId + " position: (" + pos.x + ", " + pos.y + ")");
            }
        }
    }

    private void resolveWallCollision(GameWorld world, PositionComponent pos) {
        Set<Integer> walls = world.getAllEntitiesWithComponent(WallComponent.class);
        for (int wallId : walls) {
            PositionComponent wp = world.getComponent(PositionComponent.class, wallId);
            WallComponent wc = world.getComponent(WallComponent.class, wallId);
            if (wp == null || wc == null) continue;

            float px2 = pos.x + PLAYER_SIZE;
            float py2 = pos.y + PLAYER_SIZE;
            float wx2 = wp.x + wc.width;
            float wy2 = wp.y + wc.height;

            // AABB overlap check
            if (pos.x < wx2 && px2 > wp.x && pos.y < wy2 && py2 > wp.y) {
                // Find smallest overlap axis and push out
                float overlapLeft  = px2 - wp.x;
                float overlapRight = wx2 - pos.x;
                float overlapTop   = py2 - wp.y;
                float overlapBottom = wy2 - pos.y;

                float minX = Math.min(overlapLeft, overlapRight);
                float minY = Math.min(overlapTop, overlapBottom);

                if (minX < minY) {
                    pos.x += (overlapLeft < overlapRight) ? -overlapLeft : overlapRight;
                } else {
                    pos.y += (overlapTop < overlapBottom) ? -overlapTop : overlapBottom;
                }
            }
        }
    }
}
