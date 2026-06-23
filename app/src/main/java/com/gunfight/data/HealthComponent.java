package com.gunfight.data;

public class HealthComponent {
    public int health;
    public int maxHealth;
    
    public HealthComponent(int health) {
        this.health = health;
        this.maxHealth = health;
    }
    
    public int getHealth() {
        return health;
    }
}
