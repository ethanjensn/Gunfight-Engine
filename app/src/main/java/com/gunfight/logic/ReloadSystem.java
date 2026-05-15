package com.gunfight.logic;

import java.util.Set;
import com.gunfight.engine.GameWorld;
import com.gunfight.data.WeaponComponent;
import com.gunfight.data.InputComponent;

public class ReloadSystem {
    public void update(GameWorld world, int currentTick) {
        Set<Integer> entities = world.getAllEntitiesWithComponent(WeaponComponent.class);

        for (int entityId : entities) {
            WeaponComponent weapon = world.getComponent(WeaponComponent.class, entityId);
            InputComponent input = world.getComponent(InputComponent.class, entityId);

            if (weapon == null || input == null) continue;

            // Start reload when R is pressed, not already reloading, and ammo is not full
            if (input.isReloading && !weapon.reloading && weapon.ammo < weapon.maxAmmo) {
                weapon.reloading = true;
                weapon.reloadStartTick = currentTick;
            }

            // Complete reload after reloadTicks have elapsed
            if (weapon.reloading) {
                if (currentTick - weapon.reloadStartTick >= weapon.reloadTicks) {
                    weapon.ammo = weapon.maxAmmo;
                    weapon.reloading = false;
                }
            }
        }
    }
}
