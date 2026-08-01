package com.gunfight.engine;

import java.util.Queue;

import com.gunfight.logic.InputSystem;
import com.gunfight.logic.MovementSystem;
import com.gunfight.logic.NetworkBroadcastSystem;
import com.gunfight.logic.WeaponSystem;
import com.gunfight.logic.ProjectileSystem;
import com.gunfight.logic.ReloadSystem;
import com.gunfight.logic.CombatSystem;
import com.gunfight.logic.DeathSystem;
import com.gunfight.logic.RoundSystem;
import com.gunfight.logic.VisionSystem;
import com.gunfight.logic.ProjectilePool;
import com.gunfight.net.GameServer;
import com.gunfight.net.InputPacket;

import java.util.Map;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gunfight.metrics.GameMetrics;
import com.gunfight.metrics.MetricsReporter;

public class GameLoop implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GameLoop.class);
    private boolean running = false;
    private int tickCount = 0;
    private GameWorld world;
    private Queue<InputPacket> inputQueue;
    private InputSystem inputSystem = new InputSystem();
    private MovementSystem movementSystem = new MovementSystem();
    private ProjectilePool projectilePool;
    private WeaponSystem weaponSystem;
    private ProjectileSystem projectileSystem;
    private CombatSystem combatSystem;
    private ReloadSystem reloadSystem = new ReloadSystem();
    private DeathSystem deathSystem = new DeathSystem();
    private RoundSystem roundSystem = new RoundSystem();
    private VisionSystem visionSystem = new VisionSystem();
    private NetworkBroadcastSystem broadcastSystem;
    private GameMetrics metrics = new GameMetrics();
    private MetricsReporter metricsReporter = new MetricsReporter(metrics);

    // GameWorld — to access entities and components when running systems
    // Queue<InputPacket> — to grab network inputs each tick and apply them
    public GameLoop(GameWorld world, Queue<InputPacket> inputQueue, GameServer server, Map<WebSocket, Integer> connectionToEntity) {
        this.world = world;
        this.inputQueue = inputQueue;
        this.projectilePool = new ProjectilePool(world);
        this.weaponSystem = new WeaponSystem(projectilePool);
        this.projectileSystem = new ProjectileSystem(projectilePool);
        this.combatSystem = new CombatSystem(projectilePool);
        this.broadcastSystem = new NetworkBroadcastSystem(server, connectionToEntity, metrics);
    }

    public void start() {
        running = true;
        metricsReporter.start();
        new Thread(this, "GameLoop").start();
    }

    public void stop() {
        running = false;
        metricsReporter.stop();
    }

    public GameMetrics getMetrics() {
        return metrics;
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
        long tickStart = System.nanoTime();

        try {
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

            // Check projectile-vs-player collisions
            combatSystem.update(world);

            // Check for deaths (health <= 0) — strips Input/Weapon, adds RespawnComponent
            deathSystem.update(world);

            // Handle round transitions, scoring, and respawns
            roundSystem.update(world);

            // Compute per-player line-of-sight
            visionSystem.update(world);

            // Broadcast world state to all clients
            long broadcastStart = System.nanoTime();
            broadcastSystem.update(world, tickCount);
            metrics.recordBroadcastNs(System.nanoTime() - broadcastStart);
        } catch (Exception e) {
            log.error("Exception during tick {}, continuing to next tick", tickCount, e);
        }

        metrics.recordTickNs(System.nanoTime() - tickStart);
    }
    
    public int getTickCount() {
        return tickCount;
    }
    
}
