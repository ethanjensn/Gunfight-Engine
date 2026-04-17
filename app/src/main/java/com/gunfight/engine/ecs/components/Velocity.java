package com.gunfight.engine.ecs.components;

import com.gunfight.engine.ecs.Component;

/**
 * Velocity component representing movement speed in 3D space.
 * Defines how fast an entity moves along each axis.
 */
public class Velocity implements Component {
    // Velocity along x, y, z axes
    public float x, y, z;

    /**
     * Creates a new velocity with the specified components.
     * @param x velocity along x-axis
     * @param y velocity along y-axis
     * @param z velocity along z-axis
     */
    public Velocity(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
