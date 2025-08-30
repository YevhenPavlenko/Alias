package com.example.alias.model;

import android.content.Context;

import com.example.alias.util.WordUtils;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public List<String> wordsList;
    public List<Team> teamsList;
    public int pointsToWin;
    public int turnTime;

    public Game(ArrayList<Team> teams, int timeLimit, int winningPoints, String difficulty, Context context) {
        this.teamsList = teams;
        this.turnTime = timeLimit;
        this.pointsToWin = winningPoints;

        this.wordsList = WordUtils.getRandomWords(context, 100, difficulty);
    }
}
