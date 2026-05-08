package com.gunfight.net;

public class InputPacket {
    public boolean moveUp;
    public boolean moveDown;
    public boolean moveLeft;
    public boolean moveRight;

    // Internal ID used by the server, not sent by the browser
    private int entityId;
    
    // Getters and setters
    public int getEntityId() {
        return this.entityId;
    }
    
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }
}
