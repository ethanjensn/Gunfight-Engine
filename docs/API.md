# WebSocket API

All messages between the HTML client and the Java server are JSON objects sent over a single WebSocket connection.

## Connection

- **WebSocket endpoint:** `ws://<host>:8080`
- **Web client:** `http://<host>:3000`

## Client → Server

### `setUsername`

Sent when the player enters a username.

```json
{
  "type": "setUsername",
  "username": "PlayerOne"
}
```

### `joinQueue`

Sent from the main menu to enter matchmaking.

```json
{
  "type": "joinQueue",
  "gameMode": "1v1"
}
```

Valid `gameMode` values: `1v1`, `2v2`, `3v3`.

### `cancelQueue`

Sent to leave the queue and return to the menu.

```json
{
  "type": "cancelQueue"
}
```

### `gameInput`

Sent every tick while in a match. The server applies this to the entity mapped to the connection.

```json
{
  "type": "gameInput",
  "moveUp": false,
  "moveDown": false,
  "moveLeft": false,
  "moveRight": true,
  "isFiring": true,
  "isReloading": false,
  "cursorX": 400.0,
  "cursorY": 300.0,
  "ready": false
}
```

- `ready` — set to `true` after a match to signal the player wants a rematch.

## Server → Client

### `state`

Sent on connect to tell the client which UI to show.

```json
{
  "type": "state",
  "state": "MENU"
}
```

### `queueStatus`

Sent periodically while the player is in queue.

```json
{
  "type": "queueStatus",
  "position": 1,
  "estimatedSeconds": 5,
  "gameMode": "1v1",
  "queueSize": 1
}
```

### `gameStart`

Sent when a match is found.

```json
{
  "type": "gameStart",
  "gameMode": "1v1",
  "roomId": "Room@1a2b3c4d"
}
```

### `gameState`

The main server-authoritative state packet, broadcast every tick.

```json
{
  "type": "gameState",
  "players": [
    {
      "id": 1,
      "x": 384.0,
      "y": 300.0,
      "username": "PlayerOne",
      "slot": 0,
      "ammo": 10,
      "maxAmmo": 10,
      "reloading": false,
      "reloadProgress": 0.0,
      "health": 100,
      "maxHealth": 100,
      "dead": false,
      "wins": 0,
      "readyForRematch": false
    }
  ],
  "projectiles": [
    {
      "id": 5,
      "x": 400.0,
      "y": 310.0
    }
  ],
  "walls": [
    {
      "x": 0.0,
      "y": 0.0,
      "w": 32.0,
      "h": 32.0
    }
  ],
  "phase": "IN_ROUND",
  "roundNumber": 1,
  "roundWinners": [],
  "matchWinners": []
}
```

### `error`

Sent when the client sends an invalid request.

```json
{
  "type": "error",
  "message": "Set username first"
}
```

## Visibility Rules

- Teammates (same slot parity) are always visible.
- Enemies are only visible if they are inside the observer's `VisionComponent` cone.
