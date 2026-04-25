package com.gunfight.game.components;

import com.gunfight.engine.ecs.Component;

public class HealthComponent implements Component {
    public int hp;

    public HealthComponent(int hp) {
        this.hp = hp;
    }
}
