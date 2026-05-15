package com.gunfight.net;

import java.util.List;

public class GameStatePacket {
    public List<PlayerState> players;
    public List<ProjectileState> projectiles;

    public GameStatePacket(List<PlayerState> players, List<ProjectileState> projectiles) {
        this.players = players;
        this.projectiles = projectiles;
    }

    public static class PlayerState {
        public int id;
        public float x;
        public float y;
        public short ammo;
        public short maxAmmo;
        public boolean reloading;
        public float reloadProgress;
        public int health;
        public int maxHealth;
        public boolean dead;

        public PlayerState(int id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    public static class ProjectileState {
        public int id;
        public float x;
        public float y;

        public ProjectileState(int id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }
}
