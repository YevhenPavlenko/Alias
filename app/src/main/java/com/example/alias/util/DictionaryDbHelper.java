package com.example.alias.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.alias.model.DictionaryWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps two different kinds of data:
 * 1) dictionary_words - user's personal words for repetition;
 * 2) word_catalog - all game words with difficulty, description and associations.
 */
public class DictionaryDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "alias_dictionary.db";
    private static final int DB_VERSION = 5;

    private static final String TABLE_WORDS = "dictionary_words";
    private static final String TABLE_ASSOCIATIONS = "dictionary_word_associations";

    private static final String TABLE_CATALOG = "word_catalog";
    private static final String TABLE_CATALOG_ASSOCIATIONS = "word_catalog_associations";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_SEED_VERSION = "seed_version";

    private static final String COLUMN_WORD_ID = "word_id";
    private static final String COLUMN_CATALOG_WORD_ID = "catalog_word_id";
    private static final String COLUMN_ASSOCIATED_TEXT = "associated_text";
    private static final String COLUMN_ASSOCIATION_ORDER = "association_order";

    private static final String DIFFICULTY_EASY = "easy";
    private static final String DIFFICULTY_MEDIUM = "medium";
    private static final String DIFFICULTY_HARD = "hard";
    private static final int EASY_SEED_VERSION = 2;
    private static final int MEDIUM_SEED_VERSION = 1;
    private static final int HARD_SEED_VERSION = 1;

    private final Context appContext;

    public DictionaryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUserDictionaryTables(db);
        createCatalogTables(db);
        seedCatalogIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createUserDictionaryTables(db);
        createCatalogTables(db);

        seedCatalogIfNeeded(db);
    }

    private void createUserDictionaryTables(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_WORDS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TEXT + " TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                        COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                        COLUMN_CREATED_AT + " INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_ASSOCIATIONS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_WORD_ID + " INTEGER NOT NULL, " +
                        COLUMN_ASSOCIATED_TEXT + " TEXT NOT NULL, " +
                        COLUMN_ASSOCIATION_ORDER + " INTEGER NOT NULL, " +
                        "UNIQUE(" + COLUMN_WORD_ID + ", " + COLUMN_ASSOCIATED_TEXT + " COLLATE NOCASE), " +
                        "FOREIGN KEY(" + COLUMN_WORD_ID + ") REFERENCES " + TABLE_WORDS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                        ")"
        );
    }

    private void createCatalogTables(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_CATALOG + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TEXT + " TEXT NOT NULL COLLATE NOCASE, " +
                        COLUMN_DIFFICULTY + " TEXT NOT NULL, " +
                        COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                        COLUMN_SEED_VERSION + " INTEGER NOT NULL DEFAULT 1, " +
                        "UNIQUE(" + COLUMN_TEXT + " COLLATE NOCASE, " + COLUMN_DIFFICULTY + ")" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_CATALOG_ASSOCIATIONS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_CATALOG_WORD_ID + " INTEGER NOT NULL, " +
                        COLUMN_ASSOCIATED_TEXT + " TEXT NOT NULL, " +
                        COLUMN_ASSOCIATION_ORDER + " INTEGER NOT NULL, " +
                        "UNIQUE(" + COLUMN_CATALOG_WORD_ID + ", " + COLUMN_ASSOCIATED_TEXT + " COLLATE NOCASE), " +
                        "FOREIGN KEY(" + COLUMN_CATALOG_WORD_ID + ") REFERENCES " + TABLE_CATALOG + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                        ")"
        );
    }

    private void seedCatalogIfNeeded(SQLiteDatabase db) {
        boolean easyWasSeeded = seedCatalogForDifficultyIfNeeded(
                db,
                DIFFICULTY_EASY,
                "word_catalog_easy.csv",
                EASY_SEED_VERSION
        );

        boolean mediumWasSeeded = seedCatalogForDifficultyIfNeeded(
                db,
                DIFFICULTY_MEDIUM,
                "word_catalog_medium.csv",
                MEDIUM_SEED_VERSION
        );

        boolean hardWasSeeded = seedCatalogForDifficultyIfNeeded(
                db,
                DIFFICULTY_HARD,
                "word_catalog_hard.csv",
                HARD_SEED_VERSION
        );

        if (easyWasSeeded || mediumWasSeeded || hardWasSeeded) {
            syncUserDictionaryWithCatalog(db);
        }
    }

    private boolean seedCatalogForDifficultyIfNeeded(
            SQLiteDatabase db,
            String difficulty,
            String fileName,
            int seedVersion
    ) {
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        int currentSeedVersion = getMaxCatalogSeedVersionForDifficulty(db, normalizedDifficulty);
        int currentWordsCount = getCatalogCountForDifficulty(db, normalizedDifficulty);

        if (currentWordsCount > 0 && currentSeedVersion >= seedVersion) {
            return false;
        }

        deleteCatalogForDifficulty(db, normalizedDifficulty);
        seedCatalogFromAsset(db, fileName, seedVersion);
        return true;
    }

    public void ensureCatalogSeeded() {
        SQLiteDatabase db = getWritableDatabase();
        seedCatalogIfNeeded(db);
    }

    public long addWord(String text, String description, List<String> associations) {
        String normalizedText = normalizeText(text);

        if (normalizedText.isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            long existingWordId = getWordId(db, normalizedText);

            if (existingWordId != -1) {
                db.setTransactionSuccessful();
                return existingWordId;
            }

            ContentValues wordValues = new ContentValues();
            wordValues.put(COLUMN_TEXT, normalizedText);
            wordValues.put(COLUMN_DESCRIPTION, normalizeDescription(description));
            wordValues.put(COLUMN_CREATED_AT, System.currentTimeMillis());

            long wordId = db.insertOrThrow(TABLE_WORDS, null, wordValues);

            insertAssociations(db, wordId, associations);

            db.setTransactionSuccessful();
            return wordId;
        } finally {
            db.endTransaction();
        }
    }

    public long addWordFromCatalog(String text) {
        String normalizedText = normalizeText(text);

        if (normalizedText.isEmpty()) {
            return -1;
        }

        CatalogWordInfo info = getCatalogWordInfo(normalizedText);

        return addWord(
                normalizedText,
                info.getDescription(),
                info.getAssociations()
        );
    }

    public boolean isWordInDictionary(String text) {
        String normalizedText = normalizeText(text);

        if (normalizedText.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_WORDS,
                new String[]{COLUMN_ID},
                COLUMN_TEXT + " = ? COLLATE NOCASE",
                new String[]{normalizedText},
                null,
                null,
                null,
                "1"
        )) {
            return cursor.moveToFirst();
        }
    }

    public List<DictionaryWord> getAllWords() {
        List<DictionaryWord> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_WORDS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_CREATED_AT + " DESC"
        )) {
            while (cursor.moveToNext()) {
                long wordId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));

                result.add(new DictionaryWord(
                        wordId,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        getAssociationsForWord(db, wordId),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                ));
            }
        }

        return result;
    }

    public boolean deleteWord(long wordId) {
        SQLiteDatabase db = getWritableDatabase();

        int deletedRows = db.delete(
                TABLE_WORDS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(wordId)}
        );

        return deletedRows > 0;
    }

    public boolean deleteWord(String text) {
        String normalizedText = normalizeText(text);

        if (normalizedText.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        int deletedRows = db.delete(
                TABLE_WORDS,
                COLUMN_TEXT + " = ? COLLATE NOCASE",
                new String[]{normalizedText}
        );

        return deletedRows > 0;
    }

    public List<String> getRandomCatalogWords(String difficulty, int count) {
        List<String> result = new ArrayList<>();

        if (count <= 0) {
            return result;
        }

        ensureCatalogSeeded();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_CATALOG,
                new String[]{COLUMN_TEXT},
                COLUMN_DIFFICULTY + " = ?",
                new String[]{normalizeDifficulty(difficulty)},
                null,
                null,
                "RANDOM()",
                String.valueOf(count)
        )) {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)));
            }
        }

        return result;
    }

    public CatalogWordInfo getCatalogWordInfo(String text) {
        String normalizedText = normalizeText(text);

        if (normalizedText.isEmpty()) {
            return CatalogWordInfo.fallback();
        }

        ensureCatalogSeeded();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_CATALOG,
                new String[]{COLUMN_ID, COLUMN_DESCRIPTION},
                COLUMN_TEXT + " = ? COLLATE NOCASE",
                new String[]{normalizedText},
                null,
                null,
                null,
                "1"
        )) {
            if (!cursor.moveToFirst()) {
                return CatalogWordInfo.fallback();
            }

            long catalogWordId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));

            return new CatalogWordInfo(
                    description,
                    getAssociationsForCatalogWord(db, catalogWordId)
            );
        }
    }

    private void insertAssociations(SQLiteDatabase db, long wordId, List<String> associations) {
        if (associations == null || associations.isEmpty()) {
            return;
        }

        int order = 0;

        for (String association : associations) {
            String normalizedAssociation = normalizeText(association);

            if (normalizedAssociation.isEmpty()) {
                continue;
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_WORD_ID, wordId);
            values.put(COLUMN_ASSOCIATED_TEXT, normalizedAssociation);
            values.put(COLUMN_ASSOCIATION_ORDER, order);

            db.insertWithOnConflict(
                    TABLE_ASSOCIATIONS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            );

            order++;
        }
    }

    private List<String> getAssociationsForWord(SQLiteDatabase db, long wordId) {
        List<String> result = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLE_ASSOCIATIONS,
                new String[]{COLUMN_ASSOCIATED_TEXT},
                COLUMN_WORD_ID + " = ?",
                new String[]{String.valueOf(wordId)},
                null,
                null,
                COLUMN_ASSOCIATION_ORDER + " ASC"
        )) {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ASSOCIATED_TEXT)));
            }
        }

        return result;
    }

    private List<String> getAssociationsForCatalogWord(SQLiteDatabase db, long catalogWordId) {
        List<String> result = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLE_CATALOG_ASSOCIATIONS,
                new String[]{COLUMN_ASSOCIATED_TEXT},
                COLUMN_CATALOG_WORD_ID + " = ?",
                new String[]{String.valueOf(catalogWordId)},
                null,
                null,
                COLUMN_ASSOCIATION_ORDER + " ASC"
        )) {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ASSOCIATED_TEXT)));
            }
        }

        return result;
    }

    private long getWordId(SQLiteDatabase db, String text) {

        try (Cursor cursor = db.query(
                TABLE_WORDS,
                new String[]{COLUMN_ID},
                COLUMN_TEXT + " = ? COLLATE NOCASE",
                new String[]{text},
                null,
                null,
                null,
                "1"
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }

            return -1;
        }
    }

    private int getCatalogCountForDifficulty(SQLiteDatabase db, String difficulty) {

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_CATALOG + " WHERE " + COLUMN_DIFFICULTY + " = ?",
                new String[]{normalizeDifficulty(difficulty)}
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

            return 0;
        }
    }

    private int getMaxCatalogSeedVersionForDifficulty(SQLiteDatabase db, String difficulty) {

        try (Cursor cursor = db.rawQuery(
                "SELECT MAX(" + COLUMN_SEED_VERSION + ") FROM " + TABLE_CATALOG + " WHERE " + COLUMN_DIFFICULTY + " = ?",
                new String[]{normalizeDifficulty(difficulty)}
        )) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getInt(0);
            }

            return 0;
        }
    }

    private void deleteCatalogForDifficulty(SQLiteDatabase db, String difficulty) {
        db.delete(
                TABLE_CATALOG,
                COLUMN_DIFFICULTY + " = ?",
                new String[]{normalizeDifficulty(difficulty)}
        );
    }

    private void seedCatalogFromAsset(SQLiteDatabase db, String fileName, int seedVersion) {
        db.beginTransaction();

        try {
            InputStream inputStream = appContext.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            );

            String line;

            while ((line = reader.readLine()) != null) {
                seedCatalogLine(db, line, seedVersion);
            }

            reader.close();
            db.setTransactionSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private void seedCatalogLine(SQLiteDatabase db, String line, int seedVersion) {
        if (line == null) {
            return;
        }

        String trimmedLine = stripUtfBom(line).trim();

        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
            return;
        }

        String[] parts = splitCatalogLine(trimmedLine);

        if (parts.length < 4) {
            return;
        }

        String text = normalizeText(parts[0]);
        String difficulty = normalizeDifficulty(parts[1]);
        String description = normalizeDescription(parts[2]);

        if (text.isEmpty() || difficulty.isEmpty()) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, text);
        values.put(COLUMN_DIFFICULTY, difficulty);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_SEED_VERSION, seedVersion);

        long catalogWordId = db.insertWithOnConflict(
                TABLE_CATALOG,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        if (catalogWordId == -1) {
            catalogWordId = getCatalogWordId(db, text, difficulty);
        }

        if (catalogWordId == -1) {
            return;
        }

        insertCatalogAssociations(db, catalogWordId, parseAssociations(parts[3]));
    }

    private String[] splitCatalogLine(String line) {
        if (line.contains(";")) {
            return line.split(";", -1);
        }

        return line.split("\\|", -1);
    }

    private long getCatalogWordId(SQLiteDatabase db, String text, String difficulty) {

        try (Cursor cursor = db.query(
                TABLE_CATALOG,
                new String[]{COLUMN_ID},
                COLUMN_TEXT + " = ? COLLATE NOCASE AND " + COLUMN_DIFFICULTY + " = ?",
                new String[]{text, difficulty},
                null,
                null,
                null,
                "1"
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }

            return -1;
        }
    }

    private void insertCatalogAssociations(SQLiteDatabase db, long catalogWordId, List<String> associations) {
        int order = 0;

        for (String association : associations) {
            String normalizedAssociation = normalizeText(association);

            if (normalizedAssociation.isEmpty()) {
                continue;
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_CATALOG_WORD_ID, catalogWordId);
            values.put(COLUMN_ASSOCIATED_TEXT, normalizedAssociation);
            values.put(COLUMN_ASSOCIATION_ORDER, order);

            db.insertWithOnConflict(
                    TABLE_CATALOG_ASSOCIATIONS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            );

            order++;
        }
    }

    private void syncUserDictionaryWithCatalog(SQLiteDatabase db) {

        try (Cursor cursor = db.query(
                TABLE_WORDS,
                new String[]{COLUMN_ID, COLUMN_TEXT},
                null,
                null,
                null,
                null,
                null
        )) {
            while (cursor.moveToNext()) {
                long userWordId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));
                CatalogWordInfo info = getCatalogWordInfoWithoutEnsure(db, text);

                if (!info.hasCatalogMatch()) {
                    continue;
                }

                ContentValues values = new ContentValues();
                values.put(COLUMN_DESCRIPTION, info.getDescription());

                db.update(
                        TABLE_WORDS,
                        values,
                        COLUMN_ID + " = ?",
                        new String[]{String.valueOf(userWordId)}
                );

                db.delete(
                        TABLE_ASSOCIATIONS,
                        COLUMN_WORD_ID + " = ?",
                        new String[]{String.valueOf(userWordId)}
                );

                insertAssociations(db, userWordId, info.getAssociations());
            }
        }
    }

    private CatalogWordInfo getCatalogWordInfoWithoutEnsure(SQLiteDatabase db, String text) {
        String normalizedText = normalizeText(text);

        if (normalizedText.isEmpty()) {
            return CatalogWordInfo.fallback();
        }

        try (Cursor cursor = db.query(
                TABLE_CATALOG,
                new String[]{COLUMN_ID, COLUMN_DESCRIPTION},
                COLUMN_TEXT + " = ? COLLATE NOCASE",
                new String[]{normalizedText},
                null,
                null,
                null,
                "1"
        )) {
            if (!cursor.moveToFirst()) {
                return CatalogWordInfo.fallback();
            }

            long catalogWordId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));

            return new CatalogWordInfo(
                    description,
                    getAssociationsForCatalogWord(db, catalogWordId),
                    true
            );
        }
    }

    private List<String> parseAssociations(String value) {
        List<String> result = new ArrayList<>();

        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        String[] parts = value.split(",");

        for (String part : parts) {
            String normalizedPart = normalizeText(part);

            if (!normalizedPart.isEmpty()) {
                result.add(normalizedPart);
            }
        }

        return result;
    }

    private String stripUtfBom(String value) {
        if (value != null && value.startsWith("\uFEFF")) {
            return value.substring(1);
        }

        return value;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return stripUtfBom(value).trim().replaceAll("\\s+", " ");
    }

    private String normalizeDifficulty(String difficulty) {
        return normalizeText(difficulty).toLowerCase();
    }

    private String normalizeDescription(String description) {
        String normalizedDescription = normalizeText(description);

        if (normalizedDescription.isEmpty()) {
            return "опис слова відсутній";
        }

        return normalizedDescription;
    }

    public static class CatalogWordInfo {

        private final String description;
        private final List<String> associations;
        private final boolean catalogMatch;

        public CatalogWordInfo(String description, List<String> associations) {
            this(description, associations, true);
        }

        public CatalogWordInfo(String description, List<String> associations, boolean catalogMatch) {
            this.description = description;
            this.associations = associations == null
                    ? new ArrayList<>()
                    : new ArrayList<>(associations);
            this.catalogMatch = catalogMatch;
        }

        public static CatalogWordInfo fallback() {
            return new CatalogWordInfo(
                    "опис слова відсутній",
                    new ArrayList<>(),
                    false
            );
        }

        public String getDescription() {
            return description;
        }

        public List<String> getAssociations() {
            return new ArrayList<>(associations);
        }

        public boolean hasCatalogMatch() {
            return catalogMatch;
        }
    }
}