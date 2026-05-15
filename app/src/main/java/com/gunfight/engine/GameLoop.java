package com.gunfight.engine;

import java.util.Queue;

import com.gunfight.logic.InputSystem;
import com.gunfight.logic.MovementSystem;
import com.gunfight.logic.NetworkBroadcastSystem;
import com.gunfight.logic.WeaponSystem;
import com.gunfight.logic.ProjectileSystem;
import com.gunfight.logic.ReloadSystem;
import com.gunfight.logic.ProjectilePool;
import com.gunfight.net.GameServer;
import com.gunfight.net.InputPacket;

public class GameLoop implements Runnable {
    private boolean running = false;
    private int tickCount = 0;
    private GameWorld world;
    private Queue<InputPacket> inputQueue;
    private InputSystem inputSystem = new InputSystem();
    private MovementSystem movementSystem = new MovementSystem();
    private ProjectilePool projectilePool;
    private WeaponSystem weaponSystem;
    private ProjectileSystem projectileSystem = new ProjectileSystem();
    private ReloadSystem reloadSystem = new ReloadSystem();
    private NetworkBroadcastSystem broadcastSystem;

    // GameWorld — to access entities and components when running systems
    // Queue<InputPacket> — to grab network inputs each tick and apply them
    public GameLoop(GameWorld world, Queue<InputPacket> inputQueue, GameServer server) {
        this.world = world;
        this.inputQueue = inputQueue;
        this.projectilePool = new ProjectilePool(world);
        this.weaponSystem = new WeaponSystem(projectilePool);
        this.broadcastSystem = new NetworkBroadcastSystem(server);
    }

    public void start() {
        running = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double tickspersecond = 60.0;
        double nsPerTick = 1000000000.0 / tickspersecond;
        double delta = 0.0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            
            if (delta >= 1.0) {
                // Update game logic here
                tick();
                delta--;
            }
        }
    }

    private void tick() {
        tickCount++;

        // process network inputs
        inputSystem.processInputs(inputQueue, world);

        // Apply movement
        movementSystem.update(world);

        // Process reload input
        reloadSystem.update(world, tickCount);

        // Process weapon firing
        weaponSystem.update(world, tickCount);

        // Update projectiles (move, check lifetime/bounds)
        projectileSystem.update(world);

        // Broadcast world state to all clients
        broadcastSystem.update(world, tickCount);

        // System.out.println("Tick: " + tickCount);
    }
    
    public int getTickCount() {
        return tickCount;
    }
    
}
