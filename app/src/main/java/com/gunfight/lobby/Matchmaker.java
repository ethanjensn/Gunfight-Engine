package com.gunfight.lobby;

import com.gunfight.engine.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Matchmaker {
    private final QueueManager queueManager;
    private final RoomManager roomManager;
    private final LobbyManager lobbyManager;
    private final ScheduledExecutorService executor;
    private static final long MATCHMAKING_INTERVAL_MS = 500; // Run every 500ms

    public Matchmaker(QueueManager queueManager, RoomManager roomManager, LobbyManager lobbyManager) {
        this.queueManager = queueManager;
        this.roomManager = roomManager;
        this.lobbyManager = lobbyManager;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        executor.scheduleAtFixedRate(this::runMatchmaking, 1000, MATCHMAKING_INTERVAL_MS, TimeUnit.MILLISECONDS);
        System.out.println("Matchmaker started");
    }

    public void stop() {
        executor.shutdown();
        System.out.println("Matchmaker stopped");
    }

    private void runMatchmaking() {
        try {
            // Try to create matches for each game mode
            tryCreateMatches("1v1");
            tryCreateMatches("2v2");
            tryCreateMatches("3v3");
            
            // Broadcast updated queue status to all waiting players
            lobbyManager.broadcastQueueStatus();
        } catch (Exception e) {
            System.err.println("Matchmaking error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void tryCreateMatches(String gameMode) {
        int requiredPlayers = queueManager.getRequiredPlayers(gameMode);
        Queue<QueueEntry> queue = queueManager.getQueue(gameMode);
        
        if (queue == null || queue.size() < requiredPlayers) {
            return; // Not enough players
        }

        // Keep trying to create matches while we have enough players
        while (queue.size() >= requiredPlayers) {
            List<QueueEntry> matchedPlayers = new ArrayList<>();
            List<QueueEntry> deadEntries = new ArrayList<>();
            
            // Poll players from queue
            for (int i = 0; i < requiredPlayers; i++) {
                QueueEntry entry = queue.poll();
                if (entry == null) break;
                
                // ZOMBIE CONNECTION CHECK: Verify socket is still open
                if (!entry.connection.isOpen()) {
                    // Dead socket - remove from tracking and skip
                    queueManager.removeDeadEntry(entry);
                    deadEntries.add(entry);
                    i--; // Don't count this slot, need another player
                    continue;
                }
                
                matchedPlayers.add(entry);
            }
            
            // If we removed dead entries, we might not have enough valid players
            // Put any valid polled players back and try again next tick
            if (matchedPlayers.size() < requiredPlayers) {
                // Return valid players to front of queue
                for (QueueEntry entry : matchedPlayers) {
                    queue.offer(entry); // Add back to queue
                }
                break; // Not enough players this round
            }
            
            // All connections verified open - safe to create room
            try {
                Room room = roomManager.createRoom(gameMode, matchedPlayers);
                lobbyManager.onMatchFound(gameMode, matchedPlayers, room);
                System.out.println("Created " + gameMode + " match with " + matchedPlayers.size() + " players");
            } catch (Exception e) {
                System.err.println("Failed to create room: " + e.getMessage());
                // Return players to queue
                for (QueueEntry entry : matchedPlayers) {
                    queue.offer(entry);
                }
                break;
            }
        }
    }
}
