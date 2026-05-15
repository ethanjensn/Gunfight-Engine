package com.gunfight.data;

public class WeaponComponent {
    public int damage;
    public short maxAmmo;
    public short ammo;
    public int cooldownTicks;
    public int lastFireTick;
    
    public WeaponComponent(int damage, short maxAmmo, short ammo, int cooldownTicks) {
        this.damage = damage;
        this.maxAmmo = maxAmmo;
        this.ammo = ammo;
        this.cooldownTicks = cooldownTicks;
        this.lastFireTick = 0;
    }
    
    public int getDamage() { return damage; }
}
