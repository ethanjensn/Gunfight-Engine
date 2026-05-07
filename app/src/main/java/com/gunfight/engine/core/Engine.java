package com.gunfight.engine.core;

import com.gunfight.engine.ecs.Entity;
import com.gunfight.engine.ecs.World;
import com.gunfight.engine.ecs.systems.MovementSystem;
import com.gunfight.game.components.DirectionComponent;
import com.gunfight.game.components.HealthComponent;
import com.gunfight.game.systems.ShootSystem;
import com.gunfight.game.systems.LifetimeSystem;
import com.gunfight.game.systems.CollisionSystem;
import com.gunfight.engine.ecs.components.Transform;
import com.gunfight.engine.ecs.components.Velocity;

/**
 * Headless game engine that manages the game loop and ECS.
 * Runs independently without rendering or window management.
 * Uses delta time to ensure consistent game logic updates at 60 Hz.
 */
public class Engine {

    private World world;
    private boolean running = false;

    /**
     * Starts the game engine main loop.
     * Initializes the ECS World, then runs the game loop until stopped.
     */
    public void run() {
        running = true; // Set engine to running state

        // Initialize timing variables for fixed timestep
        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / 60.0; // Nanoseconds per tick (60 ticks per second = ~16.67ms)
        double delta = 0; // Accumulator for time passed

        int tickCount = 0;


        world = new World();
        world.addSystem(new MovementSystem());
        
        Entity player = world.createEntity();

        // Position
        Transform transform = new Transform();
        transform.x = 0;
        transform.y = 0;

        // Velocity (stationary)
        Velocity velocity = new Velocity();
        velocity.x = 0;
        velocity.y = 0;

        world.addComponent(player, transform);
        world.addComponent(player, velocity);

        // Direction (shooting right)
        DirectionComponent dir = new DirectionComponent(1, 0); // shooting right
        world.addComponent(player, dir);

        // Health
        HealthComponent hp = new HealthComponent(100);
        world.addComponent(player, hp);

        // Player 2
        Entity player2 = world.createEntity();

        Transform t2 = new Transform(50, 0);

        Velocity v2 = new Velocity(0, 0); // stationary for now

        DirectionComponent d2 = new DirectionComponent(-1, 0); // shoots LEFT

        HealthComponent hp2 = new HealthComponent(100);

        world.addComponent(player2, t2);
        world.addComponent(player2, v2);
        world.addComponent(player2, d2);
        world.addComponent(player2, hp2);

        // Systems
        world.addSystem(new ShootSystem());
        world.addSystem(new LifetimeSystem());
        world.addSystem(new CollisionSystem());


        // Main game loop
        while (running) {
            long now = System.nanoTime(); // Get current time
            delta += (now - lastTime) / nsPerTick; // Add elapsed time to delta (converted to tick units)
            lastTime = now; // Update lastTime for next iteration

            // Process one tick for each accumulated tick unit
            // This ensures consistent tick rate even if frame time varies
            while (delta >= 1) {
                update(); 
                tickCount++; 
                System.out.println("Tick: " + tickCount);
                
                Transform t = world.getComponent(player, Transform.class);
                System.out.println("Player Position: x=" + t.x + " y=" + t.y);
                
                delta--; // Consume one tick unit from delta
            }
        }
    }

    /**
     * Updates game logic.
     * Called at a fixed rate of 60 times per second.
     */
    private void update() {
        world.update();
    }

    /**
     * Stops the game engine.
     */
    public void stop() {
        running = false;
    }
}