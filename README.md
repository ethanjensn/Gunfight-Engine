# Gunfight-Engine
2v2 Round Based Game with custom engine

## Project Structure

```
Gunfight-Engine/
├── adrs/                        # Architecture Decision Records
├── src/
│   ├── main/
│   │   ├── java/com/gunfight/
│   │   │   ├── engine/          # Core: Engine, EntityManager, GameLoop
│   │   │   ├── data/            # Components: HealthComponent, PositionComponent, VelocityComponent
│   │   │   ├── logic/           # Systems: MovementSystem, CombatSystem
│   │   │   ├── net/             # Networking: GameServer
│   │   │   └── Main.java        # Entry point
│   │   └── resources/           # Config files (JSON/YAML)
│   └── test/
│       └── java/com/gunfight/   # Unit tests
├── build.gradle
├── settings.gradle
└── README.md
```

## Build

```bash
./gradlew build
./gradlew run
```
