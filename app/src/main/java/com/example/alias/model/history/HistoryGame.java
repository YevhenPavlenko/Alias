package com.example.alias.model.history;

import java.util.List;

public class HistoryGame {

    private final long id;
    private final long finishedAtMillis;
    private final long durationSeconds;
    private final int roundTime;
    private final int pointsToWin;
    private final String difficulty;
    private final String winnerTeamName;

    private boolean expanded;
    private boolean teamsExpanded;
    private List<HistoryTeam> teams;

    public HistoryGame(
            long id,
            long finishedAtMillis,
            long durationSeconds,
            int roundTime,
            int pointsToWin,
            String difficulty,
            String winnerTeamName
    ) {
        this.id = id;
        this.finishedAtMillis = finishedAtMillis;
        this.durationSeconds = durationSeconds;
        this.roundTime = roundTime;
        this.pointsToWin = pointsToWin;
        this.difficulty = difficulty;
        this.winnerTeamName = winnerTeamName;
    }

    public long getId() {
        return id;
    }

    public long getFinishedAtMillis() {
        return finishedAtMillis;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public int getRoundTime() {
        return roundTime;
    }

    public int getPointsToWin() {
        return pointsToWin;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getWinnerTeamName() {
        return winnerTeamName;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isTeamsExpanded() {
        return teamsExpanded;
    }

    public void setTeamsExpanded(boolean teamsExpanded) {
        this.teamsExpanded = teamsExpanded;
    }

    public List<HistoryTeam> getTeams() {
        return teams;
    }

    public void setTeams(List<HistoryTeam> teams) {
        this.teams = teams;
    }
}