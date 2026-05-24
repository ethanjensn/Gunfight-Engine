package com.gunfight.data;

public class RoundStateComponent {

    public enum RoundPhase {
        WAITING,
        IN_ROUND,
        ROUND_OVER,
        MATCH_OVER
    }

    public int roundNumber;
    public RoundPhase phase;
    public int phaseTimer;

    public RoundStateComponent() {
        this.roundNumber = 0;
        this.phase = RoundPhase.WAITING;
        this.phaseTimer = 0;
    }
}
