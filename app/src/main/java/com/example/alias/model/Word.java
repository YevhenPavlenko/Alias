package com.example.alias.model;

public class Word {
    private final String text;
    private boolean guessed;

    public Word(String text, boolean guessed) {
        this.text = text;
        this.guessed = guessed;
    }

    public String getText() { return text; }
    public boolean isGuessed() { return guessed; }
    public void setGuessed(boolean guessed) { this.guessed = guessed; }
}