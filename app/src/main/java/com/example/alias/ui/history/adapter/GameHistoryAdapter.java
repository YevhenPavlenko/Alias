package com.example.alias.ui.history.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.util.GameHistoryDbHelper;
import com.example.alias.model.history.HistoryGame;
import com.example.alias.model.history.HistoryTeam;
import com.example.alias.model.history.HistoryWord;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.GameHistoryViewHolder> {

    private final Context context;
    private final GameHistoryDbHelper dbHelper;
    private final List<HistoryGame> games;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public GameHistoryAdapter(
            Context context,
            GameHistoryDbHelper dbHelper,
            List<HistoryGame> games
    ) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.games = games;
    }

    @NonNull
    @Override
    public GameHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_history_game, parent, false);

        return new GameHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameHistoryViewHolder holder, int position) {
        HistoryGame game = games.get(position);

        holder.tvGameTitle.setText(
                context.getString(R.string.history_game_number, game.getId())
        );

        holder.tvGameDate.setText(
                dateFormat.format(new Date(game.getFinishedAtMillis()))
        );

        holder.tvWinner.setText(
                context.getString(R.string.history_winner, game.getWinnerTeamName())
        );

        holder.tvGameDuration.setText(
                context.getString(
                        R.string.history_duration,
                        formatDuration(game.getDurationSeconds())
                )
        );

        holder.tvRoundTime.setText(
                context.getString(R.string.history_round_time, game.getRoundTime())
        );

        holder.tvPointsToWin.setText(
                context.getString(R.string.history_points_to_win, game.getPointsToWin())
        );

        holder.tvDifficulty.setText(
                context.getString(
                        R.string.history_difficulty,
                        formatDifficulty(game.getDifficulty())
                )
        );

        holder.layoutGameDetails.setVisibility(game.isExpanded() ? View.VISIBLE : View.GONE);
        holder.tvGameExpandIcon.setText(
                game.isExpanded() ? R.string.arrow_collapse : R.string.arrow_expand
        );

        holder.teamsContainer.setVisibility(game.isTeamsExpanded() ? View.VISIBLE : View.GONE);
        holder.tvTeamsExpandIcon.setText(
                game.isTeamsExpanded() ? R.string.arrow_collapse : R.string.arrow_expand
        );

        holder.rowGameHeader.setOnClickListener(v -> {
            game.setExpanded(!game.isExpanded());
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.rowTeamsHeader.setOnClickListener(v -> {
            if (game.getTeams() == null) {
                game.setTeams(dbHelper.getTeamsForGame(game.getId()));
            }

            game.setTeamsExpanded(!game.isTeamsExpanded());
            notifyItemChanged(holder.getAdapterPosition());
        });

        renderTeams(holder, game);
    }

    private void renderTeams(GameHistoryViewHolder holder, HistoryGame game) {
        holder.teamsContainer.removeAllViews();

        if (!game.isTeamsExpanded()) {
            return;
        }

        List<HistoryTeam> teams = game.getTeams();

        if (teams == null) {
            teams = dbHelper.getTeamsForGame(game.getId());
            game.setTeams(teams);
        }

        LayoutInflater inflater = LayoutInflater.from(context);

        for (HistoryTeam team : teams) {
            View teamView = inflater.inflate(
                    R.layout.item_history_team,
                    holder.teamsContainer,
                    false
            );

            TextView tvTeamPlace = teamView.findViewById(R.id.tvHistoryTeamPlace);
            TextView tvTeamName = teamView.findViewById(R.id.tvHistoryTeamName);
            TextView tvTeamScore = teamView.findViewById(R.id.tvHistoryTeamScore);
            TextView tvTeamExpandIcon = teamView.findViewById(R.id.tvTeamExpandIcon);
            View rowTeamHeader = teamView.findViewById(R.id.rowHistoryTeamHeader);
            LinearLayout wordsContainer = teamView.findViewById(R.id.wordsContainer);

            tvTeamPlace.setText(context.getString(R.string.history_team_place, team.getPlace()));
            tvTeamName.setText(team.getName());
            tvTeamScore.setText(
                    context.getString(R.string.history_team_score, team.getScore())
            );


            tvTeamExpandIcon.setText(
                    team.isExpanded() ? R.string.arrow_collapse : R.string.arrow_expand
            );
            wordsContainer.setVisibility(team.isExpanded() ? View.VISIBLE : View.GONE);

            rowTeamHeader.setOnClickListener(v -> {
                if (team.getWords() == null) {
                    team.setWords(dbHelper.getWordsForTeam(game.getId(), team.getId()));
                }

                team.setExpanded(!team.isExpanded());
                notifyItemChanged(holder.getAdapterPosition());
            });

            renderWords(wordsContainer, team);

            holder.teamsContainer.addView(teamView);
        }
    }

    private void renderWords(LinearLayout wordsContainer, HistoryTeam team) {
        wordsContainer.removeAllViews();

        if (!team.isExpanded()) {
            return;
        }

        List<HistoryWord> words = team.getWords();

        if (words == null || words.isEmpty()) {
            TextView emptyWords = new TextView(context);
            emptyWords.setText(R.string.history_no_words_for_team);
            emptyWords.setTextColor(ContextCompat.getColor(context, R.color.white));
            emptyWords.setTextSize(13f);
            emptyWords.setPadding(dp(8), dp(8), dp(8), dp(8));
            wordsContainer.addView(emptyWords);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);

        for (HistoryWord word : words) {
            View wordView = inflater.inflate(
                    R.layout.item_history_word,
                    wordsContainer,
                    false
            );

            MaterialCardView cardHistoryWord = wordView.findViewById(R.id.cardHistoryWord);
            TextView tvHistoryWordText = wordView.findViewById(R.id.tvHistoryWordText);
            TextView tvHistoryWordStatus = wordView.findViewById(R.id.tvHistoryWordStatus);

            tvHistoryWordText.setText(word.getText());
            applyWordStyle(cardHistoryWord, tvHistoryWordText, tvHistoryWordStatus, word);

            wordsContainer.addView(wordView);
        }
    }

    private void applyWordStyle(
            MaterialCardView card,
            TextView tvWord,
            TextView tvStatus,
            HistoryWord word
    ) {
        if (word.isLastWord() && word.isGuessed()) {
            card.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.last_word_success_bg)
            );
            card.setStrokeColor(
                    ContextCompat.getColor(context, R.color.last_word_success_stroke)
            );
            tvWord.setTextColor(ContextCompat.getColor(context, R.color.last_word_text));
            tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.last_word_success_stroke)
            );
            tvStatus.setText(R.string.history_word_last_guessed);
            return;
        }

        if (word.isLastWord()) {
            card.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.last_word_no_point_bg)
            );
            card.setStrokeColor(
                    ContextCompat.getColor(context, R.color.last_word_no_point_stroke)
            );
            tvWord.setTextColor(ContextCompat.getColor(context, R.color.last_word_text));
            tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.last_word_no_point_stroke)
            );
            tvStatus.setText(R.string.history_word_last_skipped);
            return;
        }

        if (word.isGuessed()) {
            card.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.answer_correct_bg)
            );
            card.setStrokeColor(
                    ContextCompat.getColor(context, R.color.answer_correct_text)
            );
            tvWord.setTextColor(
                    ContextCompat.getColor(context, R.color.answer_correct_text)
            );
            tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.answer_correct_text)
            );
            tvStatus.setText(R.string.history_word_guessed);
        } else {
            card.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.answer_wrong_bg)
            );
            card.setStrokeColor(
                    ContextCompat.getColor(context, R.color.answer_wrong_text)
            );
            tvWord.setTextColor(
                    ContextCompat.getColor(context, R.color.answer_wrong_text)
            );
            tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.answer_wrong_text)
            );
            tvStatus.setText(R.string.history_word_skipped);
        }
    }

    private String formatDuration(long durationSeconds) {
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;

        if (minutes <= 0) {
            return seconds + " сек";
        }

        return minutes + " хв " + seconds + " сек";
    }

    private String formatDifficulty(String difficulty) {
        if ("medium".equals(difficulty)) {
            return context.getString(R.string.medium);
        }

        if ("hard".equals(difficulty)) {
            return context.getString(R.string.hard);
        }

        return context.getString(R.string.easy);
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameHistoryViewHolder extends RecyclerView.ViewHolder {

        LinearLayout rowGameHeader;
        LinearLayout layoutGameDetails;
        LinearLayout rowTeamsHeader;
        LinearLayout teamsContainer;

        TextView tvGameTitle;
        TextView tvGameDate;
        TextView tvWinner;
        TextView tvGameDuration;
        TextView tvRoundTime;
        TextView tvPointsToWin;
        TextView tvDifficulty;
        TextView tvGameExpandIcon;
        TextView tvTeamsExpandIcon;

        GameHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            rowGameHeader = itemView.findViewById(R.id.rowGameHeader);
            layoutGameDetails = itemView.findViewById(R.id.layoutGameDetails);
            rowTeamsHeader = itemView.findViewById(R.id.rowTeamsHeader);
            teamsContainer = itemView.findViewById(R.id.teamsContainer);

            tvGameTitle = itemView.findViewById(R.id.tvGameTitle);
            tvGameDate = itemView.findViewById(R.id.tvGameDate);
            tvWinner = itemView.findViewById(R.id.tvWinner);
            tvGameDuration = itemView.findViewById(R.id.tvGameDuration);
            tvRoundTime = itemView.findViewById(R.id.tvRoundTime);
            tvPointsToWin = itemView.findViewById(R.id.tvPointsToWin);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvGameExpandIcon = itemView.findViewById(R.id.tvGameExpandIcon);
            tvTeamsExpandIcon = itemView.findViewById(R.id.tvTeamsExpandIcon);
        }
    }
}