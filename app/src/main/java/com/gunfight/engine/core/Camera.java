package com.gunfight.engine.core;

/**
 * Represents a camera in 3D space.
 * Holds position data for view transformation.
 * The camera is used to transform world coordinates to camera coordinates.
 */
public class Camera {

    public float x, y, z;

    public float dirX = 0;
    public float dirY = 0;
    public float dirZ = -1;

    public float yaw = -90.0f;
    public float pitch = 0.0f;

    /**
     * Creates a new camera at the origin (0, 0, 0).
     */
    public Camera() {
        this.dirX = 0;
        this.dirY = 0;
        this.dirZ = -1;
    }
}
