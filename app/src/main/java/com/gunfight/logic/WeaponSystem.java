package com.gunfight.logic;

import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.WeaponComponent;
import com.gunfight.data.InputComponent;
import com.gunfight.data.PositionComponent;

public class WeaponSystem {
    private static final float PROJECTILE_SPEED = 15.0f; // tiles per tick
    private static final int PROJECTILE_LIFETIME = 60;   // ticks (1 second at 60fps)
    
    private final ProjectilePool projectilePool;
    
    public WeaponSystem(ProjectilePool projectilePool) {
        this.projectilePool = projectilePool;
    }
    
    public void update(GameWorld world, int currentTick) {
        Set<Integer> entities = world.getAllEntitiesWithComponent(WeaponComponent.class);
        
        for (int entityId : entities) {
            WeaponComponent weapon = world.getComponent(WeaponComponent.class, entityId);
            InputComponent input = world.getComponent(InputComponent.class, entityId);
            PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
            
            if (weapon != null && input != null && pos != null && input.isFiring) {
                if (canFire(weapon, currentTick)) {
                    weapon.ammo--;
                    weapon.lastFireTick = currentTick;
                    
                    // Calculate velocity toward cursor
                    float dx = input.cursorX - pos.x;
                    float dy = input.cursorY - pos.y;
                    float length = (float)Math.sqrt(dx * dx + dy * dy);
                    
                    float vx = 0, vy = 0;
                    if (length > 0.001f) {
                        vx = (dx / length) * PROJECTILE_SPEED;
                        vy = (dy / length) * PROJECTILE_SPEED;
                    }
                    
                    // Spawn projectile
                    int bulletId = projectilePool.acquire(
                        entityId,                    // ownerId
                        weapon.damage,               // damage from weapon
                        PROJECTILE_LIFETIME,         // lifeTicks
                        pos.x, pos.y,                // spawn position
                        vx, vy                       // calculated velocity
                    );
                    
                    if (bulletId < 0) {
                        System.out.println("Entity " + entityId + " fired but pool exhausted!");
                    } else {
                        System.out.println("Entity " + entityId + " fired! Ammo: " + weapon.ammo + " Bullet: " + bulletId);
                    }
                }
            }
        }
    }
    
    private boolean canFire(WeaponComponent weapon, int currentTick) {
        return weapon.ammo > 0 && 
               currentTick - weapon.lastFireTick >= weapon.cooldownTicks;
    }
}
