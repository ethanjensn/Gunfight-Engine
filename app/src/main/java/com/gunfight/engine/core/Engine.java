package com.gunfight.engine.core;

import com.gunfight.engine.render.Render;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * Core game engine that manages the game loop, window, and rendering.
 * Uses delta time to ensure consistent game logic updates at 60 Hz
 * while allowing rendering to run at the monitor's refresh rate.
 */
public class Engine {

    private boolean running = false;
    // The game window managed by GLFW
    private Window window;
    // The renderer for drawing graphics
    private Render renderer;

    /**
     * Starts the game engine main loop.
     * Initializes the window, then runs the game loop until the window is closed.
     */
    public void run() {
        running = true;

        // Create and initialize the game window
        window = new Window();
        window.init();

        // Initialize the renderer (must be after OpenGL context is created)
        renderer = new Render();
        renderer.init();

        // Track the time of the last loop iteration
        long lastTime = System.nanoTime();
        // Nanoseconds per update: 1 second / 60 FPS = ~16.67ms per update
        // This ensures game logic runs at a consistent 60 Hz
        double nsPerUpdate = 1_000_000_000.0 / 60.0;

        // Delta accumulator: tracks how many updates should run based on elapsed time
        double delta = 0;

        // Main game loop: continues while running and window is open
        while (running && !window.shouldClose()) {
            // Current time in nanoseconds
            long now = System.nanoTime();
            // Add elapsed time to delta (normalized to update cycles)
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            // Run update() once for each accumulated update cycle
            // This catches up if rendering was slow, or skips if fast
            while (delta >= 1) {
                update();
                delta--;
            }

            // Render every frame (can run faster than 60 FPS)
            render();

            // Update the window (swap buffers, poll events)
            window.update();
        }

        // Clean up window resources before exiting
        window.cleanup();
    }

    /**
     * Updates game logic (physics, input handling, game state).
     * Called at a fixed rate of 60 times per second.
     */
    private void update() {
        // Game logic updates will go here
        // Examples: move objects, check collisions, process input
    }

    /**
     * Renders the game scene to the screen.
     * Called every frame after all updates are complete.
     */
    private void render() {
        // Clear the color buffer (screen)
        glClear(GL_COLOR_BUFFER_BIT);
        
        renderer.render();
    }
}