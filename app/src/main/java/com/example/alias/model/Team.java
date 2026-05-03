package com.example.alias.model;

import java.io.Serializable;

public class Team implements Serializable {
    private String name;
    private int score;

    public Team(String name) {
        this.name = name;
        this.score = 0;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getScore() { return score; }
    public void incrementScore() { score++; }
    public void decrementScore() { score--; }
}