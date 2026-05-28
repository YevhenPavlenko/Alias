package com.example.alias.ui.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import com.example.alias.R;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.ui.gamemode.GameModeActivity;
import com.example.alias.ui.rules.RuleActivity;
import com.example.alias.ui.settings.SettingsActivity;
import com.example.alias.ui.history.HistoryActivity;

public class MainActivity extends BaseActivity {

    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnHowToPlay = findViewById(R.id.btnHowToPlay);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnExit = findViewById(R.id.btnExit);

        animateButtons(btnPlay, btnHowToPlay, btnHistory, btnSettings, btnExit);

        btnPlay.setOnClickListener(v -> navigateTo(GameModeActivity.class));
        btnHowToPlay.setOnClickListener(v -> navigateTo(RuleActivity.class));
        btnSettings.setOnClickListener(v -> navigateTo(SettingsActivity.class));
        btnHistory.setOnClickListener(v -> navigateTo(HistoryActivity.class));
        btnExit.setOnClickListener(v -> finish());

        enableDoubleBackToExit();
    }

    protected void enableDoubleBackToExit() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    if (backToast != null) backToast.cancel();
                    finish();
                } else {
                    backToast = showToast("Натисніть ще раз, щоб вийти");
                    backPressedTime = System.currentTimeMillis();
                }
            }
        });
    }
}
