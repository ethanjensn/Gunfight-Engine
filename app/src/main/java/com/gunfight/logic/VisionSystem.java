package com.gunfight.logic;

import java.util.Set;

import com.gunfight.data.InputComponent;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.VisionComponent;
import com.gunfight.data.StaticMapComponent;
import com.gunfight.engine.GameWorld;

public class VisionSystem {

    private static final float RAY_STEP = 4f;

    // Pre-allocated wall AABB cache: [x, y, x+w, y+h] per wall, reused every tick
    private float[] wallCache = new float[0];
    private int wallCount = 0;

    public void update(GameWorld world) {
        // Hoist sets — fetched once per tick, not per observer
        Set<Integer> candidates   = world.getAllEntitiesWithComponent(PositionComponent.class);
        Set<Integer> observers    = world.getAllEntitiesWithComponent(VisionComponent.class);

        // Build flat AABB cache from static map tiles
        Set<Integer> mapEntities = world.getAllEntitiesWithComponent(StaticMapComponent.class);
        wallCount = 0;
        if (!mapEntities.isEmpty()) {
            StaticMapComponent map = world.getComponent(StaticMapComponent.class, mapEntities.iterator().next());
            if (map != null) {
                int solidCount = map.getSolidTileCount();
                int needed = solidCount * 4;
                if (wallCache.length < needed) wallCache = new float[needed + 16];
                
                for (int tx = 0; tx < StaticMapComponent.COLS; tx++) {
                    for (int ty = 0; ty < StaticMapComponent.ROWS; ty++) {
                        if (!map.solid[tx][ty]) continue;
                        float x = tx * StaticMapComponent.TILE_W;
                        float y = ty * StaticMapComponent.TILE_H;
                        wallCache[wallCount++] = x;
                        wallCache[wallCount++] = y;
                        wallCache[wallCount++] = x + StaticMapComponent.TILE_W;
                        wallCache[wallCount++] = y + StaticMapComponent.TILE_H;
                    }
                }
            }
        }

        for (int observerId : observers) {
            VisionComponent vision = world.getComponent(VisionComponent.class,  observerId);
            PositionComponent pos  = world.getComponent(PositionComponent.class, observerId);
            InputComponent input   = world.getComponent(InputComponent.class,    observerId);

            if (vision == null || pos == null || input == null) continue;

            vision.visibleIds.clear();
            vision.visibleIds.add(observerId);

            float ox = pos.x;
            float oy = pos.y;
            float cursorAngle = (float) Math.atan2(input.cursorY - oy, input.cursorX - ox);
            float halfFov     = (float) Math.toRadians(vision.fovDegrees / 2.0);

            for (int candidateId : candidates) {
                if (candidateId == observerId) continue;

                PositionComponent cPos = world.getComponent(PositionComponent.class, candidateId);
                if (cPos == null) continue;

                // Walls are no longer entities — they're part of StaticMapComponent
                // All candidates are players, no need to skip walls

                float dx   = cPos.x - ox;
                float dy   = cPos.y - oy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist > vision.range) continue;

                // Angle check — inside FOV cone?
                float angleDiff = normalizeAngle((float) Math.atan2(dy, dx) - cursorAngle);
                if (Math.abs(angleDiff) > halfFov) continue;

                // Occlusion check using cached wall AABBs (no HashMap lookups)
                if (!isOccluded(ox, oy, cPos.x, cPos.y, dist)) {
                    vision.visibleIds.add(candidateId);
                }
            }
        }
    }

    /**
     * Returns true if the segment (ox,oy)->(tx,ty) is blocked by any cached wall AABB.
     * Uses the flat wallCache array — zero HashMap overhead.
     */
    private boolean isOccluded(float ox, float oy, float tx, float ty, float dist) {
        if (dist < 0.001f) return false;
        float invDist = 1f / dist;
        float stepX = (tx - ox) * invDist * RAY_STEP;
        float stepY = (ty - oy) * invDist * RAY_STEP;
        int steps = (int) (dist / RAY_STEP);

        for (int s = 1; s < steps; s++) {
            float rx = ox + stepX * s;
            float ry = oy + stepY * s;
            for (int i = 0; i < wallCount; i += 4) {
                if (rx >= wallCache[i]     && rx <= wallCache[i + 2] &&
                    ry >= wallCache[i + 1] && ry <= wallCache[i + 3]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Normalizes an angle to the range (-PI, PI].
     */
    private float normalizeAngle(float a) {
        while (a >  Math.PI) a -= (float)(2 * Math.PI);
        while (a < -Math.PI) a += (float)(2 * Math.PI);
        return a;
    }
}
