package com.example.alias.ui.game.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Team;
import com.example.alias.model.Word;
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

        holder.btnIsGuessed.setImageResource(
                word.isGuessed() ? R.drawable.ic_guessed : R.drawable.ic_unguessed
        );

        holder.tvUsedWord.setTextColor(word.isGuessed() ? context.getColor(R.color.correctGreen) : context.getColor(R.color.wrongRed));

        holder.itemView.setOnClickListener(v -> {
            word.setGuessed(!word.isGuessed());
            notifyItemChanged(holder.getAdapterPosition());
        });

        setupIsGuessedButton(holder, word);
    }

    private void setupIsGuessedButton(@NonNull TurnResultsViewHolder holder, Word word) {
        holder.btnIsGuessed.setImageResource(
                word.isGuessed() ? R.drawable.ic_guessed : R.drawable.ic_unguessed
        );

        holder.btnIsGuessed.setOnClickListener(v -> {
            word.setGuessed(!word.isGuessed());
            notifyItemChanged(holder.getAdapterPosition());
        });
    }


    @Override
    public int getItemCount() {
        return words.size();
    }

    public static class TurnResultsViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsedWord;
        ImageButton btnIsGuessed;

        public TurnResultsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsedWord = itemView.findViewById(R.id.tvUsedWord);
            btnIsGuessed = itemView.findViewById(R.id.btnIsGuessed);
        }
    }
}