package com.gunfight.benchmark;

import com.gunfight.data.InputComponent;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.RoundStateComponent;
import com.gunfight.data.RoundStateComponent.RoundPhase;
import com.gunfight.data.StaticMapComponent;
import com.gunfight.engine.GameWorld;
import com.gunfight.logic.MovementSystem;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for the core movement system.
 *
 * <p>Measures the time it takes to update positions for a fixed number of
 * entities each tick. This is the hottest path in the game loop.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Fork(1)
@State(Scope.Benchmark)
public class GameLoopBenchmark {

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

    private GameWorld world;
    private MovementSystem movementSystem;

    @Param({"4", "16", "64"})
    public int entityCount;

    @Setup(Level.Trial)
    public void setup() {
        world = new GameWorld();
        int mapEntity = world.createEntity();
        world.addComponent(StaticMapComponent.class, mapEntity, new StaticMapComponent(MAP));
        int matchEntity = world.createEntity();
        RoundStateComponent round = new RoundStateComponent();
        round.phase = RoundPhase.IN_ROUND;
        world.addComponent(RoundStateComponent.class, matchEntity, round);

        for (int i = 0; i < entityCount; i++) {
            int entity = world.createEntity();
            world.addComponent(PositionComponent.class, entity, new PositionComponent(100f + i, 100f + i));
            InputComponent input = new InputComponent();
            input.moveRight = true;
            world.addComponent(InputComponent.class, entity, input);
        }

        movementSystem = new MovementSystem();
    }

    @Benchmark
    public void movementSystemTick() {
        movementSystem.update(world);
    }
}
