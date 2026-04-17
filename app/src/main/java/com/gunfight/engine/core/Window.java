package com.gunfight.engine.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL33.*;

/**
 * Manages the game window using GLFW for window creation and OpenGL context.
 * Handles window lifecycle: initialization, event polling, buffer swapping, and cleanup.
 */
public class Window {

    // Native handle to the GLFW window
    private long windowHandle;
    // Window dimensions
    private int width = 800;
    private int height = 600;
    // Window title displayed in the title bar
    private String title = "Gunfight";

    /**
     * Initializes GLFW and creates the window with OpenGL context.
     * Sets up OpenGL 3.3 Core Profile and makes the window visible.
     * @throws RuntimeException if GLFW initialization or window creation fails
     */
    public void init() {
        // Initialize GLFW library
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        // Configure GLFW window hints for OpenGL 3.3 Core Profile
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);  // Hide window initially
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);  // Allow window resizing
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);  // OpenGL major version
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);  // OpenGL minor version
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);  // Use core profile

        // Create the window with specified dimensions and title
        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if (windowHandle == 0) {
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window");
        }

        // Make the window's OpenGL context current for this thread
        glfwMakeContextCurrent(windowHandle);
        // Enable OpenGL context capabilities (required for OpenGL calls)
        GL.createCapabilities();

        // Set the clear color to black (R, G, B, A) - only needs to be done once
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Show the window (now that it's fully configured)
        glfwShowWindow(windowHandle);
    }

    /**
     * Checks if the window has been requested to close (e.g., user clicked X button).
     * @return true if the window should close, false otherwise
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    /**
     * Returns the native GLFW window handle.
     * Used for input handling and other GLFW operations.
     * @return the window handle
     */
    public long getHandle() {
        return windowHandle;
    }

    /**
     * Updates the window by swapping buffers and processing input events.
     * Should be called once per frame after rendering.
     */
    public void update() {
        // Swap the front and back buffers (double buffering)
        // This displays what was just rendered
        glfwSwapBuffers(windowHandle);
        // Process all pending window events (keyboard, mouse, window resize, etc.)
        glfwPollEvents();
    }

    /**
     * Cleans up GLFW resources by destroying the window and terminating GLFW.
     * Should be called when shutting down the engine.
     */
    public void cleanup() {
        // Destroy the window and free its resources
        glfwDestroyWindow(windowHandle);
        // Terminate GLFW and free all allocated resources
        glfwTerminate();
    }
}
