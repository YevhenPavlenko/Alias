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

    private static final int TIME_MIN = 30;
    private static final int TIME_MAX = 120;
    private static final int TIME_STEP = 5;
    private static final int TIME_DEFAULT = 60;

    private static final int POINTS_MIN = 10;
    private static final int POINTS_MAX = 100;
    private static final int POINTS_STEP = 5;
    private static final int POINTS_DEFAULT = 30;
    private List<Team> teamsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_setup);
        setupHeader(getString(R.string.game_setup_title));

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

        int maxProgress = (TIME_MAX - TIME_MIN) / TIME_STEP;
        int defaultProgress = (TIME_DEFAULT - TIME_MIN) / TIME_STEP;

        sbTime.setMax(maxProgress);
        sbTime.setProgress(defaultProgress);

        updateTimeText(tvTimeValue, TIME_DEFAULT);

        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seconds = TIME_MIN + progress * TIME_STEP;
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

        int maxProgress = (POINTS_MAX - POINTS_MIN) / POINTS_STEP;
        int defaultProgress = (POINTS_DEFAULT - POINTS_MIN) / POINTS_STEP;

        sbWinningPoints.setMax(maxProgress);
        sbWinningPoints.setProgress(defaultProgress);

        updatePointsText(tvWinningPointsValue, POINTS_DEFAULT);

        sbWinningPoints.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int points = POINTS_MIN + progress * POINTS_STEP;
                updatePointsText(tvWinningPointsValue, points);
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

    private void updatePointsText(TextView textView, int points) {
        textView.setText(String.valueOf(points));
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
