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
    public int roundWinnerTeam; // -1 until a team wins the current round
    public int matchWinnerTeam; // -1 until a team wins the match

    public RoundStateComponent() {
        this.roundNumber = 0;
        this.phase = RoundPhase.WAITING;
        this.phaseTimer = 0;
        this.roundWinnerTeam = -1;
        this.matchWinnerTeam = -1;
    }
}
