package com.gunfight.logic;

import java.util.Queue;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.InputComponent;
import com.gunfight.net.InputPacket;

public class InputSystem {
    // Process input packets from the queue and update entity states
    public void processInputs(Queue<InputPacket> inputQueue, GameWorld world) {
        InputPacket packet;

        // the poll() method is literally handing you an InputPacket object
        while ((packet = inputQueue.poll()) != null) {
            InputComponent input = world.getComponent(InputComponent.class, packet.getEntityId());
            if (input != null) {
                input.moveUp = packet.moveUp;
                input.moveDown = packet.moveDown;
                input.moveLeft = packet.moveLeft;
                input.moveRight = packet.moveRight;
                input.isFiring = packet.isFiring;
                input.cursorX = packet.cursorX;
                input.cursorY = packet.cursorY;
            }
        }        
    }
}
