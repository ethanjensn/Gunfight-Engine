package com.gunfight.engine.ecs.components;

import com.gunfight.engine.ecs.Component;

/**
 * Velocity component representing movement speed in 2D space.
 * Defines how fast an entity moves along each axis.
 */
public class Velocity implements Component {
    // Velocity along x, y axes
    public float x, y;

    /**
     * Default constructor - initializes velocity to (0, 0).
     */
    public Velocity() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Creates a new velocity with the specified components.
     * @param x velocity along x-axis
     * @param y velocity along y-axis
     */
    public Velocity(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
