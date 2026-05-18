package com.example.alias.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREF_NAME = "alias_settings";

    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_DEFAULT_ROUND_TIME = "default_round_time";
    private static final String KEY_DEFAULT_WIN_SCORE = "default_win_score";
    private static final String KEY_DEFAULT_DIFFICULTY = "default_difficulty";

    public static final int ROUND_TIME_MIN = 30;
    public static final int ROUND_TIME_MAX = 120;
    public static final int ROUND_TIME_STEP = 5;
    public static final int ROUND_TIME_DEFAULT = 60;

    public static final int WIN_SCORE_MIN = 10;
    public static final int WIN_SCORE_MAX = 100;
    public static final int WIN_SCORE_STEP = 5;
    public static final int WIN_SCORE_DEFAULT = 30;

    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_MEDIUM = "medium";
    public static final String DIFFICULTY_HARD = "hard";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply();
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public void setVibrationEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply();
    }

    public int getDefaultRoundTime() {
        return prefs.getInt(KEY_DEFAULT_ROUND_TIME, ROUND_TIME_DEFAULT);
    }

    public void setDefaultRoundTime(int value) {
        prefs.edit().putInt(KEY_DEFAULT_ROUND_TIME, value).apply();
    }

    public int getDefaultWinScore() {
        return prefs.getInt(KEY_DEFAULT_WIN_SCORE, WIN_SCORE_DEFAULT);
    }

    public void setDefaultWinScore(int value) {
        prefs.edit().putInt(KEY_DEFAULT_WIN_SCORE, value).apply();
    }

    public String getDefaultDifficulty() {
        return prefs.getString(KEY_DEFAULT_DIFFICULTY, DIFFICULTY_EASY);
    }

    public void setDefaultDifficulty(String value) {
        prefs.edit().putString(KEY_DEFAULT_DIFFICULTY, value).apply();
    }

    public void resetToDefaults() {
        prefs.edit()
                .putBoolean(KEY_SOUND_ENABLED, true)
                .putBoolean(KEY_VIBRATION_ENABLED, true)
                .putInt(KEY_DEFAULT_ROUND_TIME, ROUND_TIME_DEFAULT)
                .putInt(KEY_DEFAULT_WIN_SCORE, WIN_SCORE_DEFAULT)
                .putString(KEY_DEFAULT_DIFFICULTY, DIFFICULTY_EASY)
                .apply();
    }
}