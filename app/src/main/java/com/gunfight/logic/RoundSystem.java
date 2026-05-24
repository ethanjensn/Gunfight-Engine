package com.gunfight.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gunfight.data.HealthComponent;
import com.gunfight.data.InputComponent;
import com.gunfight.data.PositionComponent;
import com.gunfight.data.RespawnComponent;
import com.gunfight.data.RoundStateComponent;
import com.gunfight.data.RoundStateComponent.RoundPhase;
import com.gunfight.data.ScoreComponent;
import com.gunfight.data.SpawnPointComponent;
import com.gunfight.data.WeaponComponent;
import com.gunfight.engine.GameWorld;

public class RoundSystem {
    private static final int WINS_TO_WIN_MATCH = 3;
    private static final int ROUND_OVER_PAUSE_TICKS = 180; // 3 seconds at 60 tps

    private final List<Integer> playerEntities = new ArrayList<>();

    public void update(GameWorld world) {
        // Find the singleton match-state entity
        Set<Integer> matchEntities = world.getAllEntitiesWithComponent(RoundStateComponent.class);
        if (matchEntities.isEmpty()) return;
        int matchEntity = matchEntities.iterator().next();
        RoundStateComponent state = world.getComponent(RoundStateComponent.class, matchEntity);
        if (state == null) return;

        switch (state.phase) {
            case WAITING:
                handleWaiting(world, state);
                break;
            case IN_ROUND:
                handleInRound(world, state);
                break;
            case ROUND_OVER:
                handleRoundOver(world, state);
                break;
            case MATCH_OVER:
                handleMatchOver(world, state);
                break;
        }
    }

    private void handleWaiting(GameWorld world, RoundStateComponent state) {
        // Wait until 2 players with ScoreComponent are present
        Set<Integer> players = world.getAllEntitiesWithComponent(ScoreComponent.class);
        if (players.size() < 2) return;

        startRound(world, state);
    }

    private void handleInRound(GameWorld world, RoundStateComponent state) {
        // A death is signaled by DeathSystem attaching RespawnComponent
        Set<Integer> players = world.getAllEntitiesWithComponent(ScoreComponent.class);
        playerEntities.clear();
        playerEntities.addAll(players);

        int deadCount = 0;
        int survivorId = -1;

        for (int entityId : playerEntities) {
            RespawnComponent respawn = world.getComponent(RespawnComponent.class, entityId);
            if (respawn != null) {
                deadCount++;
            } else {
                survivorId = entityId;
            }
        }

        if (deadCount == 0) return;

        // Award point to survivor
        if (survivorId != -1) {
            ScoreComponent score = world.getComponent(ScoreComponent.class, survivorId);
            if (score != null) score.wins++;
        }

        state.phase = RoundPhase.ROUND_OVER;
        state.phaseTimer = ROUND_OVER_PAUSE_TICKS;
    }

    private void handleRoundOver(GameWorld world, RoundStateComponent state) {
        state.phaseTimer--;
        if (state.phaseTimer > 0) return;

        // Check if anyone has won the match
        Set<Integer> players = world.getAllEntitiesWithComponent(ScoreComponent.class);
        for (int entityId : players) {
            ScoreComponent score = world.getComponent(ScoreComponent.class, entityId);
            if (score != null && score.wins >= WINS_TO_WIN_MATCH) {
                state.phase = RoundPhase.MATCH_OVER;
                return;
            }
        }

        // No winner yet — start next round
        state.roundNumber++;
        startRound(world, state);
    }

    private void handleMatchOver(GameWorld world, RoundStateComponent state) {
        // Wait for all players to signal readyForRematch
        Set<Integer> players = world.getAllEntitiesWithComponent(ScoreComponent.class);
        if (players.size() < 2) return;

        for (int entityId : players) {
            ScoreComponent score = world.getComponent(ScoreComponent.class, entityId);
            if (score == null || !score.readyForRematch) return;
        }

        // Reset all scores and rematch flags
        for (int entityId : players) {
            ScoreComponent score = world.getComponent(ScoreComponent.class, entityId);
            if (score != null) {
                score.wins = 0;
                score.readyForRematch = false;
            }
        }

        state.roundNumber = 0;
        state.phase = RoundPhase.WAITING;
    }

    private void startRound(GameWorld world, RoundStateComponent state) {
        Set<Integer> players = world.getAllEntitiesWithComponent(ScoreComponent.class);

        for (int entityId : players) {
            // Clear death marker if present
            world.removeComponent(RespawnComponent.class, entityId);

            // Reset position to spawn point
            SpawnPointComponent spawn = world.getComponent(SpawnPointComponent.class, entityId);
            PositionComponent pos = world.getComponent(PositionComponent.class, entityId);
            if (spawn != null && pos != null) {
                pos.x = spawn.x;
                pos.y = spawn.y;
            }

            // Reset health
            HealthComponent hp = world.getComponent(HealthComponent.class, entityId);
            if (hp != null) {
                hp.health = hp.maxHealth;
            }

            // Restore capability if stripped by DeathSystem
            if (world.getComponent(InputComponent.class, entityId) == null) {
                world.addComponent(InputComponent.class, entityId, new InputComponent());
            }
            if (world.getComponent(WeaponComponent.class, entityId) == null) {
                world.addComponent(WeaponComponent.class, entityId,
                        new WeaponComponent(10, (short) 50, (short) 50, 3, 90));
            }

            // Auto-reload — always reset ammo to full at round start
            WeaponComponent weapon = world.getComponent(WeaponComponent.class, entityId);
            if (weapon != null) {
                weapon.ammo = weapon.maxAmmo;
                weapon.reloading = false;
            }
        }

        state.phase = RoundPhase.IN_ROUND;
    }
}
