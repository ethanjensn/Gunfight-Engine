package com.gunfight.lobby;

public enum PlayerState {
    MENU,      // In main menu, can join queue
    QUEUED,    // In matchmaking queue
    MATCHMAKING, // Match found, countdown to game start
    IN_GAME    // Actively playing in ECS Room
}
