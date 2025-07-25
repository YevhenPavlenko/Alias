package com.example.alias.model;

import java.util.Arrays;
import java.util.List;

public class Game {
    public List<String> wordsList;

    public Game(String[] words) {
        wordsList = Arrays.asList(words);
    }
}