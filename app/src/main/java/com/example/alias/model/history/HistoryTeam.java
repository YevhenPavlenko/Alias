package com.example.alias.model.history;

import java.util.List;

public class HistoryTeam {

    private final long id;
    private final long gameId;
    private final String name;
    private final int score;
    private final int place;

    private boolean expanded;
    private List<HistoryWord> words;

    public HistoryTeam(long id, long gameId, String name, int score, int place) {
        this.id = id;
        this.gameId = gameId;
        this.name = name;
        this.score = score;
        this.place = place;
    }

    public long getId() {
        return id;
    }

    public long getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getPlace() {
        return place;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public List<HistoryWord> getWords() {
        return words;
    }

    public void setWords(List<HistoryWord> words) {
        this.words = words;
    }
}