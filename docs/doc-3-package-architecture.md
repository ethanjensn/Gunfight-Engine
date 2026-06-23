# Package Architecture

## Overview

The codebase follows a classic ECS (Entity Component System) layered architecture. The logic files call data files, and then the engine files mold all logic together. The dependency flow is:

```
data → logic → engine
```

## Packages

### `data/` — Pure state containers
Components like `PositionComponent`, `InputComponent`, and `VelocityComponent` are plain data bags with no logic. Nothing depends on them except logic systems.

### `logic/` — Behavior systems
Each system (e.g. `MovementSystem`, `CombatSystem`) imports from `data` to read/write component state, and imports `GameWorld` from `engine` to query entities. Logic systems depend on both `data` and `engine`.

### `engine/` — The glue layer
`GameLoop` imports every logic system and orchestrates them in order each tick. `GameWorld` wraps `EntityManager` and `ComponentRegistry` to provide a unified API for creating entities, attaching components, and querying them.

### `net/` — Networking
`GameServer` handles incoming client connections and the input queue. `GameStatePacket` and `InputPacket` are the data structures sent over the wire. `NetworkBroadcastSystem` in `logic` reads from `engine` and sends state out via `net`.

## Dependency Notes

- `engine` never imports from `data` directly — it only coordinates behavior via `logic`.
- `logic` systems depend on both `data` (component types) and `engine` (`GameWorld` API).
- `net` is consumed by both `engine` (`GameLoop` takes a `GameServer`) and `logic` (`NetworkBroadcastSystem`).
