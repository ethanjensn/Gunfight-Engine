package com.gunfight.engine.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Handles keyboard input polling using GLFW.
 * Provides a clean abstraction over GLFW key state queries.
 */
public class Input {

    // Native handle to the GLFW window for input queries
    private long windowHandle;

    /**
     * Initializes the input system with a GLFW window handle.
     * @param windowHandle the native GLFW window handle
     */
    public Input(long windowHandle) {
        this.windowHandle = windowHandle;
    }

    /**
     * Checks if a specific key is currently pressed.
     * @param key the GLFW key code (e.g., GLFW_KEY_A, GLFW_KEY_W)
     * @return true if the key is currently pressed, false otherwise
     */
    public boolean isKeyPressed(int key) {
        return glfwGetKey(windowHandle, key) == GLFW_PRESS;
    }
}
