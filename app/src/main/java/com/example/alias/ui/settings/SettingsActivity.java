package com.example.alias.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.example.alias.R;
import com.example.alias.databinding.ActivitySettingsBinding;
import com.example.alias.ui.base.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    private SettingsManager settingsManager;

    private boolean ignoreSpinnerCallback = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHeader(getString(R.string.settings));

        settingsManager = new SettingsManager(this);

        setupSeekBars();
        setupDifficultySpinner();
        loadSettings();
        setupListeners();
    }

    private void setupSeekBars() {
        int roundTimeMaxProgress =
                (SettingsManager.ROUND_TIME_MAX - SettingsManager.ROUND_TIME_MIN) / SettingsManager.ROUND_TIME_STEP;
        int winScoreMaxProgress =
                (SettingsManager.WIN_SCORE_MAX - SettingsManager.WIN_SCORE_MIN) / SettingsManager.WIN_SCORE_STEP;

        binding.seekBarRoundTime.setMax(roundTimeMaxProgress);
        binding.seekBarWinScore.setMax(winScoreMaxProgress);
    }

    private void setupDifficultySpinner() {
        String[] difficultyOptions = {
                getString(R.string.easy),
                getString(R.string.medium),
                getString(R.string.hard)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                difficultyOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDifficulty.setAdapter(adapter);
    }

    private void loadSettings() {
        binding.switchSound.setChecked(settingsManager.isSoundEnabled());
        binding.switchVibration.setChecked(settingsManager.isVibrationEnabled());

        int roundTime = settingsManager.getDefaultRoundTime();
        int winScore = settingsManager.getDefaultWinScore();
        String difficulty = settingsManager.getDefaultDifficulty();

        int roundTimeProgress =
                (roundTime - SettingsManager.ROUND_TIME_MIN) / SettingsManager.ROUND_TIME_STEP;
        int winScoreProgress =
                (winScore - SettingsManager.WIN_SCORE_MIN) / SettingsManager.WIN_SCORE_STEP;

        binding.seekBarRoundTime.setProgress(roundTimeProgress);
        binding.seekBarWinScore.setProgress(winScoreProgress);

        binding.tvRoundTimeValue.setText(getString(R.string.time_in_seconds, roundTime));
        binding.tvWinScoreValue.setText(String.valueOf(winScore));

        ignoreSpinnerCallback = true;
        switch (difficulty) {
            case SettingsManager.DIFFICULTY_MEDIUM:
                binding.spinnerDifficulty.setSelection(1);
                break;
            case SettingsManager.DIFFICULTY_HARD:
                binding.spinnerDifficulty.setSelection(2);
                break;
            default:
                binding.spinnerDifficulty.setSelection(0);
                break;
        }
        ignoreSpinnerCallback = false;
    }

    private void setupListeners() {
        binding.switchSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                settingsManager.setSoundEnabled(isChecked)
        );

        binding.switchVibration.setOnCheckedChangeListener((buttonView, isChecked) ->
                settingsManager.setVibrationEnabled(isChecked)
        );

        binding.seekBarRoundTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = SettingsManager.ROUND_TIME_MIN + progress * SettingsManager.ROUND_TIME_STEP;
                binding.tvRoundTimeValue.setText(getString(R.string.time_in_seconds, value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = SettingsManager.ROUND_TIME_MIN + seekBar.getProgress() * SettingsManager.ROUND_TIME_STEP;
                settingsManager.setDefaultRoundTime(value);
            }
        });

        binding.seekBarWinScore.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = SettingsManager.WIN_SCORE_MIN + progress * SettingsManager.WIN_SCORE_STEP;
                binding.tvWinScoreValue.setText(String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = SettingsManager.WIN_SCORE_MIN + seekBar.getProgress() * SettingsManager.WIN_SCORE_STEP;
                settingsManager.setDefaultWinScore(value);
            }
        });

        binding.spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ignoreSpinnerCallback) {
                    return;
                }

                String difficulty;
                if (position == 1) {
                    difficulty = SettingsManager.DIFFICULTY_MEDIUM;
                } else if (position == 2) {
                    difficulty = SettingsManager.DIFFICULTY_HARD;
                } else {
                    difficulty = SettingsManager.DIFFICULTY_EASY;
                }

                settingsManager.setDefaultDifficulty(difficulty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        binding.btnResetSettings.setOnClickListener(v -> {
            settingsManager.resetToDefaults();
            loadSettings();
            showToast(getString(R.string.settings_reset));
        });
    }
}