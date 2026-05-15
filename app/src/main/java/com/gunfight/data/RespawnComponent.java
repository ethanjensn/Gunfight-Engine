package com.gunfight.data;

public class RespawnComponent {
    public int respawnTimer;
    public float spawnX;
    public float spawnY;
    public boolean isPending;

    public RespawnComponent(int respawnTimer, float spawnX, float spawnY) {
        this.respawnTimer = respawnTimer;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.isPending = true;
    }
}
