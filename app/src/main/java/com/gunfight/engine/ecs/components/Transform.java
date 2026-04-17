package com.gunfight.engine.ecs.components;

import com.gunfight.engine.ecs.Component;

/**
 * Transform component representing position in 3D space.
 * This component holds the x, y, z coordinates of an entity.
 * Replaces the old Vector3 usage in gameplay logic.
 */
public class Transform implements Component {
    // Position in 3D space
    public float x, y, z;

    /**
     * Creates a new transform at the specified position.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public Transform(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
