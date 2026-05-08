package com.gunfight.net;

import java.util.List;

public class GameStatePacket {
    public List<PlayerState> players;

    public GameStatePacket(List<PlayerState> players) {
        this.players = players;
    }

    public static class PlayerState {
        public int id;
        public float x;
        public float y;

        public PlayerState(int id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }
}
