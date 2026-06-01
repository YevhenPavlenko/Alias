package com.example.alias.ui.gamemode;

import android.os.Bundle;
import android.view.View;

import com.example.alias.R;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.ui.dictionary.DictionaryActivity;
import com.example.alias.ui.setup.GameSetupActivity;

public class GameModeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

        setupHeader(getString(R.string.game_mode_title));

        View btnSingleDevice = findViewById(R.id.btnSingleDevice);
        View btnDictionary = findViewById(R.id.btnDictionary);

        animateButtons(btnSingleDevice, btnDictionary);

        btnSingleDevice.setOnClickListener(v -> navigateTo(GameSetupActivity.class));
        btnDictionary.setOnClickListener(v -> navigateTo(DictionaryActivity.class));
    }
}