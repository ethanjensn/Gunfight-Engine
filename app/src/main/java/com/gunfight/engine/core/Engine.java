package com.gunfight.engine.core;

import com.gunfight.engine.ecs.World;
import com.gunfight.engine.ecs.systems.MovementSystem;

/**
 * Headless game engine that manages the game loop and ECS.
 * Runs independently without rendering or window management.
 * Uses delta time to ensure consistent game logic updates at 60 Hz.
 */
public class Engine {

    private boolean running = false;
    // ECS World for managing entities and components
    private World world;
    // Movement system for updating entity positions
    private MovementSystem movementSystem;

    /**
     * Starts the game engine main loop.
     * Initializes the ECS World, then runs the game loop until stopped.
     */
    public void run() {
        running = true;

        // Initialize ECS World
        world = new World();

        // Initialize movement system
        movementSystem = new MovementSystem();

        // Track the time of the last loop iteration
        long lastTime = System.nanoTime();
        // Nanoseconds per update: 1 second / 60 FPS = ~16.67ms per update
        // This ensures game logic runs at a consistent 60 Hz
        double nsPerUpdate = 1_000_000_000.0 / 60.0;

        // Delta accumulator: tracks how many updates should run based on elapsed time
        double delta = 0;

        // Main game loop: continues while running
        while (running) {
            // Current time in nanoseconds
            long now = System.nanoTime();
            // Add elapsed time to delta (normalized to update cycles)
            delta += (now - lastTime) / nsPerUpdate;
            float deltaTime = (float) ((now - lastTime) / 1_000_000_000.0);
            lastTime = now;

            // Run update() once for each accumulated update cycle
            // This catches up if simulation was slow, or skips if fast
            while (delta >= 1) {
                update(deltaTime);
                delta--;
            }
        }
    }

    /**
     * Updates game logic.
     * Called at a fixed rate of 60 times per second.
     */
    private void update(float deltaTime) {
        movementSystem.update(world, deltaTime);
    }

    /**
     * Stops the game engine.
     */
    public void stop() {
        running = false;
    }
}