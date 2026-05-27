package com.example.alias.model;

public class Word {

    private final String text;
    private boolean guessed;

    private boolean lastWord;
    private Team assignedTeam;

    public Word(String text, boolean guessed) {
        this(text, guessed, false);
    }

    public Word(String text, boolean guessed, boolean lastWord) {
        this.text = text;
        this.guessed = guessed;
        this.lastWord = lastWord;
        this.assignedTeam = null;
    }

    public String getText() {
        return text;
    }

    public boolean isGuessed() {
        return guessed;
    }

    public void setGuessed(boolean guessed) {
        this.guessed = guessed;
    }

    public boolean isLastWord() {
        return lastWord;
    }

    public void setLastWord(boolean lastWord) {
        this.lastWord = lastWord;
    }

    public Team getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(Team assignedTeam) {
        this.assignedTeam = assignedTeam;
    }
}