package com.gunfight;

import com.gunfight.engine.ComponentRegistry;
import com.gunfight.engine.EntityManager;
import com.gunfight.data.HealthComponent;

public class Main {
    
    public String getGreeting() {
        return "Hello from Gunfight Engine";
    }
    
    public static void main(String[] args) {
        System.out.println(new Main().getGreeting());

        System.out.println("Testing ComponentRegistry...");

        ComponentRegistry registry = new ComponentRegistry();
        EntityManager entityManager = new EntityManager();

        int entityId = entityManager.createEntity();
        registry.addComponent(HealthComponent.class, entityId, new HealthComponent(100));

        HealthComponent healthComponent = registry.getComponent(HealthComponent.class, entityId);
        System.out.println("Entity ID: " + entityId);
        System.out.println("Health: " + healthComponent.getHealth());

        System.out.println("ComponentRegistry test completed.");
    }
}
