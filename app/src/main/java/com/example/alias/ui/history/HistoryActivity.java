package com.example.alias.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.util.GameHistoryDbHelper;
import com.example.alias.model.history.HistoryGame;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.ui.history.adapter.GameHistoryAdapter;

import java.util.List;

public class HistoryActivity extends BaseActivity {

    private GameHistoryDbHelper dbHelper;
    private RecyclerView rvGameHistory;
    private TextView tvEmptyHistory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setupHeader(getString(R.string.history));
        setupViews();
        loadHistory();
    }

    private void setupViews() {
        dbHelper = new GameHistoryDbHelper(this);

        rvGameHistory = findViewById(R.id.rvGameHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        rvGameHistory.setLayoutManager(new LinearLayoutManager(this));
        rvGameHistory.setHasFixedSize(false);
    }

    private void loadHistory() {
        List<HistoryGame> games = dbHelper.getGames();

        if (games.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvGameHistory.setVisibility(View.GONE);
            return;
        }

        tvEmptyHistory.setVisibility(View.GONE);
        rvGameHistory.setVisibility(View.VISIBLE);

        rvGameHistory.setAdapter(new GameHistoryAdapter(this, dbHelper, games));
    }
}