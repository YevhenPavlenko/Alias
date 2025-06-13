package com.example.alias.ui.game;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.alias.R;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.util.DashedZoneDrawable;

import java.util.Arrays;
import java.util.List;

public class GameActivity extends BaseActivity {

    private TextView tvWordCard;

    private final List<String> words = Arrays.asList("Море", "Сонце", "Ракета", "Книга", "Піца", "Кава");
    private int currentWordIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvWordCard = findViewById(R.id.wordCard);
        showCurrentWord();

        View topZone = findViewById(R.id.topZone);
        topZone.setBackground(new DashedZoneDrawable(Color.parseColor("#70BE74"), 6f));

        View bottomZone = findViewById(R.id.bottomZone);
        bottomZone.setBackground(new DashedZoneDrawable(Color.parseColor("#E45656"), 6f));

    }

    private void showCurrentWord() {
        tvWordCard.setText(words.get(currentWordIndex));
    }
}
