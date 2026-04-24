package com.gunfight.game.components;

import com.gunfight.engine.ecs.Component;

public class DirectionComponent implements Component {
    public float x;
    public float y;
    
    public DirectionComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
