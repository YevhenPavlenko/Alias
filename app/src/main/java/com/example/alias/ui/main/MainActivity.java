package com.example.alias.ui.main;

import android.os.Bundle;
import android.widget.Button;

import com.example.alias.R;
import com.example.alias.ui.base.BaseActivity;

public class MainActivity extends BaseActivity {

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

        btnExit.setOnClickListener(v -> finish());
    }
}
