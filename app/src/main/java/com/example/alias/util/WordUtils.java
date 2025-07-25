package com.example.alias.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordUtils {

    public static List<String> getRandomWords(Context context, int count) {
        Set<String> uniqueWords = new HashSet<>();

        try {
            InputStream inputStream = context.getAssets().open("words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    uniqueWords.add(line.trim());
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> shuffled = new ArrayList<>(uniqueWords);
        Collections.shuffle(shuffled);

        return shuffled.size() > count ? shuffled.subList(0, count) : shuffled;
    }

}
