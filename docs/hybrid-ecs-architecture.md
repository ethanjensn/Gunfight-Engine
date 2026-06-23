# Hybrid ECS Architecture

## Overview

Gunfight Engine uses a **hybrid architecture** that combines traditional OOP for lobby/matchmaking with a high-performance ECS (Entity Component System) for live gameplay. This design optimizes for both developer productivity and runtime scalability.

## Architecture Layers

### Layer 1: OOP Lobby (Standard Java)

**Purpose:** Matchmaking, queue management, player sessions

**Key Classes:**
- `LobbyManager` — Manages player connections in pre-game state
- `MatchmakingQueue` — FIFO queues per game mode (1v1, 2v2, 3v3)
- `PlayerSession` — Username, WebSocket connection, queue status

**Characteristics:**
- Mutable state with traditional getters/setters
- Synchronous method calls
- WebSocket message handling
- No gameplay simulation

```java
// OOP style - Lobby
public class MatchmakingQueue {
    private final Queue<PlayerSession> waiting = new ConcurrentLinkedQueue<>();
    
    public void enqueue(PlayerSession player) {
        waiting.add(player);
        broadcastQueuePosition(player);
    }
}
```

### Layer 2: ECS Gameplay (Data-Oriented)

**Purpose:** High-frequency gameplay simulation (60 ticks/second)

**Core Systems:**
| System | Responsibility |
|--------|---------------|
| `MovementSystem` | Player position, wall collision |
| `ProjectileSystem` | Bullet physics, wall hits |
| `CombatSystem` | Damage, health, death |
| `VisionSystem` | Line-of-sight, fog of war |
| `RoundSystem` | Round state, scoring, respawn |
| `NetworkBroadcastSystem` | Game state serialization |

**ECS Structure:**
```java
// ECS style - Gameplay
public class MovementSystem {
    public void update(GameWorld world) {
        Set<Integer> entities = world.getAllEntitiesWithComponent(PositionComponent.class);
        for (int entityId : entities) {
            PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
            InputComponent input = world.getComponent(InputComponent.class, entityId);
            // Process movement...
        }
    }
}
```

## The Boundary: Room Creation

The **OOP Lobby** spawns the **ECS Room** when a match is ready:

```java
// In LobbyManager - OOP world
List<PlayerSession> matchedPlayers = queue.dequeue(2); // 1v1
Room room = Room.create(gameServer, "1v1", 
    matchedPlayers.stream().map(p -> p.getSocket()).toList()
);
```

At this point:
- Players transition from `PlayerSession` (OOP) to entity IDs (ECS)
- WebSocket connections are handed over to the room's `connectionToEntity` map
- The ECS `GameLoop` starts simulating at 60 TPS

## Hybrid Component: StaticMap

The most significant optimization in the hybrid ECS is `StaticMapComponent` — it replaces traditional per-wall entities with a dense data structure.

### Before (Pure ECS)
```
Room with 40 walls:
- Entity #5 + PositionComponent + WallComponent
- Entity #6 + PositionComponent + WallComponent
- ... 40 entities
- Query: Iterate 40 entities × 2 HashMap lookups per wall
- Memory: 40 × object overhead
```

### After (Hybrid)
```
Room with StaticMap:
- Entity #5 + StaticMapComponent
  ├─ solid[14][10] boolean array (140 tiles)
  ├─ p1SpawnX, p1SpawnY (floats)
  └─ p2SpawnX, p2SpawnY (floats)
- Query: Direct array access solid[tx][ty]
- Memory: 1 component + 140 booleans
```

### Static Map Layout (14×10)
```
##############  ← Border walls (always solid)
#......2.....#  ← P2 spawn (top center)
#.##......##.#  ← Cover
#..##....##..#  ← Cover
##....##....##  ← Mid cover
##....##....##  ← Mid cover
#..##....##..#  ← Cover
#.##......##.#  ← Cover
#......1.....#  ← P1 spawn (bottom center)
##############  ← Border walls
```

**Tile Size:** 57×60px (800×600 canvas ÷ 14×10 grid)

## Scaling Characteristics

### Memory Usage
| Players | Rooms | Pure ECS (40 walls/room) | Hybrid ECS |
|---------|-------|-------------------------|------------|
| 2,000 | 1,000 | 40,000+ entities | 1,000 components |
| 6,000 | 1,000 (3v3) | 40,000+ entities | 1,000 components |
| 20,000 | 10,000 | 400,000+ entities | 10,000 components |

### Performance
| Operation | Pure ECS | Hybrid |
|-----------|----------|--------|
| Wall collision | O(n) entity iteration | O(1) tile lookup |
| Wall serialization | O(n) HashMap queries | O(1) array walk |
| Vision raycast | 40 AABB checks | 140 boolean checks |
| Round reset | Destroy/create 40 entities | No-op (static persists) |

## Code Examples

### Projectile → Wall Collision (Hybrid)
```java
// ProjectileSystem.java
private boolean hitsWall(GameWorld world, float bx, float by) {
    Set<Integer> mapEntities = world.getAllEntitiesWithComponent(StaticMapComponent.class);
    StaticMapComponent map = world.getComponent(StaticMapComponent.class, 
        mapEntities.iterator().next());
    
    int tx = (int)(bx / StaticMapComponent.TILE_W);
    int ty = (int)(by / StaticMapComponent.TILE_H);
    
    return map.isWall(tx, ty);  // O(1) array access
}
```

### Player → Wall Collision (Hybrid)
```java
// MovementSystem.java
private void resolveWallCollision(GameWorld world, PositionComponent pos) {
    StaticMapComponent map = /* get singleton */;
    float[] bounds = new float[4];
    
    for (int tx = 0; tx < StaticMapComponent.COLS; tx++) {
        for (int ty = 0; ty < StaticMapComponent.ROWS; ty++) {
            if (!map.solid[tx][ty]) continue;
            
            map.getTileBounds(tx, ty, bounds);  // fills x,y,w,h
            // AABB collision against tile...
        }
    }
}
```

## Key Design Decisions

### 1. Why OOP for Lobby?
- **Complex business logic:** Matchmaking rules are easier in OOP
- **Lower frequency:** Queue updates every few seconds, not 60×/sec
- **Developer productivity:** Standard patterns, easier to debug

### 2. Why ECS for Gameplay?
- **High frequency:** 60 TPS simulation demands cache efficiency
- **Parallel friendly:** Systems can run in parallel on entity batches
- **Deterministic:** Easy to replay/validate for anti-cheat

### 3. Why StaticMap over Wall Entities?
- **Memory density:** 140 booleans vs 40× object overhead
- **Cache locality:** Solid tiles contiguous in memory
- **No GC pressure:** Static allocation, no per-round entity churn

## Future Extensions

The hybrid architecture supports:
- **Multiple maps:** Different `StaticMapComponent` layouts loaded per room
- **Destructible walls:** `solid[][]` can be modified at runtime
- **Dynamic obstacles:** Add traditional entities on top of static base
- **Replay system:** ECS world states serialize cleanly for replay
