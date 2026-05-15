package com.gunfight.data;

public class WeaponComponent {
    public int damage;
    public short maxAmmo;
    public short ammo;
    public int cooldownTicks;
    public int lastFireTick;
    public int reloadTicks;       // How many ticks a full reload takes
    public int reloadStartTick;   // Tick when reload began (0 = not reloading)
    public boolean reloading;     // Whether currently in a reload
    
    public WeaponComponent(int damage, short maxAmmo, short ammo, int cooldownTicks, int reloadTicks) {
        this.damage = damage;
        this.maxAmmo = maxAmmo;
        this.ammo = ammo;
        this.cooldownTicks = cooldownTicks;
        this.lastFireTick = 0;
        this.reloadTicks = reloadTicks;
        this.reloadStartTick = 0;
        this.reloading = false;
    }
    
    public int getDamage() { return damage; }
}
