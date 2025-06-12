package com.example.alias.ui.setup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Team;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.ui.setup.adapter.TeamAdapter;
import com.example.alias.util.SwipeHelper;

import java.util.ArrayList;
import java.util.List;

public class GameSetupActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_setup);
        setupHeader(R.string.game_setup);

        setupDifficultyButtons();
        setupTimeSeekBar();
        setupTeamCards();
    }

    private void setupDifficultyButtons() {
        Button btnEasy = findViewById(R.id.btnEasy);
        Button btnMedium = findViewById(R.id.btnMedium);
        Button btnHard = findViewById(R.id.btnHard);

        View.OnClickListener listener = v -> {
            btnEasy.setSelected(false);
            btnMedium.setSelected(false);
            btnHard.setSelected(false);
            v.setSelected(true);
        };

        btnEasy.setOnClickListener(listener);
        btnMedium.setOnClickListener(listener);
        btnHard.setOnClickListener(listener);

        btnEasy.setSelected(true);
    }

    private void setupTimeSeekBar() {
        AppCompatSeekBar sbTime = findViewById(R.id.sbTime);
        TextView tvTimeValue = findViewById(R.id.tvTimeValue);

        final int minTime = 30;
        final int defaultTime = 60;

        sbTime.setMax(120 - minTime);
        sbTime.setProgress(defaultTime - minTime);

        updateTimeText(tvTimeValue, defaultTime);

        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seconds = minTime + progress;
                updateTimeText(tvTimeValue, seconds);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void updateTimeText(TextView textView, int seconds) {
        textView.setText(getString(R.string.time_in_seconds, seconds));
    }

    private void setupTeamCards() {
        RecyclerView rvTeams = findViewById(R.id.rvTeams);
        AppCompatButton btnAddTeam = findViewById(R.id.btnAddTeam);

        List<Team> teamList = new ArrayList<>();
        teamList.add(new Team("Команда 1"));
        teamList.add(new Team("Команда 2"));

        TeamAdapter adapter = new TeamAdapter(this, teamList);
        rvTeams.setAdapter(adapter);
        rvTeams.setLayoutManager(new LinearLayoutManager(this));

        btnAddTeam.setOnClickListener(v -> {
            if (teamList.size() < 4) {
                teamList.add(new Team("Команда " + (teamList.size() + 1)));
                adapter.notifyItemInserted(teamList.size() - 1);
            } else {
                showToast("Максимум 4 команди");
            }
        });

        SwipeHelper.attachSwipeToDelete(rvTeams, adapter);
    }
}
