package com.gunfight.lobby;

import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueManager {
    // Queue per game mode: "1v1", "2v2", "3v3"
    private final Map<String, Queue<QueueEntry>> queues = new ConcurrentHashMap<>();
    
    // Fast lookup: WebSocket -> its QueueEntry for O(1) position calculation
    private final Map<WebSocket, QueueEntry> entryMap = new ConcurrentHashMap<>();
    
    // Required players per mode
    private static final Map<String, Integer> PLAYERS_PER_MODE = Map.of(
        "1v1", 2,
        "2v2", 4,
        "3v3", 6
    );

    public QueueManager() {
        // Initialize queues for all modes
        queues.put("1v1", new ConcurrentLinkedQueue<>());
        queues.put("2v2", new ConcurrentLinkedQueue<>());
        queues.put("3v3", new ConcurrentLinkedQueue<>());
    }

    public boolean joinQueue(WebSocket connection, String gameMode, String username) {
        Queue<QueueEntry> queue = queues.get(gameMode);
        if (queue == null) return false;

        // Prevent duplicate entries for same connection
        if (entryMap.containsKey(connection)) {
            return false;
        }

        QueueEntry entry = new QueueEntry(connection, username, gameMode);
        queue.offer(entry);
        entryMap.put(connection, entry);
        return true;
    }

    public boolean cancelQueue(WebSocket connection) {
        QueueEntry entry = entryMap.remove(connection);
        if (entry == null) return false;

        Queue<QueueEntry> queue = queues.get(entry.gameMode);
        if (queue != null) {
            queue.remove(entry);
        }
        return true;
    }

    public void removeDeadEntry(QueueEntry entry) {
        entryMap.remove(entry.connection);
        Queue<QueueEntry> queue = queues.get(entry.gameMode);
        if (queue != null) {
            queue.remove(entry);
        }
    }

    public int getQueueSize(String gameMode) {
        Queue<QueueEntry> queue = queues.get(gameMode);
        return queue != null ? queue.size() : 0;
    }

    public int getPosition(WebSocket connection) {
        QueueEntry target = entryMap.get(connection);
        if (target == null) return -1;

        Queue<QueueEntry> queue = queues.get(target.gameMode);
        if (queue == null) return -1;

        int position = 1;
        for (QueueEntry entry : queue) {
            if (entry.connection == connection) {
                return position;
            }
            position++;
        }
        return -1; // Not found (shouldn't happen if entryMap is consistent)
    }

    public int getRequiredPlayers(String gameMode) {
        return PLAYERS_PER_MODE.getOrDefault(gameMode, 2);
    }

    public Queue<QueueEntry> getQueue(String gameMode) {
        return queues.get(gameMode);
    }

    public QueueEntry getEntry(WebSocket connection) {
        return entryMap.get(connection);
    }

    // For cleanup when player disconnects
    public void onDisconnect(WebSocket connection) {
        QueueEntry entry = entryMap.remove(connection);
        if (entry != null) {
            Queue<QueueEntry> queue = queues.get(entry.gameMode);
            if (queue != null) {
                queue.remove(entry);
            }
        }
    }

    public int estimateWaitSeconds(String gameMode) {
        int queueSize = getQueueSize(gameMode);
        int required = getRequiredPlayers(gameMode);
        
        if (queueSize == 0) return 0;
        
        // Rough estimate: assume match forms every 5-10 seconds on average
        // when there's activity, longer when queue is small
        int matchesNeeded = (queueSize + required - 1) / required;
        return Math.max(5, matchesNeeded * 8);
    }
}
