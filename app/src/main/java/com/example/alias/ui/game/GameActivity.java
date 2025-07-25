package com.example.alias.ui.game;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.alias.R;
import com.example.alias.model.Game;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.util.DashedZoneDrawable;
import com.example.alias.util.WordUtils;

import java.util.List;

public class GameActivity extends BaseActivity {

    private TextView tvWordCard;
    private int currentWordIndex = 0;

    private Game game;

    private float dY = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initializeGame();

        tvWordCard = findViewById(R.id.wordCard);
        showCurrentWord();
        setupZones();
        setupCardSwipe();
    }

    private void initializeGame() {
        List<String> randomWords = WordUtils.getRandomWords(this, 100);
        game = new Game(randomWords.toArray(new String[0]));
        currentWordIndex = 0;
    }

    @SuppressLint("ResourceType")
    private void setupZones() {
        findViewById(R.id.topZone).setBackground(new DashedZoneDrawable(Color.parseColor(getString(R.color.correctGreen)), 6f));
        findViewById(R.id.bottomZone).setBackground(new DashedZoneDrawable(Color.parseColor(getString(R.color.wrongRed)), 6f));
    }

    private void showCurrentWord() {
        if (currentWordIndex < game.wordsList.size()) {
            tvWordCard.setText(game.wordsList.get(currentWordIndex));
            tvWordCard.setTranslationY(0);
        } else {
            tvWordCard.setText("Гру завершено!");
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

        tvWordCard.animate()
                .translationYBy(deltaY)
                .setDuration(300)
                .withEndAction(() -> {
                    currentWordIndex++;
                    tvWordCard.postDelayed(() -> {
                        tvWordCard.setTranslationY(0f);
                        showCurrentWord();
                    }, 300);
                })
                .start();
    }
}