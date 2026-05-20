package com.example.alias.ui.game;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Game;
import com.example.alias.model.Team;
import com.example.alias.model.Word;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.ui.game.adapter.TurnResultsAdapter;
import com.example.alias.util.DashedZoneDrawable;
import com.example.alias.util.DialogUtils;

import java.util.ArrayList;
import java.util.Collections;

public class GameActivity extends BaseActivity {

    private TextView tvWordCard;
    private TextView tvTimeLeft;
    private TextView tvTurnScore;
    private int currentWordIndex = 0;
    private Word currentWord;
    private int currentTeamIndex = 0;
    private int turnGuessedCount;
    private int turnTimeLeft;

    private Game game;
    private Team currentTeam;
    private ArrayList<Word> wordsUsed;

    private float dY = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initializeGame();
        setupLayout();
        startTurn();
    }

    private void initializeGame() {
        Intent intent = getIntent();
        int timeLimit = intent.getIntExtra("timeLimit", 60);
        int winningPoints = intent.getIntExtra("winningPoints", 30);
        String difficulty = intent.getStringExtra("difficulty");
        ArrayList<Team> teams = (ArrayList<Team>) intent.getSerializableExtra("teams");
        Collections.shuffle(teams);

        game = new Game(teams, timeLimit, winningPoints, difficulty, this);
    }

    private void setupLayout() {
        initializeLayout();
        setupZones();
        setupCardSwipe();
        showCurrentWord();
    }

    private void initializeLayout() {
        tvWordCard = findViewById(R.id.wordCard);
        tvTimeLeft = findViewById(R.id.tvTimeLeft);
        tvTurnScore = findViewById(R.id.tvTurnScore);
    }

    private void startTurn() {
        currentTeamIndex = currentTeamIndex == game.teamsList.size() ? 0 : currentTeamIndex;
        currentTeam = game.teamsList.get(currentTeamIndex);

        if(currentTeamIndex == 0 && checkForWinner()) {
            endGame(currentTeam);
        }
        else {
            wordsUsed = new ArrayList<>();
            turnGuessedCount = 0;
            updateTurnScore(0, 0);
            showStartTurnDialog();
            currentTeamIndex++;
        }
    }

    private boolean checkForWinner() {
        ArrayList<Team> teams = new ArrayList<>(game.teamsList);
        int maxTeamPoints = Integer.MIN_VALUE;
        int teamsWithMaxPointsCounter = 0;

        for(int i = 0; i < teams.size(); i++) {
            int teamScore = teams.get(i).getScore();

            if(teamScore >= game.pointsToWin) {
                if(teamScore > maxTeamPoints) {
                    maxTeamPoints = teamScore;
                    teamsWithMaxPointsCounter = 1;
                }
                else if(teamScore == maxTeamPoints) {
                    teamsWithMaxPointsCounter++;
                }
            }
        }

        return teamsWithMaxPointsCounter == 1;
    }

    private void endGame(Team gameWinner) {
    }

    @SuppressLint("ResourceType")
    private void updateTurnScore(int guessed, int skipped) {
        String text = guessed + " / " + skipped;

        Spannable spannable = new SpannableString(text);

        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor(getString(R.color.correctGreen))),
                0,
                String.valueOf(guessed).length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor(getString(R.color.white))),
                String.valueOf(guessed).length(),
                String.valueOf(guessed).length() + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor(getString(R.color.wrongRed))),
                String.valueOf(guessed).length() + 3,
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvTurnScore.setText(spannable);
    }

    @SuppressLint("ResourceType")
    private void setupZones() {
        findViewById(R.id.topZone).setBackground(new DashedZoneDrawable(Color.parseColor(getString(R.color.correctGreen)), 6f));
        findViewById(R.id.bottomZone).setBackground(new DashedZoneDrawable(Color.parseColor(getString(R.color.wrongRed)), 6f));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showCurrentWord() {
        if (currentWordIndex < game.wordsList.size()) {
            currentWord = new Word(game.wordsList.get(currentWordIndex), false);
            tvWordCard.setText(currentWord.getText());
            tvWordCard.setTranslationY(0);
        } else {
            tvWordCard.setText("Слова закінчилися!");
            tvWordCard.setOnTouchListener((v, event) -> false);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCardSwipe() {
        tvWordCard.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dY = v.getY() - event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float newY = event.getRawY() + dY;
                    v.setY(newY);
                    return true;

                case MotionEvent.ACTION_UP:
                    if (isViewOverlapping(tvWordCard, findViewById(R.id.topZone))) {
                        animateCardSwipe(true);
                    } else if (isViewOverlapping(tvWordCard, findViewById(R.id.bottomZone))) {
                        animateCardSwipe(false);
                    } else {
                        tvWordCard.animate().translationY(0f).setDuration(200).start();
                    }
                    return true;
            }
            return false;
        });
    }

    private boolean isViewOverlapping(View view1, View view2) {
        int[] loc1 = new int[2];
        int[] loc2 = new int[2];

        view1.getLocationOnScreen(loc1);
        view2.getLocationOnScreen(loc2);

        int left1 = loc1[0];
        int top1 = loc1[1];
        int right1 = left1 + view1.getWidth();
        int bottom1 = top1 + view1.getHeight();

        int left2 = loc2[0];
        int top2 = loc2[1];
        int right2 = left2 + view2.getWidth();
        int bottom2 = top2 + view2.getHeight();

        return !(left1 > right2 || right1 < left2 || top1 > bottom2 || bottom1 < top2);
    }

    private void animateCardSwipe(boolean toTopZone) {
        View zone = findViewById(toTopZone ? R.id.topZone : R.id.bottomZone);

        int[] cardLocation = new int[2];
        int[] zoneLocation = new int[2];
        tvWordCard.getLocationOnScreen(cardLocation);
        zone.getLocationOnScreen(zoneLocation);

        float cardCenterY = cardLocation[1] + tvWordCard.getHeight() / 2f;
        float zoneCenterY = zoneLocation[1] + zone.getHeight() / 2f;

        float deltaY = zoneCenterY - cardCenterY;

        int swipeAnimationDuration = 300;
        tvWordCard.animate()
                .translationYBy(deltaY)
                .setDuration(swipeAnimationDuration)
                .withEndAction(() -> {
                    currentWordIndex++;
                    tvWordCard.postDelayed(() -> {
                        tvWordCard.setTranslationY(0f);
                        showCurrentWord();
                    }, 100);
                })
                .start();

        if(toTopZone) {
            currentTeam.incrementScore();
            turnGuessedCount++;
            currentWord.setGuessed(true);

        } else {
            currentTeam.decrementScore();
        }

        wordsUsed.add(currentWord);

        if (isTurnEnded()) {
            tvTimeLeft.postDelayed(() -> {
                tvTimeLeft.setAlpha(0);
                tvTurnScore.setAlpha(0);
                showTurnResultsDialog();
            }, swipeAnimationDuration);
        } else {
            updateTurnScore(turnGuessedCount, wordsUsed.size() - turnGuessedCount);
        }
    }

    private boolean isTurnEnded() {
        return turnTimeLeft == 0;
    }

    private void startTimer(int seconds) {
        new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                turnTimeLeft = (int) (millisUntilFinished / 1000);
                tvTimeLeft.setText(getResources().getString(R.string.time_left, turnTimeLeft));
            }

            @Override
            public void onFinish() {
                tvTimeLeft.setText("Останнє слово!");
            }
        }.start();
    }

    private void showStartTurnDialog() {
        View dialogView = DialogUtils.inflateDialogView(this, R.layout.dialog_start_turn);

        TextView tvTeamNameDialog = dialogView.findViewById(R.id.tvTeamName);
        AppCompatButton btnStartTurn = dialogView.findViewById(R.id.btnStartTurn);

        String currentTeamName = currentTeam.getName();
        tvTeamNameDialog.setText(getResources().getString(R.string.team_turn, currentTeamName));

        AlertDialog dialog = DialogUtils.buildDialog(this, dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        btnStartTurn.setOnClickListener(v -> {
            dialog.dismiss();
            tvTimeLeft.setAlpha(1f);
            tvTurnScore.setAlpha(1f);
            startTimer(game.turnTime);
        });

        dialog.show();
        DialogUtils.setDialogWidth(dialog, this, 320);
    }

    private void showTurnResultsDialog() {
        View dialogView = DialogUtils.inflateDialogView(this, R.layout.dialog_turn_result);

        RecyclerView rvUsedWords = dialogView.findViewById(R.id.rvUsedWords);
        AppCompatButton btnCloseScore = dialogView.findViewById(R.id.btnCloseScore);
        TextView tvTeamResults = dialogView.findViewById(R.id.tvTeamResults);

        tvTeamResults.setText(
                getString(R.string.dialog_team_results_with_name, currentTeam.getName())
        );

        TurnResultsAdapter adapter = new TurnResultsAdapter(this, wordsUsed, currentTeam);
        rvUsedWords.setAdapter(adapter);
        rvUsedWords.setLayoutManager(new LinearLayoutManager(this));
        rvUsedWords.setHasFixedSize(true);

        AlertDialog dialog = DialogUtils.buildDialog(this, dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        btnCloseScore.setOnClickListener(v -> {
            dialog.dismiss();
            startTurn();
        });

        dialog.show();
        resizeTurnResultsDialog(dialog);
    }

    private void resizeTurnResultsDialog(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            DisplayMetrics metrics = new DisplayMetrics();
            window.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels * 0.86);

            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
    }
}