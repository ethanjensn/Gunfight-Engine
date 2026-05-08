# Gunfight-Engine
2v2 Round Based Game with custom engine

## Project Structure

```
Gunfight-Engine/
├── app/
│   ├── bin/
│   │   ├── main/
│   │   └── test/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gunfight/
│   │   │   │   ├── engine/          # Core engine: EntityManager, ComponentRegistry, GameLoop, GameWorld
│   │   │   │   ├── data/            # Components ("Attachments"): state containers attached to entities
│   │   │   │   ├── logic/           # Server-side game systems: read component state and apply game rules
│   │   │   │   ├── client/          # Client-only: captures local input and sends packets to server
│   │   │   │   ├── net/             # Networking: receives packets and writes to component state
│   │   │   │   └── Main.java        # Entry point
│   │   │   └── resources/           # Config files (JSON/YAML)
│   │   └── test/
│   │       └── java/com/gunfight/   # Unit tests
│   │           └── AppTest.java
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## Build

```bash
./gradlew build
./gradlew run
```
