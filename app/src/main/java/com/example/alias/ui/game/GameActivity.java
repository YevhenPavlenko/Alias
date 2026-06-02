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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Game;
import com.example.alias.model.Team;
import com.example.alias.model.Word;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.ui.game.adapter.TurnResultsAdapter;
import com.example.alias.ui.score.ScoreActivity;
import com.example.alias.util.DashedZoneDrawable;
import com.example.alias.util.DialogUtils;
import com.example.alias.util.GameFeedbackManager;
import com.example.alias.util.GameHistoryDbHelper;
import com.example.alias.model.GameWordResult;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Collections;

public class GameActivity extends BaseActivity {

    private TextView tvWordCard;
    private TextView tvTimeLeft;
    private TextView tvTurnScore;
    private int currentWordIndex = 0;
    private Word currentWord;
    private int currentTeamIndex = 0;
    private int turnTimeLeft;
    private long turnTimeLeftMillis;

    private Game game;
    private Team currentTeam;
    private ArrayList<Word> wordsUsed;

    private float dY = 0f;

    private boolean isCardSwipeInProgress = false;
    private boolean isTurnResultsDialogShowing = false;
    private GameFeedbackManager feedbackManager;
    private CountDownTimer turnTimer;
    private AlertDialog exitGameDialog;
    private boolean isTurnTimerRunning = false;
    private boolean isExitGameDialogShowing = false;

    private String gameDifficulty = "easy";

    private long gameStartedAtMillis;
    private boolean isGameSavedToHistory = false;

    private final ArrayList<GameWordResult> completedGameWords = new ArrayList<>();
    private int gameWordOrder = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        feedbackManager = new GameFeedbackManager(this);

