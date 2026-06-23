# Architecture Overview

Gunfight Engine is a server-authoritative, real-time multiplayer shooter. The server simulates the game at a fixed 60 ticks per second and broadcasts the resulting state to connected clients.

## High-Level Flow

```
┌─────────────┐      WebSocket       ┌──────────────┐
│ HTML Client │  <──────────────────>│  GameServer  │
│  (Canvas)   │      JSON packets      │  Java 21     │
└─────────────┘                        └──────┬───────┘
                                              │
                                              ▼
                                        ┌─────────────┐
                                        │ LobbyManager │
                                        │  (queues)   │
                                        └──────┬──────┘
                                               │
                                               ▼
                                        ┌─────────────┐
                                        │    Room     │
                                        │  (per game) │
                                        └──────┬──────┘
                                               │
                                               ▼
                                        ┌─────────────┐
                                        │  GameLoop   │
                                        │ 60 ticks/s  │
                                        └──────┬──────┘
                                               │
                              ┌────────────────┼────────────────┐
                              ▼                ▼                ▼
                       ┌──────────┐    ┌──────────┐    ┌─────────────┐
                       │  Input   │    │ Movement │    │  Weapon   │
                       │  System  │    │  System  │    │  System   │
                       └──────────┘    └──────────┘    └─────────────┘
                              │                │                │
                              ▼                ▼                ▼
                       ┌──────────┐    ┌──────────┐    ┌─────────────┐
                       │Combat/   │    │  Vision  │    │  Network   │
                       │Death     │    │  System  │    │ Broadcast │
                       │System    │    │          │    │  System   │
                       └──────────┘    └──────────┘    └─────────────┘
```

## Lobby Layer

The lobby layer is a traditional OOP state machine. It manages player connections, usernames, queues, and matchmaking.

- **LobbyManager** — entry point for connection, disconnection, and message routing.
- **QueueManager** — FIFO queues per game mode (1v1, 2v2, 3v3).
- **Matchmaker** — scheduled task that polls queues and creates rooms.
- **RoomManager** — owns active `Room` instances and cleans them up when empty.

## ECS Layer

Each `Room` owns a `GameWorld`, a `GameLoop`, and a WebSocket-to-entity mapping. The game loop is the heartbeat of the room.

- **GameWorld** — facade over `EntityManager` and `ComponentRegistry`.
- **EntityManager** — bitset-based allocation of entity IDs.
- **ComponentRegistry** — map-of-maps storage keyed by component class and entity ID.
- **GameLoop** — runs all logic systems in a fixed order, measures tick/broadcast time, and logs metrics.

### System Order

1. `InputSystem` — applies queued `InputPacket`s to `InputComponent`s.
2. `MovementSystem` — moves players, clamps to bounds, resolves wall collisions.
3. `ReloadSystem` — handles weapon reload timing.
4. `WeaponSystem` — fires projectiles when input is set and cooldown allows.
5. `ProjectileSystem` — moves bullets and checks bounds/lifetime.
6. `CombatSystem` — resolves projectile-vs-player collisions.
7. `DeathSystem` — processes health <= 0 and prepares respawn.
8. `RoundSystem` — advances round phases, scoring, and respawns.
9. `VisionSystem` — computes per-player line-of-sight.
10. `NetworkBroadcastSystem` — serializes visible state and sends to each client.

## Networking

- **GameServer** — `WebSocketServer` implementation; routes all messages to `LobbyManager`.
- **InputPacket** — client input (movement, fire, reload, cursor).
- **GameStatePacket** — server-authoritative world state sent to clients.
- **NetworkBroadcastSystem** — filters player visibility (teammates always visible, enemies only when in line-of-sight) and broadcasts JSON state.

## Metrics

`GameMetrics` and `MetricsReporter` record per-tick and broadcast timings in a lock-free way. They report averages and maximums every 60 seconds to the log, making it easy to spot tick-rate problems or expensive broadcast frames.

## Error Handling

Critical paths in the game loop are wrapped in try/catch so a single tick exception cannot crash the room. The server also registers a JVM shutdown hook to close the WebSocket server and stop game loops cleanly.
