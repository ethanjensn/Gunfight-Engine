package com.gunfight.net;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputPacketTest {

    private final Gson gson = new Gson();

    @Test
    void serializesAndDeserializesInputPacket() {
        InputPacket original = new InputPacket();
        original.moveUp = true;
        original.moveRight = true;
        original.isFiring = true;
        original.cursorX = 123f;
        original.cursorY = 456f;

        String json = gson.toJson(original);
        InputPacket parsed = gson.fromJson(json, InputPacket.class);

        assertTrue(parsed.moveUp);
        assertTrue(parsed.moveRight);
        assertTrue(parsed.isFiring);
        assertFalse(parsed.isReloading);
        assertEquals(123f, parsed.cursorX);
        assertEquals(456f, parsed.cursorY);
    }

    @Test
    void entityIdIsNotSentToClient() {
        InputPacket packet = new InputPacket();
        packet.setEntityId(42);
        String json = gson.toJson(packet);
        assertFalse(json.contains("42"), "entityId should not be serialized to client JSON");
    }

    @Test
    void entityIdCanBeSetAfterDeserialization() {
        InputPacket parsed = gson.fromJson("{}", InputPacket.class);
        parsed.setEntityId(7);
        assertEquals(7, parsed.getEntityId());
    }
}
