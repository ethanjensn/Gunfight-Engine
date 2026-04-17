package com.gunfight.engine.core;

import com.gunfight.engine.ecs.Entity;
import com.gunfight.engine.ecs.World;
import com.gunfight.engine.ecs.components.Transform;
import com.gunfight.engine.ecs.components.Velocity;
import com.gunfight.engine.ecs.systems.MovementSystem;
import com.gunfight.engine.input.Input;
import com.gunfight.engine.render.Render;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.*;
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
    // Input system for keyboard handling (abstracts GLFW)
    private Input input;
    // Camera for view transformation (holds camera position)
    private Camera camera;
    // ECS World for managing entities and components
    private World world;
    // Movement system for updating entity positions
    private MovementSystem movementSystem;

    /**
     * Starts the game engine main loop.
     * Initializes the window, then runs the game loop until the window is closed.
     */
    public void run() {
        running = true;

        // Create and initialize the game window
        window = new Window();
        window.init();

        // Initialize input system
        input = new Input(window.getHandle());

        // Initialize camera
        camera = new Camera();

        // Initialize ECS World
        world = new World();

        // Create triangle entity with Transform component
        Entity triangle = new Entity();
        world.addEntity(triangle);
        world.addComponent(triangle, new Transform(0, 0, -2.0f));
        world.addComponent(triangle, new Velocity(0.005f, 0, 0));

        // Create second triangle entity
        Entity triangle2 = new Entity();
        world.addEntity(triangle2);
        world.addComponent(triangle2, new Transform(0.5f, 0, -2.0f));
        world.addComponent(triangle2, new Velocity(-0.003f, 0, 0));

        // Initialize the renderer (must be after OpenGL context is created)
        renderer = new Render();
        renderer.init();

        // Initialize movement system
        movementSystem = new MovementSystem();

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
                movementSystem.update(world);
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

        // Camera control with WASD
        if (input.isKeyPressed(GLFW_KEY_A)) {
            camera.x -= 0.02f;
        }
        if (input.isKeyPressed(GLFW_KEY_D)) {
            camera.x += 0.02f;
        }
        if (input.isKeyPressed(GLFW_KEY_W)) {
            camera.y += 0.02f;
        }
        if (input.isKeyPressed(GLFW_KEY_S)) {
            camera.y -= 0.02f;
        }
    }

    /**
     * Renders the game scene to the screen.
     * Called every frame after all updates are complete.
     */
    private void render() {
        // Clear the color buffer (screen)
        glClear(GL_COLOR_BUFFER_BIT);

        renderer.render(world, camera);
    }
}