package com.example.alias.ui.game.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alias.R;
import com.example.alias.model.Word;
import com.example.alias.util.DictionaryDbHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TurnResultsAdapter extends RecyclerView.Adapter<TurnResultsAdapter.TurnResultsViewHolder> {

    public interface OnWordResultActionListener {
        void onToggleNormalWord(Word word, int position);

        void onToggleLastWord(Word word, int position);

        void onChangeLastWordTeam(Word word, int position);
    }

    private final List<Word> words;
    private final Context context;
    private final OnWordResultActionListener listener;
    private final DictionaryDbHelper dictionaryDbHelper;

    public TurnResultsAdapter(
            Context context,
            List<Word> words,
            OnWordResultActionListener listener
    ) {
        this.context = context;
        this.words = words;
        this.listener = listener;
        this.dictionaryDbHelper = new DictionaryDbHelper(context);
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

        String formattedName;

        if (word.isLastWord()) {
            formattedName = context.getString(R.string.last_word_format, word.getText());
        } else {
            formattedName = context.getString(R.string.word_format, word.getText());
        }

        holder.tvUsedWord.setText(formattedName);

        applyWordState(holder, word, context);
        applyLastWordInfo(holder, word, context);
        setupDictionaryButton(holder, word);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION || listener == null) {
                return;
            }

            Word clickedWord = words.get(adapterPosition);

            if (clickedWord.isLastWord()) {
                listener.onChangeLastWordTeam(clickedWord, adapterPosition);
            } else {
                listener.onToggleNormalWord(clickedWord, adapterPosition);
            }
        });

        holder.btnIsGuessed.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION || listener == null) {
                return;
            }

            Word clickedWord = words.get(adapterPosition);

            if (clickedWord.isLastWord()) {
                listener.onToggleLastWord(clickedWord, adapterPosition);
            } else {
                listener.onToggleNormalWord(clickedWord, adapterPosition);
            }
        });

        holder.tvWordMeta.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION || listener == null) {
                return;
            }

            Word clickedWord = words.get(adapterPosition);

            if (clickedWord.isLastWord()) {
                listener.onChangeLastWordTeam(clickedWord, adapterPosition);
            }
        });
    }

    private void setupDictionaryButton(@NonNull TurnResultsViewHolder holder, Word word) {
        String wordText = getCleanWordText(word);

        if (wordText.isEmpty() || dictionaryDbHelper.isWordInDictionary(wordText)) {
            holder.btnAddToDictionary.setVisibility(View.GONE);
            holder.btnAddToDictionary.setOnClickListener(null);
            return;
        }

        holder.btnAddToDictionary.setVisibility(View.VISIBLE);
        holder.btnAddToDictionary.setColorFilter(
                ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white)
        );

        holder.btnAddToDictionary.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            Word clickedWord = words.get(adapterPosition);
            String clickedWordText = getCleanWordText(clickedWord);

            if (clickedWordText.isEmpty()) {
                return;
            }

            if (dictionaryDbHelper.isWordInDictionary(clickedWordText)) {
                holder.btnAddToDictionary.setVisibility(View.GONE);
                return;
            }

            long wordId = dictionaryDbHelper.addWordFromCatalog(clickedWordText);

            if (wordId != -1) {
                holder.btnAddToDictionary.setVisibility(View.GONE);
                Toast.makeText(
                        holder.itemView.getContext(),
                        R.string.word_added_to_dictionary,
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        holder.itemView.getContext(),
                        R.string.word_add_to_dictionary_error,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private String getCleanWordText(Word word) {
        if (word == null || word.getText() == null) {
            return "";
        }

        return word.getText().trim().replaceAll("\\s+", " ");
    }

    private void applyLastWordInfo(
            @NonNull TurnResultsViewHolder holder,
            Word word,
            Context context
    ) {
        if (!word.isLastWord()) {
            holder.tvWordMeta.setVisibility(View.GONE);
            return;
        }

        holder.tvWordMeta.setVisibility(View.VISIBLE);
        holder.tvWordMeta.setTextColor(
                ContextCompat.getColor(context, R.color.last_word_meta_text)
        );

        if (word.isGuessed() && word.getAssignedTeam() != null) {
            holder.tvWordMeta.setText(
                    context.getString(
                            R.string.last_word_point_to,
                            word.getAssignedTeam().getName()
                    )
            );
        } else if (word.isGuessed()) {
            holder.tvWordMeta.setText(R.string.last_word_choose_team);
        } else {
            holder.tvWordMeta.setText(R.string.last_word_no_point);
        }
    }

    private void applyWordState(
            @NonNull TurnResultsViewHolder holder,
            Word word,
            Context context
    ) {
        if (word.isLastWord()) {
            holder.cardUsedWord.setStrokeWidth(dp(context, 2));

            holder.tvUsedWord.setTextColor(
                    ContextCompat.getColor(context, R.color.last_word_text)
            );

            holder.tvWordMeta.setTextColor(
                    ContextCompat.getColor(context, R.color.last_word_meta_text)
            );

            if (word.isGuessed() && word.getAssignedTeam() != null) {
                holder.cardUsedWord.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.last_word_success_bg)
                );

                holder.cardUsedWord.setStrokeColor(
                        ContextCompat.getColor(context, R.color.last_word_success_stroke)
                );

                holder.btnIsGuessed.setImageResource(R.drawable.ic_guessed);

                holder.btnIsGuessed.setColorFilter(
                        ContextCompat.getColor(context, R.color.last_word_success_stroke)
                );

            } else if (word.isGuessed()) {
                holder.cardUsedWord.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.last_word_pending_bg)
                );

                holder.cardUsedWord.setStrokeColor(
                        ContextCompat.getColor(context, R.color.last_word_pending_stroke)
                );

                holder.btnIsGuessed.setImageResource(R.drawable.ic_guessed);

                holder.btnIsGuessed.setColorFilter(
                        ContextCompat.getColor(context, R.color.last_word_pending_stroke)
                );

            } else {
                holder.cardUsedWord.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.last_word_no_point_bg)
                );

                holder.cardUsedWord.setStrokeColor(
                        ContextCompat.getColor(context, R.color.last_word_no_point_stroke)
                );

                holder.btnIsGuessed.setImageResource(R.drawable.ic_unguessed);

                holder.btnIsGuessed.setColorFilter(
                        ContextCompat.getColor(context, R.color.last_word_no_point_stroke)
                );
            }

            return;
        }

        holder.cardUsedWord.setStrokeWidth(dp(context, 1));

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

    private int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public static class TurnResultsViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardUsedWord;
        TextView tvUsedWord;
        TextView tvWordMeta;
        ImageButton btnIsGuessed;
        ImageButton btnAddToDictionary;

        public TurnResultsViewHolder(@NonNull View itemView) {
            super(itemView);

            cardUsedWord = itemView.findViewById(R.id.cardUsedWord);
            tvUsedWord = itemView.findViewById(R.id.tvUsedWord);
            tvWordMeta = itemView.findViewById(R.id.tvWordMeta);
            btnIsGuessed = itemView.findViewById(R.id.btnIsGuessed);
            btnAddToDictionary = itemView.findViewById(R.id.btnAddToDictionary);
        }
    }
}