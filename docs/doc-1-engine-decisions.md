# Gunfight Engine: Technical Architecture Documentation

This document summarizes the architectural evolution and technical decisions made for the Gunfight Engine, a Java-based 2v2 tactical shooter simulation.

---

## 1. Core Architecture: Entity Component System (ECS)

The engine utilizes a strict ECS pattern to decouple data from logic, allowing for high performance and scalability.

- **Entities**: Represented as unique `Integer` IDs.
- **Components**: Pure data classes (e.g., `PositionComponent`) that store the state of an entity.
- **Systems**: Logic-only classes (e.g., `MovementSystem`, `NetworkBroadcastSystem`) that query the `GameWorld` for specific component combinations.

### Core Engine Files

| File | Location | Purpose |
|------|----------|---------|
| `Main.java` | `com.gunfight` | Entry point. Initializes `GameWorld`, creates a test player with `HealthComponent(100)`, starts `GameServer` on port 8080, and launches `GameLoop`. |
| `EntityManager.java` | `com.gunfight.engine` | Manages entity ID lifecycle using `BitSet` as a "Master Switchboard." Uses `nextClearBit(0)` to find the first available ID, `set(id)` to activate, and `clear(id)` to free for reuse. |
| `ComponentRegistry.java` | `com.gunfight.engine` | The "Excel Sheet" storage. Uses `Map<Class<?>, Map<Integer, Object>>` where outer key = component type, inner key = entity ID, value = component data. Enables fast lookups and batch queries. |
| `GameWorld.java` | `com.gunfight.engine` | Facade that orchestrates `EntityManager` and `ComponentRegistry`. Provides high-level API: `createEntity()`, `addComponent()`, `getComponent()`, `destroyEntity()`, `getAllEntitiesWithComponent()`. |

### Key Implementation: `Set<Integer>` Queries

To ensure entities are processed uniquely and efficiently, the `GameWorld` utilizes `Set<Integer>` as the return type for component queries. This prevents duplicate processing—a critical safety measure for systems like `MovementSystem` to prevent entities from being moved multiple times per tick.

---

## 2. Networking & State Synchronization

The engine uses a **Server-Authoritative** model where the server broadcasts the state of all relevant entities to clients via JSON over WebSockets.

### Data Transfer Objects (DTOs)

| File | Location | Purpose |
|------|----------|---------|
| `GameStatePacket.java` | `com.gunfight.net` | Wrapper containing `List<PlayerState>`. Gson serializes this into a clean JSON array `{"players":[{"id":1,"x":100.0,"y":200.0},...]}`. |
| `PlayerState` (nested) | `com.gunfight.net` | Lightweight struct with `int id`, `float x`, `float y`. No methods—pure data for zero-overhead serialization. |
| `InputPacket.java` | `com.gunfight.net` | Client-to-server message format with `moveUp`, `moveDown`, `moveLeft`, `moveRight` booleans. Has private `entityId` (set server-side via `setEntityId()` after JSON parsing). |

### Why `ArrayList` for Broadcasting?

We chose `ArrayList` over `HashMap` for the network payload because:

1. **JSON Format**: It serializes into a clean `[]` array, which is natively easier for the JavaScript/Web client to iterate over.
2. **Sequential Access**: Broadcasting requires looping through every entity, making the $O(1)$ addition/iteration of an `ArrayList` superior to the overhead of a `Map`.

---

## 3. Memory Optimization: The "Zero-Allocation" Strategy

To prevent Garbage Collection (GC) stutters (micro-lags caused by the computer cleaning up temporary objects), the engine employs professional-grade memory management.

### Object Pooling (The Two-List Pattern)

Instead of creating new objects every frame, the `NetworkBroadcastSystem` utilizes two distinct lists to manage memory:

| List | Type | Purpose |
|------|------|---------|
| `playerPool` | `List<PlayerState>` | **Storage** — permanent list that keeps objects alive. Expands to "High Water Mark" of max players, never shrinks. |
| `activeStates` | `List<PlayerState>` | **Manifest** — temporary list cleared every tick (~16ms). Holds references (pointers) to objects in `playerPool`. |
| `packet` | `GameStatePacket` | **Reuse container** — same object every tick. Gson reads from `activeStates` which now points to recycled objects. |

### The Recycling Workflow (in `NetworkBroadcastSystem.update()`)

```java
activeStates.clear();  // Step 1: Reset manifest (objects stay in playerPool)

for (int entityId : entities) {
    if (index >= playerPool.size()) {
        playerPool.add(new PlayerState(0, 0, 0));  // Step 2: Expand pool if needed
    }
    
    PlayerState state = playerPool.get(index);  // Step 3: Borrow from pool
    state.id = entityId;  // In-place mutation
    state.x = pos.x;
    state.y = pos.y;
    
    activeStates.add(state);  // Step 4: Add to broadcast list
    index++;
}

String json = gson.toJson(packet);  // Gson serializes activeStates
server.broadcast(json);
```

