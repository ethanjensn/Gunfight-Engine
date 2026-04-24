package com.gunfight.engine.ecs.components;

import com.gunfight.engine.ecs.Component;

/**
 * Transform component representing position in 2D space.
 * This component holds the x, y coordinates of an entity.
 * Replaces the old Vector3 usage in gameplay logic.
 */
public class Transform implements Component {
    // Position in 2D space
    public float x, y;

    /**
     * Default constructor - initializes position to (0, 0).
     */
    public Transform() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Creates a new transform at the specified position.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Transform(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
