package com.example.alias.util;

import android.content.Context;

import java.util.List;

public class WordUtils {

    public static List<String> getRandomWords(Context context, int count, String difficulty) {
        DictionaryDbHelper dbHelper = new DictionaryDbHelper(context);

        return dbHelper.getRandomCatalogWords(difficulty, count);
    }
}