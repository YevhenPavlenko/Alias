package com.example.alias.model;

public class GameWordResult {

    private final Team ownerTeam;
    private final Team assignedTeam;
    private final String text;
    private final boolean guessed;
    private final boolean lastWord;
    private final int orderIndex;

    public GameWordResult(
            Team ownerTeam,
            Team assignedTeam,
            String text,
            boolean guessed,
            boolean lastWord,
            int orderIndex
    ) {
        this.ownerTeam = ownerTeam;
        this.assignedTeam = assignedTeam;
        this.text = text;
        this.guessed = guessed;
        this.lastWord = lastWord;
        this.orderIndex = orderIndex;
    }

    public Team getOwnerTeam() {
        return ownerTeam;
    }

    public Team getAssignedTeam() {
        return assignedTeam;
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