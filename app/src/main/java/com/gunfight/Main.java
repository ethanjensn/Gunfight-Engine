package com.gunfight;

import com.gunfight.engine.GameWorld;
import com.gunfight.data.HealthComponent;
import com.gunfight.engine.GameLoop;
import com.gunfight.net.GameServer;

public class Main {
    
    public String getGreeting() {
        return "Hello from Gunfight Engine";
    }
    
    public static void main(String[] args) {
        GameWorld world = new GameWorld();

        int player1 = world.createEntity();

        world.addComponent(HealthComponent.class, player1, new HealthComponent(100));

        System.out.println("Player 1's health and ID: " + world.getComponent(HealthComponent.class, player1).getHealth() + ", " + player1);

        // world.destroyEntity(player1);
        
        // if (world.getComponent(HealthComponent.class, player1) == null) {
        //     System.out.println("Player 1 destroyed - component is null as expected");
        // } else {
        //     System.out.println("Health: " + world.getComponent(HealthComponent.class, player1).getHealth());
        // }

        GameServer server = new GameServer(8080, world);
        server.start();

        GameLoop loop = new GameLoop(world, server.getInputQueue(), server);
        loop.start();

        // try {
        //     Thread.sleep(1000);
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt(); // Restore interrupt flag
        // }

        System.out.println("Ticks: " + loop.getTickCount());
    }
}
