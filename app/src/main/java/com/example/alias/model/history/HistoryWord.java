package com.example.alias.model.history;

public class HistoryWord {

    private final long id;
    private final long gameId;
    private final long ownerTeamId;
    private final Long assignedTeamId;
    private final String text;
    private final boolean guessed;
    private final boolean lastWord;
    private final int orderIndex;

    public HistoryWord(
            long id,
            long gameId,
            long ownerTeamId,
            Long assignedTeamId,
            String text,
            boolean guessed,
            boolean lastWord,
            int orderIndex
    ) {
        this.id = id;
        this.gameId = gameId;
        this.ownerTeamId = ownerTeamId;
        this.assignedTeamId = assignedTeamId;
        this.text = text;
        this.guessed = guessed;
        this.lastWord = lastWord;
        this.orderIndex = orderIndex;
    }

    public long getId() {
        return id;
    }

    public long getGameId() {
        return gameId;
    }

    public long getOwnerTeamId() {
        return ownerTeamId;
    }

    public Long getAssignedTeamId() {
        return assignedTeamId;
    }

    public String getText() {
        return text;
    }

    public boolean isGuessed() {
        return guessed;
    }

    public boolean isLastWord() {
        return lastWord;
    }

    public int getOrderIndex() {
        return orderIndex;
    }
}