        initializeGame();
        setupLayout();
        startTurn();
        setupBackPressHandler();
    }

    @SuppressWarnings("unchecked")
    private void initializeGame() {
        Intent intent = getIntent();

        int timeLimit = intent.getIntExtra("timeLimit", 60);
        int winningPoints = intent.getIntExtra("winningPoints", 30);

        gameDifficulty = intent.getStringExtra("difficulty");
        if (gameDifficulty == null) {
            gameDifficulty = "easy";
        }

        ArrayList<Team> teams = (ArrayList<Team>) intent.getSerializableExtra("teams");

        if (teams == null || teams.isEmpty()) {
            teams = new ArrayList<>();
            teams.add(new Team("Команда 1"));
            teams.add(new Team("Команда 2"));
        }

        Collections.shuffle(teams);

        game = new Game(teams, timeLimit, winningPoints, gameDifficulty, this);
        gameStartedAtMillis = System.currentTimeMillis();
    }

    private void setupLayout() {
        initializeLayout();
        setupZones();
        setupCardSwipe();
        hideWordCardUntilRoundStarts();
    }

    private void initializeLayout() {
        tvWordCard = findViewById(R.id.wordCard);
        tvTimeLeft = findViewById(R.id.tvTimeLeft);
        tvTurnScore = findViewById(R.id.tvTurnScore);
    }

    private void startTurn() {
        currentTeamIndex = currentTeamIndex == game.teamsList.size() ? 0 : currentTeamIndex;
        currentTeam = game.teamsList.get(currentTeamIndex);

        isCardSwipeInProgress = false;
        isTurnResultsDialogShowing = false;
        hideWordCardUntilRoundStarts();

        if(currentTeamIndex == 0 && checkForWinner()) {
            endGame();
        } else {
            wordsUsed = new ArrayList<>();
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

    private void endGame() {
        cancelTurnTimer();

        saveCompletedGameToHistory();

        Intent intent = new Intent(this, ScoreActivity.class);

        intent.putExtra("teams", new ArrayList<>(game.teamsList));
        intent.putExtra("timeLimit", game.turnTime);
        intent.putExtra("winningPoints", game.pointsToWin);
        intent.putExtra("difficulty", gameDifficulty);

        startActivity(intent);
        finish();
    }

    private void saveCompletedGameToHistory() {
        if (isGameSavedToHistory) {
            return;
        }

        isGameSavedToHistory = true;

        long finishedAtMillis = System.currentTimeMillis();
        long durationSeconds = Math.max(0, (finishedAtMillis - gameStartedAtMillis) / 1000L);

        Team winner = findWinnerTeam();

        String winnerTeamName = winner == null ? "" : winner.getName();

        GameHistoryDbHelper dbHelper = new GameHistoryDbHelper(this);

        dbHelper.saveCompletedGame(
                finishedAtMillis,
                durationSeconds,
                game.turnTime,
                game.pointsToWin,
                gameDifficulty,
                winnerTeamName,
                game.teamsList,
                completedGameWords
        );
    }

    private Team findWinnerTeam() {
        if (game == null || game.teamsList == null || game.teamsList.isEmpty()) {
            return null;
        }

        Team winner = game.teamsList.get(0);

        for (Team team : game.teamsList) {
            if (team.getScore() > winner.getScore()) {
                winner = team;
            }
        }

        return winner;
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
        tvWordCard.animate().cancel();
        tvWordCard.setTranslationY(0f);
        tvWordCard.setVisibility(View.VISIBLE);
        tvWordCard.setAlpha(1f);

        if (currentWordIndex < game.wordsList.size()) {
            currentWord = new Word(String.valueOf(game.wordsList.get(currentWordIndex)), false);
            tvWordCard.setText(currentWord.getText());
            tvWordCard.setEnabled(true);
        } else {
            currentWord = null;
            tvWordCard.setText("Слова закінчилися!");
            tvWordCard.setEnabled(false);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCardSwipe() {
        tvWordCard.setOnTouchListener((v, event) -> {
            if (isCardSwipeInProgress || isTurnResultsDialogShowing || isExitGameDialogShowing) {
                return true;
            }

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().cancel();
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
                        tvWordCard.animate()
                                .translationY(0f)
                                .setDuration(200)
                                .start();
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    tvWordCard.animate()
                            .translationY(0f)
                            .setDuration(200)
                            .start();
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
        if (isCardSwipeInProgress || isTurnResultsDialogShowing || currentWord == null) {
            return;
        }

        isCardSwipeInProgress = true;
        tvWordCard.setEnabled(false);
        tvWordCard.animate().cancel();

        View zone = findViewById(toTopZone ? R.id.topZone : R.id.bottomZone);

        int[] cardLocation = new int[2];
        int[] zoneLocation = new int[2];

        tvWordCard.getLocationOnScreen(cardLocation);
        zone.getLocationOnScreen(zoneLocation);

        float cardCenterY = cardLocation[1] + tvWordCard.getHeight() / 2f;
        float zoneCenterY = zoneLocation[1] + zone.getHeight() / 2f;
        float deltaY = zoneCenterY - cardCenterY;

        int swipeAnimationDuration = 150;

        Word swipedWord = currentWord;

        boolean isLastWordSwipe = isTurnEnded();

        swipedWord.setLastWord(isLastWordSwipe);
        swipedWord.setGuessed(toTopZone);
        swipedWord.setAssignedTeam(null);

        wordsUsed.add(swipedWord);

        if (feedbackManager != null) {
            if (toTopZone) {
                feedbackManager.playCorrectWord();
            } else {
                feedbackManager.playSkippedWord();
            }
        }

        if (!swipedWord.isLastWord()) {
            applyWordScore(swipedWord);
        }

        refreshTurnScoreFromWordsUsed();

        if (swipedWord.isLastWord()) {
            isTurnResultsDialogShowing = true;
        }

        tvWordCard.animate()
                .translationYBy(deltaY)
                .setDuration(swipeAnimationDuration)
                .withEndAction(() -> {
                    currentWordIndex++;

                    tvWordCard.postDelayed(() -> {
                        tvWordCard.setTranslationY(0f);

                        boolean shouldFinishTurn = swipedWord.isLastWord() || isTurnEnded();

                        if (shouldFinishTurn) {
                            if (swipedWord.isLastWord() && swipedWord.isGuessed()) {
                                hideWordCardUntilRoundStarts();

                                tvTimeLeft.setAlpha(0f);
                                tvTurnScore.setAlpha(0f);

                                showLastWordTeamDialog(
                                        swipedWord,
                                        this::finishTurnAndShowResults
                                );
                            } else {
                                finishTurnAndShowResults();
                            }
                        } else {
                            showCurrentWord();

                            isCardSwipeInProgress = false;
                            tvWordCard.setEnabled(true);
                        }
                    }, 100);
                })
                .start();
    }

    private void applyWordScore(Word word) {
        if (word == null) {
            return;
        }

        if (word.isLastWord()) {
            if (word.isGuessed() && word.getAssignedTeam() != null) {
                word.getAssignedTeam().incrementScore();
            }

            return;
        }

        if (currentTeam == null) {
            return;
        }

        if (word.isGuessed()) {
            currentTeam.incrementScore();
        } else {
            currentTeam.decrementScore();
        }
    }

    private void revertWordScore(Word word) {
        if (word == null) {
            return;
        }

        if (word.isLastWord()) {
            if (word.isGuessed() && word.getAssignedTeam() != null) {
                word.getAssignedTeam().decrementScore();
            }

            return;
        }

        if (currentTeam == null) {
            return;
        }

        if (word.isGuessed()) {
            currentTeam.decrementScore();
        } else {
            currentTeam.incrementScore();
        }
    }

    private void changeWordResult(Word word, boolean guessed, Team assignedTeam) {
        if (word == null) {
            return;
        }

        revertWordScore(word);

        word.setGuessed(guessed);

        if (word.isLastWord()) {
            word.setAssignedTeam(guessed ? assignedTeam : null);
        } else {
            word.setAssignedTeam(null);
        }

        applyWordScore(word);
        refreshTurnScoreFromWordsUsed();
    }

    private void refreshTurnScoreFromWordsUsed() {
        int guessed = 0;
        int skipped = 0;

        if (wordsUsed != null) {
            for (Word word : wordsUsed) {
                if (word.isGuessed()) {
                    guessed++;
                } else {
                    skipped++;
                }
            }
        }

        updateTurnScore(guessed, skipped);
    }

    private void showLastWordTeamDialog(Word word, Runnable afterSelection) {
        ArrayList<Team> availableTeams = new ArrayList<>();

        if (game != null && game.teamsList != null) {
            availableTeams.addAll(game.teamsList);
        }

        if (availableTeams.isEmpty() && currentTeam != null) {
            availableTeams.add(currentTeam);
        }

        if (availableTeams.isEmpty()) {
            changeWordResult(word, false, null);

            if (afterSelection != null) {
                afterSelection.run();
            }

            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_last_word_team);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvLastWordDialogWord = dialog.findViewById(R.id.tvLastWordDialogWord);
        LinearLayout teamsContainer = dialog.findViewById(R.id.teamsContainer);
        AppCompatButton btnApplyPoint = dialog.findViewById(R.id.btnApplyLastWordPoint);
        AppCompatButton btnNoPoint = dialog.findViewById(R.id.btnNoLastWordPoint);

        tvLastWordDialogWord.setText(
                getString(R.string.last_word_dialog_word, word.getText())
        );

        final Team[] selectedTeam = new Team[1];

        if (word.isGuessed() && word.getAssignedTeam() != null) {
            selectedTeam[0] = word.getAssignedTeam();
        }

        ArrayList<View> optionViews = new ArrayList<>();
        ArrayList<Team> optionTeams = new ArrayList<>();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Team team : availableTeams) {
            View optionView = inflater.inflate(
                    R.layout.item_last_word_team_option,
                    teamsContainer,
                    false
            );

            TextView tvTeamName = optionView.findViewById(R.id.tvLastWordTeamName);
            TextView tvTeamScore = optionView.findViewById(R.id.tvLastWordTeamScore);

            tvTeamName.setText(team.getName());
            tvTeamScore.setText(getString(R.string.last_word_team_score, team.getScore()));

            optionView.setOnClickListener(v -> {
                selectedTeam[0] = team;

                updateLastWordTeamOptions(
                        optionViews,
                        optionTeams,
                        selectedTeam[0],
                        btnApplyPoint
                );
            });

            teamsContainer.addView(optionView);

            optionViews.add(optionView);
            optionTeams.add(team);
        }

        updateLastWordTeamOptions(
                optionViews,
                optionTeams,
                selectedTeam[0],
                btnApplyPoint
        );

        btnApplyPoint.setOnClickListener(v -> {
            if (selectedTeam[0] == null) {
                return;
            }

            changeWordResult(word, true, selectedTeam[0]);

            dialog.dismiss();

            if (afterSelection != null) {
                afterSelection.run();
            }
        });

        btnNoPoint.setOnClickListener(v -> {
            changeWordResult(word, false, null);

            dialog.dismiss();

            if (afterSelection != null) {
                afterSelection.run();
            }
        });

        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90f),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void updateLastWordTeamOptions(
            ArrayList<View> optionViews,
            ArrayList<Team> optionTeams,
            Team selectedTeam,
            AppCompatButton btnApplyPoint
    ) {
        for (int i = 0; i < optionViews.size(); i++) {
            View optionView = optionViews.get(i);
            Team team = optionTeams.get(i);

            boolean isSelected = isSameTeam(team, selectedTeam);

            RadioButton rbTeamSelected = optionView.findViewById(R.id.rbTeamSelected);
            TextView tvTeamChoice = optionView.findViewById(R.id.tvLastWordTeamChoice);

            optionView.setSelected(isSelected);
            rbTeamSelected.setChecked(isSelected);

            if (isSelected) {
                tvTeamChoice.setText(R.string.last_word_selected_team);
            } else {
                tvTeamChoice.setText(R.string.last_word_tap_to_select);
            }
        }

        if (selectedTeam == null) {
            btnApplyPoint.setEnabled(false);
            btnApplyPoint.setAlpha(0.55f);
            btnApplyPoint.setText(R.string.last_word_apply_disabled);
        } else {
            btnApplyPoint.setEnabled(true);
            btnApplyPoint.setAlpha(1f);
            btnApplyPoint.setText(
                    getString(R.string.last_word_apply_to, selectedTeam.getName())
            );
        }
    }

    private boolean isSameTeam(Team firstTeam, Team secondTeam) {
        if (firstTeam == secondTeam) {
            return true;
        }

        if (firstTeam == null || secondTeam == null) {
            return false;
        }

        return firstTeam.getName().equalsIgnoreCase(secondTeam.getName());
    }

    private void finishTurnAndShowResults() {
        isTurnResultsDialogShowing = true;
        isCardSwipeInProgress = false;

        hideWordCardUntilRoundStarts();

        tvTimeLeft.setAlpha(0f);
        tvTurnScore.setAlpha(0f);

        showTurnResultsDialog();
    }

    private boolean isTurnEnded() {
        return turnTimeLeft <= 0;
    }

    private void startTimer(int seconds) {
        startTimerFromMillis(seconds * 1000L);
    }

    private void startTimerFromMillis(long millis) {
        cancelTurnTimer();

        turnTimeLeftMillis = millis;
        turnTimeLeft = millisToSecondsCeil(turnTimeLeftMillis);

        tvTimeLeft.setText(getResources().getString(R.string.time_left, turnTimeLeft));

        isTurnTimerRunning = true;

        turnTimer = new CountDownTimer(turnTimeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                turnTimeLeftMillis = millisUntilFinished;
                turnTimeLeft = millisToSecondsCeil(millisUntilFinished);

                tvTimeLeft.setText(getResources().getString(R.string.time_left, turnTimeLeft));

                if (feedbackManager == null) {
                    return;
                }

                if (turnTimeLeft == 10) {
                    feedbackManager.playTenSecondsWarning();
                } else if (turnTimeLeft == 3) {
                    feedbackManager.playCountdownTick();
                }
            }

            @Override
            public void onFinish() {
                isTurnTimerRunning = false;
                turnTimer = null;

                turnTimeLeftMillis = 0L;
                turnTimeLeft = 0;

                tvTimeLeft.setText(getString(R.string.last_word_timer_text));

                if (feedbackManager != null) {
                    feedbackManager.playTimeUpLastWord();
                }
            }
        };

        turnTimer.start();
    }

    private int millisToSecondsCeil(long millis) {
        return (int) Math.ceil(millis / 1000.0);
    }

    private void cancelTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }

        isTurnTimerRunning = false;
    }

    private void pauseTurnTimer() {
        if (!isTurnTimerRunning || turnTimer == null) {
            return;
        }

        turnTimer.cancel();
        turnTimer = null;
        isTurnTimerRunning = false;

        turnTimeLeft = millisToSecondsCeil(turnTimeLeftMillis);
        tvTimeLeft.setText(getResources().getString(R.string.time_left, turnTimeLeft));
    }

    private void resumeTurnTimerIfNeeded() {
        if (isTurnTimerRunning || turnTimeLeftMillis <= 0 || isTurnResultsDialogShowing) {
            return;
        }

        startTimerFromMillis(turnTimeLeftMillis);
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

            isCardSwipeInProgress = false;
            isTurnResultsDialogShowing = false;

            showCurrentWord();
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

        final TurnResultsAdapter[] adapterHolder = new TurnResultsAdapter[1];

        TurnResultsAdapter adapter = new TurnResultsAdapter(
                this,
                wordsUsed,
                new TurnResultsAdapter.OnWordResultActionListener() {
                    @Override
                    public void onToggleNormalWord(Word word, int position) {
                        changeWordResult(word, !word.isGuessed(), null);

                        if (adapterHolder[0] != null && position != RecyclerView.NO_POSITION) {
                            adapterHolder[0].notifyItemChanged(position);
                        }
                    }

                    @Override
                    public void onToggleLastWord(Word word, int position) {
                        if (word.isGuessed()) {
                            changeWordResult(word, false, null);

                            if (adapterHolder[0] != null && position != RecyclerView.NO_POSITION) {
                                adapterHolder[0].notifyItemChanged(position);
                            }
                        } else {
                            showLastWordTeamDialog(word, () -> {
                                if (adapterHolder[0] != null && position != RecyclerView.NO_POSITION) {
                                    adapterHolder[0].notifyItemChanged(position);
                                }
                            });
                        }
                    }

                    @Override
                    public void onChangeLastWordTeam(Word word, int position) {
                        showLastWordTeamDialog(word, () -> {
                            if (adapterHolder[0] != null && position != RecyclerView.NO_POSITION) {
                                adapterHolder[0].notifyItemChanged(position);
                            }
                        });
                    }
                }
        );


        LinearLayoutManager usedWordsLayoutManager = new LinearLayoutManager(this);
        usedWordsLayoutManager.setReverseLayout(false);
        usedWordsLayoutManager.setStackFromEnd(true);

        rvUsedWords.setLayoutManager(usedWordsLayoutManager);
        rvUsedWords.setHasFixedSize(true);

        adapterHolder[0] = adapter;
        rvUsedWords.setAdapter(adapter);

        AlertDialog dialog = DialogUtils.buildDialog(this, dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        btnCloseScore.setOnClickListener(v -> {
            saveCurrentTurnWordsToMemory();
            dialog.dismiss();

            isTurnResultsDialogShowing = false;
            isCardSwipeInProgress = false;
            tvWordCard.setEnabled(true);

            startTurn();
        });

        dialog.show();
        resizeTurnResultsDialog(dialog);
        scrollRoundResultsToBottom(rvUsedWords);
    }

    private void saveCurrentTurnWordsToMemory() {
        if (wordsUsed == null || currentTeam == null) {
            return;
        }

        for (Word word : wordsUsed) {
            completedGameWords.add(new GameWordResult(
                    currentTeam,
                    word.getAssignedTeam(),
                    word.getText(),
                    word.isGuessed(),
                    word.isLastWord(),
                    gameWordOrder++
            ));
        }
    }

    private void scrollRoundResultsToBottom(RecyclerView recyclerView) {
        if (recyclerView == null || recyclerView.getAdapter() == null) {
            return;
        }

        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();

            if (adapter == null || adapter.getItemCount() == 0) {
                return;
            }

            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        });
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

    private void hideWordCardUntilRoundStarts() {
        tvWordCard.animate().cancel();
        tvWordCard.setText("");
        tvWordCard.setTranslationY(0f);
        tvWordCard.setVisibility(View.INVISIBLE);
        tvWordCard.setEnabled(false);
        currentWord = null;
    }

    private void showExitGameConfirmationDialog() {
        if (exitGameDialog != null && exitGameDialog.isShowing()) {
            return;
        }

        pauseTurnTimer();
        isExitGameDialogShowing = true;

        View dialogView = DialogUtils.inflateDialogView(this, R.layout.dialog_exit_game);

        AppCompatButton btnCancelExit = dialogView.findViewById(R.id.btnCancelExit);
        AppCompatButton btnConfirmExit = dialogView.findViewById(R.id.btnConfirmExit);

        exitGameDialog = DialogUtils.buildDialog(this, dialogView);
        exitGameDialog.setCancelable(true);
        exitGameDialog.setCanceledOnTouchOutside(true);

        exitGameDialog.setOnDismissListener(dialog -> {
            boolean shouldResumeTimer =
                    isExitGameDialogShowing
                            && !isFinishing()
                            && !isDestroyed();

            isExitGameDialogShowing = false;

            if (shouldResumeTimer) {
                resumeTurnTimerIfNeeded();
            }
        });

        btnCancelExit.setOnClickListener(v -> {
            if (exitGameDialog != null) {
                exitGameDialog.dismiss();
            }
        });

        btnConfirmExit.setOnClickListener(v -> {
            isExitGameDialogShowing = false;
            cancelTurnTimer();

            if (exitGameDialog != null) {
                exitGameDialog.dismiss();
            }

            finish();
        });

        exitGameDialog.show();
        DialogUtils.setDialogWidth(exitGameDialog, this, 320);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitGameConfirmationDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        cancelTurnTimer();

        if (exitGameDialog != null && exitGameDialog.isShowing()) {
            exitGameDialog.dismiss();
        }

        if (feedbackManager != null) {
            feedbackManager.release();
            feedbackManager = null;
        }

        super.onDestroy();
    }
}