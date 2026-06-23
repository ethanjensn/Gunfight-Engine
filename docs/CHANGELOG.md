# Changelog

## 2026-06-22 — Resume Readiness Update

### Added
- **Unit tests** with JUnit 5 and Mockito for ECS, movement, input, packets, and metrics.
- **Production logging** using SLF4J + Logback, replacing `System.out.println` across the server.
- **Runtime metrics** (`GameMetrics` + `MetricsReporter`) that report average/max tick and broadcast times.
- **JMH benchmark** (`GameLoopBenchmark`) for the hot-path movement system.
- **Graceful shutdown** hook for the WebSocket server and game loops.
- **ARCHITECTURE.md**, **API.md**, and **CHANGELOG.md** documentation.

### Changed
- Polished `README.md` with features, quick start, project structure, and tech stack.
- Mobile landscape layout now anchors the Reload button to the bottom-right of the map and accepts touch input outside the canvas.

### Fixed
- Mobile landscape layout previously clipped the map and hid the reload button; now uses `100svh` for accurate viewport sizing.

## 2026-06-21 — Initial ECS + Networking

- Implemented `EntityManager` and `ComponentRegistry`.
- Added `GameLoop`, `GameWorld`, and `Room`.
- Added lobby/matchmaking with 1v1, 2v2, 3v3 queues.
- Added WebSocket server and HTML5 canvas client.
- Added core systems: Movement, Weapon, Projectile, Combat, Death, Round, Vision, NetworkBroadcast.
