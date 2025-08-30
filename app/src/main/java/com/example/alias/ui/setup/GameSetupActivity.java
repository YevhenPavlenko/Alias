package com.example.alias.ui.setup;

import android.content.Intent;
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
import com.example.alias.ui.game.GameActivity;
import com.example.alias.ui.setup.adapter.TeamAdapter;
import com.example.alias.util.SwipeHelper;

import java.util.ArrayList;
import java.util.List;

public class GameSetupActivity extends BaseActivity {

    private List<Team> teamsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_setup);
        setupHeader(R.string.game_setup);

        setupDifficultyButtons();
        setupTimeSeekBar();
        setupPointSeekBar();
        setupTeamCards();

        Button btnAddTeam = findViewById(R.id.btnAddTeam);
        Button btnPlay = findViewById(R.id.btnPlay);

        animateButtons(btnAddTeam, btnPlay);

        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);

            int timeLimit = 30 + ((AppCompatSeekBar) findViewById(R.id.sbTime)).getProgress();
            int winningPoints = Math.max(((AppCompatSeekBar) findViewById(R.id.sbWinningPoints)).getProgress(), 10);

            String difficulty = "easy";
            if (findViewById(R.id.btnMedium).isSelected()) difficulty = "medium";
            else if (findViewById(R.id.btnHard).isSelected()) difficulty = "hard";

            ArrayList<Team> teams = new ArrayList<>(teamsList);

            intent.putExtra("timeLimit", timeLimit);
            intent.putExtra("winningPoints", winningPoints);
            intent.putExtra("difficulty", difficulty);
            intent.putExtra("teams", teams);

            startActivity(intent);
        });
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

    private void setupPointSeekBar() {
        AppCompatSeekBar sbWinningPoints = findViewById(R.id.sbWinningPoints);
        TextView tvWinningPointsValue = findViewById(R.id.tvWinningPointsValue);

        sbWinningPoints.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int points = Math.max(progress, 10);
                tvWinningPointsValue.setText(String.valueOf(points));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateTimeText(TextView textView, int seconds) {
        textView.setText(getString(R.string.time_in_seconds, seconds));
    }

    private void setupTeamCards() {
        RecyclerView rvTeams = findViewById(R.id.rvTeams);
        AppCompatButton btnAddTeam = findViewById(R.id.btnAddTeam);

        teamsList = new ArrayList<>();
        teamsList.add(new Team("Команда 1"));
        teamsList.add(new Team("Команда 2"));

        TeamAdapter adapter = new TeamAdapter(this, teamsList);
        rvTeams.setAdapter(adapter);
        rvTeams.setLayoutManager(new LinearLayoutManager(this));

        btnAddTeam.setOnClickListener(v -> {
            if (teamsList.size() >= 4) {
                showToast("Максимум 4 команди");
                return;
            }

            int index = 1;
            String newName;
            do {
                newName = "Команда " + index++;
            } while (isNameAlreadyUsed(teamsList, newName));

            teamsList.add(new Team(newName));
            adapter.notifyItemInserted(teamsList.size() - 1);
        });

        SwipeHelper.attachSwipeToDelete(rvTeams, adapter);
    }

    private boolean isNameAlreadyUsed(List<Team> teamList, String name) {
        for (Team team : teamList) {
            if (team.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
