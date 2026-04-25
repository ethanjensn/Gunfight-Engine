package com.gunfight.game.components;

import com.gunfight.engine.ecs.Component;

public class LifetimeComponent implements Component {
    public int ticksRemaining;

    public LifetimeComponent(int ticks) {
        this.ticksRemaining = ticks;
    }
}
