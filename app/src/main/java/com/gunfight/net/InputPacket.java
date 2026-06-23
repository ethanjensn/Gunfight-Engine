package com.gunfight.net;

public class InputPacket {
    public boolean moveUp;
    public boolean moveDown;
    public boolean moveLeft;
    public boolean moveRight;
    public boolean isFiring;
    public boolean isReloading;
    public float cursorX;
    public float cursorY;
    public boolean ready;

    // Internal ID used by the server, not sent by the browser
    private transient int entityId;
    
    // Getters and setters
    public int getEntityId() {
        return this.entityId;
    }
    
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
}
