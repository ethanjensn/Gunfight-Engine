package com.gunfight.engine.core;

/**
 * Headless game engine that manages the game loop and ECS.
 * Runs independently without rendering or window management.
 * Uses delta time to ensure consistent game logic updates at 60 Hz.
 */
public class Engine {

    private boolean running = false;

    /**
     * Starts the game engine main loop.
     * Initializes the ECS World, then runs the game loop until stopped.
     */
    public void run() {
        running = true; // Set engine to running state

        // Initialize timing variables for fixed timestep
        long lastTime = System.nanoTime(); // Capture current time in nanoseconds
        double nsPerTick = 1_000_000_000.0 / 60.0; // Nanoseconds per tick (60 ticks per second = ~16.67ms)
        double delta = 0; // Accumulator for time passed, normalized to tick units

        int tickCount = 0; // Counter to track total ticks

        // Main game loop - runs until engine is stopped
        while (running) {
            long now = System.nanoTime(); // Get current time
            delta += (now - lastTime) / nsPerTick; // Add elapsed time to delta (converted to tick units)
            lastTime = now; // Update lastTime for next iteration

            // Process one tick for each accumulated tick unit
            // This ensures consistent tick rate even if frame time varies
            while (delta >= 1) {
                update(); // Run game logic for this tick
                tickCount++; // Increment tick counter
                System.out.println("Tick: " + tickCount); // Log tick number
                delta--; // Consume one tick unit from delta
            }
        }
    }

    /**
     * Updates game logic.
     * Called at a fixed rate of 60 times per second.
     */
    private void update() {
        // later: ECS systems will run here
    }

    /**
     * Stops the game engine.
     */
    public void stop() {
        running = false;
    }
}