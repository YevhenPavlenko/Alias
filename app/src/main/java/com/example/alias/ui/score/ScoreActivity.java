package com.example.alias.ui.score;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Team;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.ui.setup.GameSetupActivity;
import com.example.alias.ui.main.MainActivity;
import com.example.alias.ui.score.adapter.TeamResultAdapter;

import java.util.ArrayList;

public class ScoreActivity extends BaseActivity {

    private ArrayList<Team> teamsForSetup;
    private ArrayList<Team> teams;
    private int timeLimit;
    private int winningPoints;
    private String difficulty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        readIntentData();

        setupHeader(getString(R.string.results_title));
        setupBackButton();

        setupResultsList();
        setupButtons();
    }

    @SuppressWarnings("unchecked")
    private void readIntentData() {
        Intent intent = getIntent();

        teams = (ArrayList<Team>) intent.getSerializableExtra("teams");
        if (teams == null) {
            teams = new ArrayList<>();
        }

        timeLimit = intent.getIntExtra("timeLimit", 60);
        winningPoints = intent.getIntExtra("winningPoints", 30);

        difficulty = intent.getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "easy";
        }

        teamsForSetup = copyTeamsWithoutScores(teams);

        teams.sort((team1, team2) ->
                Integer.compare(team2.getScore(), team1.getScore()));
    }

    private ArrayList<Team> copyTeamsWithoutScores(ArrayList<Team> sourceTeams) {
        ArrayList<Team> result = new ArrayList<>();

        if (sourceTeams == null) {
            return result;
        }

        for (Team team : sourceTeams) {
            if (team == null || team.getName() == null) {
                continue;
            }

            String teamName = team.getName().trim();

            if (!teamName.isEmpty()) {
                result.add(new Team(teamName));
            }
        }

        return result;
    }

    private void setupBackButton() {
        View btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> goToMainMenu());
        }
    }

    private void setupResultsList() {
        RecyclerView rvFinalResults = findViewById(R.id.rvFinalResults);

        rvFinalResults.setLayoutManager(new LinearLayoutManager(this));
        rvFinalResults.setAdapter(new TeamResultAdapter(teams));
        rvFinalResults.setHasFixedSize(true);
    }

    private void setupButtons() {
        AppCompatButton btnPlayAgain = findViewById(R.id.btnPlayAgain);
        AppCompatButton btnMainMenu = findViewById(R.id.btnMainMenu);

        animateButtons(btnPlayAgain, btnMainMenu);

        btnPlayAgain.setOnClickListener(v -> openSetupWithLastSettings());
        btnMainMenu.setOnClickListener(v -> goToMainMenu());
    }

    private void openSetupWithLastSettings() {
        Intent intent = new Intent(this, GameSetupActivity.class);

        intent.putExtra("timeLimit", timeLimit);
        intent.putExtra("winningPoints", winningPoints);
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("teams", teamsForSetup);

        startActivity(intent);
        finish();
    }

    private void goToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}