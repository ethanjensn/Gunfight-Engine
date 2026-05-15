package com.gunfight.logic;

import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.VelocityComponent;
import com.gunfight.data.ProjectileComponent;

public class ProjectilePool {
    private static final int DEFAULT_POOL_SIZE = 128;
    private final int poolSize;
    private final int[] entityIds;
    private final int[] freeList;
    private int freeIndex;
    private final GameWorld world;

    public ProjectilePool(GameWorld world) {
        this(world, DEFAULT_POOL_SIZE);
    }

    public ProjectilePool(GameWorld world, int poolSize) {
        this.world = world;
        this.poolSize = poolSize;
        this.entityIds = new int[poolSize];
        this.freeList = new int[poolSize];
        this.freeIndex = poolSize - 1;

        // Pre-allocate all projectile entities
        for (int i = 0; i < poolSize; i++) {
            int entityId = world.createEntity();
            entityIds[i] = entityId;
            
            // Add empty components (will be configured on acquire)
            world.addComponent(PositionComponent.class, entityId, new PositionComponent(0, 0));
            world.addComponent(VelocityComponent.class, entityId, new VelocityComponent(0, 0));
            ProjectileComponent proj = new ProjectileComponent(0, 0, 0);
            proj.poolIndex = i; // Store pool index for O(1) release
            world.addComponent(ProjectileComponent.class, entityId, proj);
            
            // Push index onto free stack
            freeList[i] = i;
        }
    }

    public int acquire(int ownerId, int damage, int lifeTicks, 
                       float x, float y, float vx, float vy) {
        if (freeIndex < 0) {
            return -1; // Pool exhausted
        }
        
        int poolIdx = freeList[freeIndex--];
        int entityId = entityIds[poolIdx];
        
        // Configure position
        PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
        pos.x = x;
        pos.y = y;
        
        // Configure velocity
        VelocityComponent vel = world.getComponent(VelocityComponent.class, entityId);
        vel.vx = vx;
        vel.vy = vy;
        
        // Configure projectile
        ProjectileComponent proj = world.getComponent(ProjectileComponent.class, entityId);
        proj.ownerId = ownerId;
        proj.damage = damage;
        proj.lifeTicks = lifeTicks;
        proj.active = true;
        
        return entityId;
    }

    public void release(int entityId) {
        ProjectileComponent proj = world.getComponent(ProjectileComponent.class, entityId);
        
        // Safety check: Don't release a bullet that's already inactive
        if (proj == null || !proj.active) return;

        proj.active = false;
        
        // Instant O(1) push back onto the stack
        freeList[++freeIndex] = proj.poolIndex;
    }
}
