package com.gunfight;

import com.gunfight.engine.ComponentRegistry;
import com.gunfight.engine.EntityManager;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.HealthComponent;

public class Main {
    
    public String getGreeting() {
        return "Hello from Gunfight Engine";
    }
    
    public static void main(String[] args) {
        GameWorld world = new GameWorld();

        int player1 = world.createEntity();

        world.addComponent(HealthComponent.class, player1, new HealthComponent(100));

        System.out.println("Player 1's health and ID: " + world.getComponent(HealthComponent.class, player1).getHealth() + ", " + player1);

        world.destroyEntity(player1);
        
        if (world.getComponent(HealthComponent.class, player1) == null) {
            System.out.println("Player 1 destroyed - component is null as expected");
        } else {
            System.out.println("Health: " + world.getComponent(HealthComponent.class, player1).getHealth());
        }
    }
}
