package com.gunfight.logic;

import com.gunfight.data.InputComponent;
import com.gunfight.engine.GameWorld;
import com.gunfight.net.InputPacket;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

class InputSystemTest {

    @Test
    void processInputsAppliesPacketToComponent() {
        GameWorld world = new GameWorld();
        int entity = world.createEntity();
        InputComponent input = new InputComponent();
        world.addComponent(InputComponent.class, entity, input);

        InputPacket packet = new InputPacket();
        packet.setEntityId(entity);
        packet.moveUp = true;
        packet.moveLeft = true;
        packet.isFiring = true;
        packet.cursorX = 100f;
        packet.cursorY = 200f;

        ConcurrentLinkedQueue<InputPacket> queue = new ConcurrentLinkedQueue<>();
        queue.add(packet);

        InputSystem system = new InputSystem();
        system.processInputs(queue, world);

        assertTrue(input.moveUp);
        assertTrue(input.moveLeft);
        assertTrue(input.isFiring);
        assertEquals(100f, input.cursorX);
        assertEquals(200f, input.cursorY);
        assertTrue(queue.isEmpty());
    }

    @Test
    void processInputsIgnoresMissingEntity() {
        GameWorld world = new GameWorld();
        InputPacket packet = new InputPacket();
        packet.setEntityId(999);
        packet.moveUp = true;

        ConcurrentLinkedQueue<InputPacket> queue = new ConcurrentLinkedQueue<>();
        queue.add(packet);

        InputSystem system = new InputSystem();
        assertDoesNotThrow(() -> system.processInputs(queue, world));
        assertTrue(queue.isEmpty());
    }
}
