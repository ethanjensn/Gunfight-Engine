package com.gunfight.logic;

import com.gunfight.data.InputComponent;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.RoundStateComponent;
import com.gunfight.data.RoundStateComponent.RoundPhase;
import com.gunfight.data.StaticMapComponent;
import com.gunfight.engine.GameWorld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementSystemTest {

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

    private static final String[] EMPTY_MAP = new String[10];
    static {
        java.util.Arrays.fill(EMPTY_MAP, "..............");
    }

    private GameWorld createWorldWithRoundState(RoundPhase phase) {
        return createWorldWithRoundState(phase, MAP);
    }

    private GameWorld createWorldWithRoundState(RoundPhase phase, String[] map) {
        GameWorld world = new GameWorld();
        int mapEntity = world.createEntity();
        world.addComponent(StaticMapComponent.class, mapEntity, new StaticMapComponent(map));
        int matchEntity = world.createEntity();
        RoundStateComponent round = new RoundStateComponent();
        round.phase = phase;
        world.addComponent(RoundStateComponent.class, matchEntity, round);
        return world;
    }

    @Test
    void movementOnlyHappensDuringRound() {
        GameWorld world = createWorldWithRoundState(RoundPhase.WAITING);
        int entity = world.createEntity();
        world.addComponent(PositionComponent.class, entity, new PositionComponent(100f, 100f));
        InputComponent input = new InputComponent();
        input.moveRight = true;
        world.addComponent(InputComponent.class, entity, input);

        MovementSystem system = new MovementSystem();
        system.update(world);

        PositionComponent pos = world.getComponent(PositionComponent.class, entity);
        assertEquals(100f, pos.x);
    }

    @Test
    void movesEntityWhenInputIsSet() {
        GameWorld world = createWorldWithRoundState(RoundPhase.IN_ROUND);
        int entity = world.createEntity();
        world.addComponent(PositionComponent.class, entity, new PositionComponent(100f, 100f));
        InputComponent input = new InputComponent();
        input.moveRight = true;
        input.moveDown = true;
        world.addComponent(InputComponent.class, entity, input);

        MovementSystem system = new MovementSystem();
        system.update(world);

        PositionComponent pos = world.getComponent(PositionComponent.class, entity);
        assertEquals(105f, pos.x);
        assertEquals(105f, pos.y);
    }

    @Test
    void clampsEntityToCanvasBounds() {
        GameWorld world = createWorldWithRoundState(RoundPhase.IN_ROUND, EMPTY_MAP);
        int entity = world.createEntity();
        world.addComponent(PositionComponent.class, entity, new PositionComponent(790f, 590f));
        InputComponent input = new InputComponent();
        input.moveRight = true;
        input.moveDown = true;
        world.addComponent(InputComponent.class, entity, input);

        MovementSystem system = new MovementSystem();
        system.update(world);

        PositionComponent pos = world.getComponent(PositionComponent.class, entity);
        assertEquals(768f, pos.x, 0.001f);
        assertEquals(568f, pos.y, 0.001f);
    }
}
