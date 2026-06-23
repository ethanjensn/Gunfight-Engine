package com.gunfight.net;

import com.google.gson.Gson;
import com.gunfight.net.GameStatePacket.PlayerState;
import com.gunfight.net.GameStatePacket.ProjectileState;
import com.gunfight.net.GameStatePacket.WallState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStatePacketTest {

    private final Gson gson = new Gson();

    @Test
    void serializesGameStatePacket() {
        List<PlayerState> players = new ArrayList<>();
        PlayerState player = new PlayerState(0, 100f, 200f);
        player.username = "Test";
        player.health = 80;
        player.ammo = 5;
        players.add(player);

        List<ProjectileState> projectiles = new ArrayList<>();
        projectiles.add(new ProjectileState(1, 50f, 75f));

        List<WallState> walls = new ArrayList<>();
        walls.add(new WallState(0f, 0f, 32f, 32f));

        GameStatePacket packet = new GameStatePacket(players, projectiles, walls);
        packet.phase = "IN_ROUND";
        packet.roundNumber = 1;

        String json = gson.toJson(packet);
        GameStatePacket parsed = gson.fromJson(json, GameStatePacket.class);

        assertEquals("IN_ROUND", parsed.phase);
        assertEquals(1, parsed.roundNumber);
        assertEquals(1, parsed.players.size());
        assertEquals("Test", parsed.players.get(0).username);
        assertEquals(1, parsed.projectiles.size());
        assertEquals(1, parsed.walls.size());
    }

    @Test
    void playerStateDefaultsAreApplied() {
        PlayerState state = new PlayerState(0, 10f, 20f);
        assertEquals(0, state.id);
        assertEquals(10f, state.x);
        assertEquals(20f, state.y);
        assertFalse(state.dead);
    }
}
