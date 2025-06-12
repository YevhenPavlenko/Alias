package com.example.alias.ui.gamemode;

import android.os.Bundle;
import android.widget.Button;

import com.example.alias.R;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.ui.setup.GameSetupActivity;

public class GameModeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);
        setupHeader(R.string.chose_game_mode);

        Button btnSingleDevice = findViewById(R.id.btnSingleDevice);
        Button btnJoinLobby = findViewById(R.id.btnJoinLobby);
        Button btnCreateLobby = findViewById(R.id.btnCreateLobby);

        animateButtons(btnSingleDevice, btnJoinLobby, btnCreateLobby);

        btnSingleDevice.setOnClickListener(v -> navigateTo(GameSetupActivity.class));
    }
}
