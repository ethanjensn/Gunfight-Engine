package com.gunfight.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gunfight.engine.GameWorld;
import com.gunfight.data.PositionComponent;
import com.gunfight.net.GameServer;
import com.gunfight.net.GameStatePacket;
import com.gunfight.net.GameStatePacket.PlayerState;
import com.google.gson.Gson;

public class NetworkBroadcastSystem {
    private GameServer server;
    private Gson gson = new Gson();

    // Object pool: pre-allocated PlayerState objects that get reused
    // playerPool is for the CPU to avoid garbage collection
    private final List<PlayerState> playerPool = new ArrayList<>();
    
    // Active states for this tick (references into playerPool)
    // a pointer to the playerPool list
    // activeStates is for the network
    private final List<PlayerState> activeStates = new ArrayList<>();
    // Permanent packet - Gson reads from activeStates each tick
    private final GameStatePacket packet = new GameStatePacket(activeStates);

    public NetworkBroadcastSystem(GameServer server) {
        this.server = server;
    }

    public void update(GameWorld world) {
        // activates exactly once per tick
        // Clear active list (objects remain in playerPool for reuse)
        activeStates.clear();

        Set<Integer> entities = world.getAllEntitiesWithComponent(PositionComponent.class);

        int index = 0;
        for (int entityId : entities) {
            PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
            if (pos == null) continue;

            // Expand pool if needed (only happens first few frames)
            if (index >= playerPool.size()) {
                playerPool.add(new PlayerState(0, 0, 0));
            }

            // Recycle existing object: update values in-place
            PlayerState state = playerPool.get(index);
            state.id = entityId;
            state.x = pos.x;
            state.y = pos.y;

            activeStates.add(state);
            index++;
        }

        // Reuse same packet - Gson serializes activeStates which now points to recycled objects
        String json = gson.toJson(packet);
        server.broadcast(json);
    }
}
