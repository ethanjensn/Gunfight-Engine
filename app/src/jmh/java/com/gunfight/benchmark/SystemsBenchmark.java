package com.gunfight.benchmark;

import com.gunfight.data.*;
import com.gunfight.data.RoundStateComponent.RoundPhase;
import com.gunfight.engine.GameWorld;
import com.gunfight.logic.CombatSystem;
import com.gunfight.logic.ProjectilePool;
import com.gunfight.logic.VisionSystem;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for combat and vision systems, the two next-hottest paths after movement.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Fork(1)
@State(Scope.Benchmark)
public class SystemsBenchmark {

    private static final String[] MAP = {
        "##############",
        "#............#",
        "#............#",
        "#............#",
        "#............#",
        "#............#",
        "#............#",
        "#............#",
        "#............#",
        "##############"
    };

    @Param({"4", "16", "64"})
    public int entityCount;

    private GameWorld world;
    private CombatSystem combatSystem;
    private VisionSystem visionSystem;
    private ProjectilePool projectilePool;
    private int[] playerEntities;

    @Setup(Level.Trial)
    public void setup() {
        world = new GameWorld();

        int mapEntity = world.createEntity();
        world.addComponent(StaticMapComponent.class, mapEntity, new StaticMapComponent(MAP));

        int matchEntity = world.createEntity();
        RoundStateComponent round = new RoundStateComponent();
        round.phase = RoundPhase.IN_ROUND;
        world.addComponent(RoundStateComponent.class, matchEntity, round);

        projectilePool = new ProjectilePool(world, 256);
        combatSystem = new CombatSystem(projectilePool);
        visionSystem = new VisionSystem();

        playerEntities = new int[entityCount];
        for (int i = 0; i < entityCount; i++) {
            int entity = world.createEntity();
            world.addComponent(PositionComponent.class, entity, new PositionComponent(100f + i * 40f, 100f + i * 30f));
            world.addComponent(HealthComponent.class, entity, new HealthComponent(100));
            world.addComponent(InputComponent.class, entity, new InputComponent());
            world.addComponent(VisionComponent.class, entity, new VisionComponent(600f, 90f));
            playerEntities[i] = entity;
        }
    }

    @Setup(Level.Invocation)
    public void resetPerInvocation() {
        // Reset health so bullets can hit again
        for (int entityId : playerEntities) {
            HealthComponent hp = world.getComponent(HealthComponent.class, entityId);
            if (hp != null) hp.health = 100;
        }

        // Clear any leftover bullets from previous invocation
        for (int entityId : world.getAllEntitiesWithComponent(ProjectileComponent.class)) {
            ProjectileComponent proj = world.getComponent(ProjectileComponent.class, entityId);
            if (proj != null && proj.active) {
                projectilePool.release(entityId);
            }
        }

        // Spawn one bullet aimed at each player for a realistic collision workload
        for (int i = 0; i < entityCount; i++) {
            int target = playerEntities[i];
            int shooter = playerEntities[(i + 1) % entityCount];
            PositionComponent targetPos = world.getComponent(PositionComponent.class, target);
            PositionComponent shooterPos = world.getComponent(PositionComponent.class, shooter);
            float dx = targetPos.x - shooterPos.x;
            float dy = targetPos.y - shooterPos.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float vx = dx / dist * 10f;
            float vy = dy / dist * 10f;
            projectilePool.acquire(shooter, 25, 60, shooterPos.x, shooterPos.y, vx, vy);
        }

        // Reset vision and aim each player at the next one
        for (int i = 0; i < entityCount; i++) {
            int observer = playerEntities[i];
            int target = playerEntities[(i + 1) % entityCount];
            PositionComponent tp = world.getComponent(PositionComponent.class, target);
            InputComponent input = world.getComponent(InputComponent.class, observer);
            input.cursorX = tp.x;
            input.cursorY = tp.y;
            VisionComponent vision = world.getComponent(VisionComponent.class, observer);
            vision.visibleIds.clear();
        }
    }

    @Benchmark
    public void combatSystemTick() {
        combatSystem.update(world);
    }

    @Benchmark
    public void visionSystemTick() {
        visionSystem.update(world);
    }
}
