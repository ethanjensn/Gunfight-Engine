package com.gunfight.engine;

import java.util.BitSet;

public class EntityManager {
    // The "Master Switchboard" - tracks every ID using a single bit
    private final BitSet activeEntities = new BitSet();
    
    public int createEntity() {
        // nextClearBit(0) is a magic method that scans the bits 
        // starting from index 0 and finds the first "OFF" switch.
        int id = activeEntities.nextClearBit(0);

        // Flip the switch to "ON" (true)
        activeEntities.set(id);

        return id;
    }
    
    // Frees an ID so it can be reused later.
    public void destroyEntity(int id) {
        // Flip the switch back to "OFF" (false)
        activeEntities.clear(id);
    }
    
    // Checks if an ID is currently in use.
    public boolean isActive(int id) {
        // Returns true if the switch is ON, false if OFF
        return activeEntities.get(id);
    }
}