This eliminates garbage collection from the hot path.

---

## 4. Systems & Components

### Component Files (Pure Data)

| File | Package | Fields | Purpose |
|------|---------|--------|---------|
| `PositionComponent.java` | `com.gunfight.data` | `float x, y` | 2D world position for rendering and movement. |
| `HealthComponent.java` | `com.gunfight.data` | `int health` | Hit points. Initialized to 100 in `GameServer.onOpen()`. |
| `InputComponent.java` | `com.gunfight.data` | `boolean moveUp, moveDown, moveLeft, moveRight` | Current input state, updated every tick from network packets. |

### System Files (Logic Only)

| File | Package | Key Details |
|------|---------|-------------|
| `InputSystem.java` | `com.gunfight.logic` | `processInputs(Queue<InputPacket>, GameWorld)` — drains the concurrent queue via `poll()`, maps packets to `InputComponent` updates using `packet.getEntityId()`. |
| `MovementSystem.java` | `com.gunfight.logic` | `update(GameWorld)` — queries entities with `PositionComponent`, applies `MOVE_SPEED = 5.0f` if corresponding input flags are set, clamps to `CANVAS_WIDTH/HEIGHT` bounds (800x600) keeping `PLAYER_SIZE = 32f` fully visible. |
| `NetworkBroadcastSystem.java` | `com.gunfight.logic` | `update(GameWorld)` — performs the object pooling workflow above, serializes via Gson, calls `server.broadcast()`. |

## 5. Execution Order (The Game Loop)

The `GameLoop.java` (`com.gunfight.engine`) runs at 60 TPS using a variable time-step accumulator:

```java
// Fixed time step: 60 ticks per second
double nsPerTick = 1_000_000_000.0 / 60.0;

while (running) {
    delta += (now - lastTime) / nsPerTick;
    if (delta >= 1.0) {
        tick();  // Run game logic
        delta--;
    }
}
```

### Tick Sequence (`tick()` method)

| Order | Phase | System Call | Description |
|-------|-------|-------------|-------------|
| 1 | **Input Processing** | `inputSystem.processInputs(inputQueue, world)` | Drains `ConcurrentLinkedQueue<InputPacket>` from `GameServer`, updates `InputComponent`s. |
| 2 | **Logic/Movement** | `movementSystem.update(world)` | Calculates new positions, applies canvas bounds clamping. |
| 3 | **Network Broadcast** | `broadcastSystem.update(world)` | Object pooling + JSON serialization + WebSocket broadcast. |

### WebSocket Lifecycle (`GameServer.java`)

| Event | Handler | Actions |
|-------|---------|---------|
| `onOpen` | New connection | Creates entity, adds `HealthComponent(100)`, `InputComponent`, `PositionComponent(0,0)`, maps `WebSocket` → `entityId` in `ConcurrentHashMap`. |
| `onMessage` | JSON received | Parses via `Gson.fromJson()`, sets entity ID, adds to `inputQueue` (thread-safe mailbox). |
| `onClose` | Disconnect | Looks up entity ID, calls `world.destroyEntity()` to clean up components. |
| `broadcast` | Per-tick | Iterates `connectionToEntity.keySet()`, sends JSON to all clients. |

---

## 6. File Structure Summary

```
com.gunfight/
├── Main.java                    # Entry point, wiring
├── engine/
│   ├── EntityManager.java       # BitSet ID management
│   ├── ComponentRegistry.java   # Map-based component storage
│   ├── GameWorld.java           # Facade API
│   └── GameLoop.java            # 60 TPS timing + tick orchestration
├── data/                        # Pure data components
│   ├── PositionComponent.java   # x, y floats
│   ├── HealthComponent.java     # int health
│   └── InputComponent.java      # movement booleans
├── logic/                       # Systems (logic only)
│   ├── InputSystem.java         # Queue → InputComponent
│   ├── MovementSystem.java      # Input + Position → new Position
│   └── NetworkBroadcastSystem.java  # Object pooling + Gson + broadcast
└── net/                         # Networking layer
    ├── GameServer.java          # WebSocketServer, connection management
    ├── GameStatePacket.java     # DTO for world state
    └── InputPacket.java         # DTO for client input
```

## 7. Next Development Milestones

- [ ] **Client-Side Interpolation**: Handling the rendering of the JSON data in the browser.
- [ ] **Bullet Pooling**: Implementing the "Zero-Allocation" strategy for high-frequency projectile spawning.
- [ ] **Collision Callbacks**: Efficiently handling entity deletion without breaking the current system iteration.