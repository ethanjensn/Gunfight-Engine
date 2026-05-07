package com.gunfight.engine;

public class EntityManager {
    int counter = 0;
    
    public int createEntity() {
        return counter++;
    }
}
