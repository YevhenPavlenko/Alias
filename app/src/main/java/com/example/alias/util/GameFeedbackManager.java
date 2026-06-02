package com.example.alias.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.example.alias.ui.settings.SettingsManager;

import java.util.HashSet;
import java.util.Set;

public class GameFeedbackManager {

    private static final String SOUND_CORRECT = "sfx_correct";
    private static final String SOUND_SKIP = "sfx_skip";
    private static final String SOUND_TEN_SECONDS = "sfx_ten_seconds";
    private static final String SOUND_TICK = "sfx_tick";
    private static final String SOUND_TIME_UP = "sfx_time_up";
    private static final String SOUND_WIN = "sfx_win";

    private final Context appContext;
    private final SettingsManager settingsManager;
    private final Vibrator vibrator;
    private final SoundPool soundPool;

    private final Set<Integer> loadedSoundIds = new HashSet<>();
    private final Set<Integer> pendingSoundIds = new HashSet<>();

    private final int soundCorrect;
    private final int soundSkip;
    private final int soundTenSeconds;
    private final int soundTick;
    private final int soundWin;

    private boolean released = false;

    public GameFeedbackManager(Context context) {
        appContext = context.getApplicationContext();
        settingsManager = new SettingsManager(appContext);
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status != 0 || released) {
                return;
            }

            loadedSoundIds.add(sampleId);

            if (pendingSoundIds.remove(sampleId) && settingsManager.isSoundEnabled()) {
                pool.play(sampleId, 1f, 1f, 1, 0, 1f);
            }
        });

        soundCorrect = loadSound(SOUND_CORRECT);
        soundSkip = loadSound(SOUND_SKIP);
        soundTenSeconds = loadSound(SOUND_TEN_SECONDS);
        soundTick = loadSound(SOUND_TICK);
        soundWin = loadSound(SOUND_WIN);
    }

    public void playCorrectWord() {
        play(soundCorrect);
    }

    public void playSkippedWord() {
        play(soundSkip);
    }

    public void playTenSecondsWarning() {
        play(soundTenSeconds);
    }

    public void playCountdownTick() {
        play(soundTick);
    }

    public void playTimeUpLastWord() {
        //play(soundTimeUp);
        vibratePattern(
                new long[]{0, 120, 70, 180},
                new int[]{0, 190, 0, 230}
        );
    }

    public void playGameWin() {
        play(soundWin);
        vibratePattern(
                new long[]{0, 80, 60, 80, 60, 240},
                new int[]{0, 150, 0, 180, 0, 255}
        );
    }

    public void release() {
        released = true;
        pendingSoundIds.clear();
        loadedSoundIds.clear();
        soundPool.release();
    }

    private int loadSound(String rawResourceName) {
        @SuppressLint("DiscouragedApi") int resourceId = appContext.getResources().getIdentifier(
                rawResourceName,
                "raw",
                appContext.getPackageName()
        );

        if (resourceId == 0) {
            return 0;
        }

        return soundPool.load(appContext, resourceId, 1);
    }

    private void play(int soundId) {
        if (released || soundId == 0 || !settingsManager.isSoundEnabled()) {
            return;
        }

        if (loadedSoundIds.contains(soundId)) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
        } else {
            pendingSoundIds.add(soundId);
        }
    }

    private void vibratePattern(long[] timings, int[] amplitudes) {
        if (!canVibrate()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                    VibrationEffect.createWaveform(
                            timings,
                            amplitudes,
                            -1
                    )
            );
        } else {
            vibrator.vibrate(timings, -1);
        }
    }

    private boolean canVibrate() {
        return !released
                && settingsManager.isVibrationEnabled()
                && vibrator != null
                && vibrator.hasVibrator();
    }

    private int clampAmplitude(int value) {
        return Math.max(1, Math.min(255, value));
    }
}