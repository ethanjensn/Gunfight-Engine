package com.gunfight.engine.core;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;

import com.gunfight.engine.ecs.Entity;
import com.gunfight.engine.ecs.World;
import com.gunfight.engine.ecs.components.Transform;
import com.gunfight.engine.ecs.components.Velocity;
import com.gunfight.engine.ecs.systems.MovementSystem;
import com.gunfight.engine.input.Input;
import com.gunfight.engine.render.Render;

/**
 * Core game engine that manages the game loop, window, and rendering.
 * Uses delta time to ensure consistent game logic updates at 60 Hz
 * while allowing rendering to run at the monitor's refresh rate.
 */
public class Engine {

    private boolean running = false;
    // Camera mode: true = follow player, false = free camera
    private boolean followPlayer = true;
    // The game window managed by GLFW
    private Window window;
    // Player entity reference
    private Entity player;
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
    // Mouse position tracking for camera rotation (default center of 800x600 window)
    private double lastX = 400, lastY = 300;
    // Ignores first frame's large jump when mouse enters window
    private boolean firstMouse = true;

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

        // Get player entity
        player = triangle;  // use your first entity as player

        // Main game loop: continues while running and window is open
        while (running && !window.shouldClose()) {
            // Current time in nanoseconds
            long now = System.nanoTime();
            // Add elapsed time to delta (normalized to update cycles)
            delta += (now - lastTime) / nsPerUpdate;
            float deltaTime = (float) ((now - lastTime) / 1_000_000_000.0);
            lastTime = now;

            // Run update() once for each accumulated update cycle
            // This catches up if rendering was slow, or skips if fast
            while (delta >= 1) {
                update();

                // Get player velocity component
                Velocity v = world.getComponent(player, Velocity.class);

                // Reset velocity each frame
                v.x = 0;
                v.y = 0;
                
                if(input.isKeyPressed(GLFW_KEY_A)) {
                    v.x = -1.0f;
                }
                if(input.isKeyPressed(GLFW_KEY_D)) {
                    v.x = 1.0f;
                }
                if(input.isKeyPressed(GLFW_KEY_W)) {
                    v.y = 1.0f;
                }
                if(input.isKeyPressed(GLFW_KEY_S)) {
                    v.y = -1.0f;
                }

                movementSystem.update(world, deltaTime);
                
                // Update camera to follow player AFTER movement
                if (followPlayer) {
                    Transform playerTransform = world.getComponent(player, Transform.class);
                    if (playerTransform != null) {
                        camera.x = playerTransform.x;
                        camera.y = playerTransform.y;
                        camera.z = playerTransform.z + 2.0f;  // Stay 2 units behind player
                    }
                }
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
        // Toggle camera mode with F key
        // If F key is pressed, switch between follow player and free camera modes
        if (input.isKeyPressed(GLFW_KEY_F)) {
            followPlayer = !followPlayer;
        }

        // Free camera control with WASD (only when not following player)
        // If not following player, allow camera movement with WASD keys
        if (!followPlayer) {
            // Move camera left
            // If A key is pressed, decrease camera x position
            if (input.isKeyPressed(GLFW_KEY_A)) {
                camera.x -= 0.02f;
            }
            // Move camera right
            // If D key is pressed, increase camera x position
            if (input.isKeyPressed(GLFW_KEY_D)) {
                camera.x += 0.02f;
                camera.y += 0.02f;
            }
            // Move camera up
            // If W key is pressed, increase camera y position
            if (input.isKeyPressed(GLFW_KEY_W)) {
                camera.dirY += 0.02f;
            }
            // Move camera down
            // If S key is pressed, decrease camera y position
            if (input.isKeyPressed(GLFW_KEY_S)) {
                camera.dirY -= 0.02f;
            }
        }

        // Get mouse position arrays (GLFW requires arrays to return values)
        // Create arrays to store mouse x and y positions
        double[] xpos = new double[1];
        double[] ypos = new double[1]; 

        // Get current cursor position from GLFW
        // Get current mouse position from GLFW and store in xpos and ypos arrays
        glfwGetCursorPos(window.getHandle(), xpos, ypos);

        // Skip first frame to avoid large jump when mouse enters window
        // If this is the first frame, set lastX and lastY to current mouse position
        if (firstMouse) {
            lastX = xpos[0];
            lastY = ypos[0];
            firstMouse = false;
        }

        // Calculate how much mouse moved since last frame
        // Calculate difference between current and last mouse positions
        float xoffset = (float)(xpos[0] - lastX);
        float yoffset = (float)(lastY - ypos[0]); // Reversed since y-coordinates go from bottom to top

        // Store current position for next frame
        // Update lastX and lastY to current mouse position
        lastX = xpos[0];
        lastY = ypos[0];

        // Apply sensitivity to slow down rotation speed
        // Multiply mouse movement by sensitivity to slow down rotation
        float sensitivity = 0.1f;
        xoffset *= sensitivity;
        yoffset *= sensitivity;

        // Update camera rotation angles based on mouse movement
        // Add mouse movement to camera rotation angles
        camera.yaw += xoffset;  // Left/right rotation
        camera.pitch += yoffset;  // Up/down rotation

        // Clamp pitch to prevent camera from flipping upside down
        // Limit pitch angle to prevent camera from flipping
        if (camera.pitch > 89.0f) camera.pitch = 89.0f;
        if (camera.pitch < -89.0f) camera.pitch = -89.0f;

        // Convert angles to radians for trigonometric calculations
        // Convert camera rotation angles to radians
        float yawRad = (float) Math.toRadians(camera.yaw);
        float pitchRad = (float) Math.toRadians(camera.pitch);

        // Calculate camera direction vector from yaw and pitch
        // Calculate camera direction vector using yaw and pitch angles
        camera.dirX = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        camera.dirY = (float) Math.sin(pitchRad);
        camera.dirZ = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
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