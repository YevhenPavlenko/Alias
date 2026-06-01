package com.example.alias.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DictionaryWord {

    private final long id;
    private final String text;
    private final String description;
    private final List<String> synonyms;
    private final long createdAt;

    public DictionaryWord(
            long id,
            String text,
            String description,
            List<String> synonyms,
            long createdAt
    ) {
        this.id = id;
        this.text = text;
        this.description = description;
        this.synonyms = synonyms == null
                ? new ArrayList<>()
                : new ArrayList<>(synonyms);
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSynonyms() {
        return Collections.unmodifiableList(synonyms);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getSynonymsText() {
        if (synonyms.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < synonyms.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(synonyms.get(i));
        }

        return builder.toString();
    }
}