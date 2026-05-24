package com.gunfight.net;

import java.util.List;

public class GameStatePacket {
    public List<PlayerState> players;
    public List<ProjectileState> projectiles;
    public List<WallState> walls;
    public String phase;
    public int roundNumber;

    public GameStatePacket(List<PlayerState> players, List<ProjectileState> projectiles, List<WallState> walls) {
        this.players = players;
        this.projectiles = projectiles;
        this.walls = walls;
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
        public int wins;
        public boolean readyForRematch;

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

    public static class WallState {
        public float x;
        public float y;
        public float w;
        public float h;

        public WallState(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
