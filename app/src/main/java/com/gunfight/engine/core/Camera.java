package com.gunfight.engine.core;

/**
 * Represents a camera in 3D space.
 * Holds position data for view transformation.
 * The camera is used to transform world coordinates to camera coordinates.
 */
public class Camera {

    // Camera position in 3D space
    public float x, y, z;

    /**
     * Creates a new camera at the origin (0, 0, 0).
     */
    public Camera() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
}
