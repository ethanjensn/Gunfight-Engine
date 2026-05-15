# Projectile Architecture — Decision Log

**TL;DR:** To implement physical projectiles, you'll need a `ProjectileComponent` (data), a `ProjectileSystem` (physics/collision), and a `ProjectilePool` (memory management). These will live in your `data` and `logic` packages alongside your existing systems.

---

## Projectile Architecture (File Structure)

To keep the engine clean and "Zero-Allocation," here is where the new files should go and what they do:

### 1. `com.gunfight.data.ProjectileComponent`
- **Path:** `app/src/main/java/com/gunfight/data/ProjectileComponent.java`
- **Purpose:** Stores the "stats" for a specific bullet in flight.
- **Fields:** `ownerId` (so you don't shoot yourself), `damage`, `speed`, and `lifeTicks` (how many ticks the bullet lives before disappearing).

### 2. `com.gunfight.logic.ProjectilePool`
- **Path:** `app/src/main/java/com/gunfight/logic/ProjectilePool.java`
- **Purpose:** This is your "warehouse." It pre-allocates 100+ entities with `Position`, `Velocity`, and `Projectile` components at startup.
- **Logic:** When the `WeaponSystem` fires, it asks this pool for the next "inactive" bullet.

### 3. `com.gunfight.logic.ProjectileSystem`
- **Path:** `app/src/main/java/com/gunfight/logic/ProjectileSystem.java`
- **Purpose:** The "brain" for bullets.
- **Logic:** Every tick, it moves bullets based on their velocity, checks if they hit a wall or player, and decrements their `lifeTicks`. If they hit something or time out, it marks them as "inactive" so the pool can reuse them.

---

## How the Flow Changes

Your `WeaponSystem` will no longer just print a message. Instead, it will work like this:

1. **`WeaponSystem`:** "Hey `ProjectilePool`, Player 1 just fired. Give me a bullet!"
2. **`ProjectilePool`:** "Here is Bullet #42. I've set its position to Player 1's coordinates."
3. **`ProjectileSystem`:** *(On the next tick)* "Bullet #42 is moving forward... checking for hits... still flying."

---

## Updated File Map

```
com.gunfight
├── data
│   ├── PositionComponent.java
│   ├── VelocityComponent.java   <-- NEW: Physics data
│   ├── ProjectileComponent.java <-- NEW: Bullet data
│   └── WeaponComponent.java
└── logic
    ├── MovementSystem.java
    ├── WeaponSystem.java        <-- UPDATE: Calls the Pool
    ├── ProjectileSystem.java    <-- NEW: Moves/kills bullets
    └── ProjectilePool.java      <-- NEW: Manages bullet memory
```

---

## Why This Is the "Hard" but "Right" Way

By making bullets their own entities, you can give them cool properties later — like bullets that bounce off walls, bullets that slow down over time, or even grenades that use the same physics system.

---

## Why `ProjectilePool` and `ProjectileSystem` Are Two Separate Files

**TL;DR:** It's all about the Single Responsibility Principle (SRP). One file is the Warehouse (manages memory), and the other is the Worker (handles physics). Mixing them makes your code harder to debug and prevents you from reusing the "Warehouse" logic for other things like players or particle effects later.

### 1. The Warehouse vs. The Worker

In a high-performance engine, you want to keep your "Thinking" separate from your "Storage."

- **`ProjectilePool` (Storage):** Its only job is to manage the lifecycle of objects. It doesn't care how a bullet moves or what it hits. It only knows: "Is this object in use? No? Okay, give it to whoever asks."
- **`ProjectileSystem` (Thinking):** Its only job is the math. It iterates over every active bullet and says: "Update X and Y. Check for collision." It shouldn't have to worry about where the bullets come from or how to recycle them.

### 2. Why Combining Them Is a "Trap"

If you merge them into a `ProjectileManager`, you'll likely run into these three headaches:

#### A. The "God Object" Problem
Your file will grow massive. It will have to handle:
- Initializing 100 entities
- Finding inactive entities
- Updating physics
- Handling collisions
- Resetting data

When something breaks, you won't know if it's a memory bug (Pool) or a physics bug (System).

#### B. Lack of Reusability
Later, you're going to want to pool `Explosions` or `MuzzleFlashes`. If your pooling logic is trapped inside `ProjectileSystem`, you have to copy-paste that code for every new system. If `ProjectilePool` is its own thing, you can eventually create a generic `ObjectPool<T>` that handles everything in your engine.

#### C. Testing & Debugging
If your bullets are "ghosting" (staying alive when they should be dead), it's much easier to check a 30-line `ProjectilePool` file to see if the `active` flag is being set correctly than to dig through a 200-line file that also contains trigonometry for bullet trajectories.

### 3. How They "Talk" Without Being Combined

This is the standard ECS flow:

1. `WeaponSystem` calls `ProjectilePool.acquire()` to get a "fresh" bullet.
2. `WeaponSystem` sets the initial position/velocity on that bullet.
3. `ProjectileSystem` runs every tick to move all bullets marked `active`.
4. `ProjectileSystem` calls `ProjectilePool.release(id)` when a bullet hits a wall.

### Responsibility Matrix

| Responsibility       | `ProjectilePool` | `ProjectileSystem` |
|----------------------|:----------------:|:------------------:|
| Allocation           | Yes (at startup) | No                 |
| Movement Math        | No               | Yes                |
| Recycling Logic      | Yes              | No                 |
| Collision Detection  | No               | Yes                |

---
