package com.example.alias.ui.dictionary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alias.R;
import com.example.alias.model.DictionaryWord;
import com.example.alias.ui.base.BaseActivity;
import com.example.alias.util.DictionaryDbHelper;

import java.util.List;

public class DictionaryActivity extends BaseActivity {

    private DictionaryDbHelper dbHelper;
    private LinearLayout wordsContainer;
    private View emptyDictionaryContainer;
    private View dictionaryScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        setupHeader(getString(R.string.dictionary_title));

        dbHelper = new DictionaryDbHelper(this);
        wordsContainer = findViewById(R.id.wordsContainer);
        emptyDictionaryContainer = findViewById(R.id.emptyDictionaryContainer);
        dictionaryScrollView = findViewById(R.id.dictionaryScrollView);

        loadDictionaryWords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDictionaryWords();
    }

    private void loadDictionaryWords() {
        List<DictionaryWord> words = dbHelper.getAllWords();

        wordsContainer.removeAllViews();

        if (words.isEmpty()) {
            emptyDictionaryContainer.setVisibility(View.VISIBLE);
            dictionaryScrollView.setVisibility(View.GONE);
            return;
        }

        emptyDictionaryContainer.setVisibility(View.GONE);
        dictionaryScrollView.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DictionaryWord word : words) {
            View itemView = inflater.inflate(R.layout.item_dictionary_word, wordsContainer, false);

            TextView tvWord = itemView.findViewById(R.id.tvDictionaryWord);
            TextView tvDescription = itemView.findViewById(R.id.tvDictionaryDescription);
            TextView tvSynonyms = itemView.findViewById(R.id.tvDictionarySynonyms);
            TextView btnDelete = itemView.findViewById(R.id.btnDeleteDictionaryWord);

            tvWord.setText(word.getText());
            tvDescription.setText(word.getDescription());

            String synonymsText = word.getSynonymsText();

            if (synonymsText.isEmpty()) {
                tvSynonyms.setText(getString(R.string.dictionary_no_synonyms));
            } else {
                tvSynonyms.setText(getString(R.string.dictionary_synonyms_format, synonymsText));
            }

            btnDelete.setOnClickListener(v -> showDeleteConfirmation(word));

            wordsContainer.addView(itemView);
        }
    }

    private void showDeleteConfirmation(DictionaryWord word) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dictionary_delete_title))
                .setMessage(getString(R.string.dictionary_delete_message, word.getText()))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> deleteWord(word))
                .show();
    }

    private void deleteWord(DictionaryWord word) {
        boolean deleted = dbHelper.deleteWord(word.getId());

        if (deleted) {
            Toast.makeText(this, R.string.dictionary_word_deleted, Toast.LENGTH_SHORT).show();
            loadDictionaryWords();
        } else {
            Toast.makeText(this, R.string.dictionary_word_delete_error, Toast.LENGTH_SHORT).show();
        }
    }
}