package com.gunfight.data;

public class ProjectileComponent {
    public int ownerId;    // The ID of the player who fired (prevents self-damage)
    public int damage;     // How much health to subtract on impact
    public int lifeTicks;  // How many ticks until the bullet "fades out"
    public boolean active; // Essential for your ProjectilePool to track state
    public int poolIndex;  // Index in ProjectilePool for O(1) release

    public ProjectileComponent(int ownerId, int damage, int lifeTicks) {
        this.ownerId = ownerId;
        this.damage = damage;
        this.lifeTicks = lifeTicks;
        this.active = false; // Default to inactive when first created in the pool
    }
}
