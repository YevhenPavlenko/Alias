package com.example.alias.ui.game.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Team;
import com.example.alias.model.Word;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TurnResultsAdapter extends RecyclerView.Adapter<TurnResultsAdapter.TurnResultsViewHolder> {

    private final List<Word> words;
    private final Context context;
    private final Team team;

    public TurnResultsAdapter(Context context, List<Word> words, Team team) {
        this.context = context;
        this.words = words;
        this.team = team;
    }

    @NonNull
    @Override
    public TurnResultsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_used_word, parent, false);
        return new TurnResultsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TurnResultsViewHolder holder, int position) {
        Word word = words.get(position);
        Context context = holder.itemView.getContext();

        String formattedName = context.getString(R.string.word_format, word.getText());
        holder.tvUsedWord.setText(formattedName);

        applyWordState(holder, word, context);

        holder.itemView.setOnClickListener(v -> {
            word.setGuessed(!word.isGuessed());
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.btnIsGuessed.setOnClickListener(v -> {
            word.setGuessed(!word.isGuessed());
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    private void applyWordState(@NonNull TurnResultsViewHolder holder, Word word, Context context) {
        if (word.isGuessed()) {
            holder.cardUsedWord.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.answer_correct_bg)
            );
            holder.cardUsedWord.setStrokeColor(
                    ContextCompat.getColor(context, R.color.answer_correct_text)
            );
            holder.tvUsedWord.setTextColor(
                    ContextCompat.getColor(context, R.color.answer_correct_text)
            );
            holder.btnIsGuessed.setImageResource(R.drawable.ic_guessed);
            holder.btnIsGuessed.setColorFilter(
                    ContextCompat.getColor(context, R.color.answer_correct_icon)
            );
        } else {
            holder.cardUsedWord.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.answer_wrong_bg)
            );
            holder.cardUsedWord.setStrokeColor(
                    ContextCompat.getColor(context, R.color.answer_wrong_text)
            );
            holder.tvUsedWord.setTextColor(
                    ContextCompat.getColor(context, R.color.answer_wrong_text)
            );
            holder.btnIsGuessed.setImageResource(R.drawable.ic_unguessed);
            holder.btnIsGuessed.setColorFilter(
                    ContextCompat.getColor(context, R.color.answer_wrong_icon)
            );
        }
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public static class TurnResultsViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardUsedWord;
        TextView tvUsedWord;
        ImageButton btnIsGuessed;

        public TurnResultsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUsedWord = itemView.findViewById(R.id.cardUsedWord);
            tvUsedWord = itemView.findViewById(R.id.tvUsedWord);
            btnIsGuessed = itemView.findViewById(R.id.btnIsGuessed);
        }
    }
}