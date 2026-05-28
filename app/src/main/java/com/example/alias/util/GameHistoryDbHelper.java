package com.example.alias.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.alias.model.GameWordResult;
import com.example.alias.model.Team;
import com.example.alias.model.history.HistoryGame;
import com.example.alias.model.history.HistoryTeam;
import com.example.alias.model.history.HistoryWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GameHistoryDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "alias_game_history.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_GAMES = "games";
    private static final String TABLE_TEAMS = "teams";
    private static final String TABLE_WORDS = "words";

    public GameHistoryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_GAMES + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "finished_at INTEGER NOT NULL, " +
                        "duration_seconds INTEGER NOT NULL, " +
                        "round_time INTEGER NOT NULL, " +
                        "points_to_win INTEGER NOT NULL, " +
                        "difficulty TEXT NOT NULL, " +
                        "winner_team_name TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_TEAMS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "game_id INTEGER NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "score INTEGER NOT NULL, " +
                        "place INTEGER NOT NULL, " +
                        "FOREIGN KEY(game_id) REFERENCES " + TABLE_GAMES + "(id) ON DELETE CASCADE" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_WORDS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "game_id INTEGER NOT NULL, " +
                        "owner_team_id INTEGER NOT NULL, " +
                        "assigned_team_id INTEGER, " +
                        "text TEXT NOT NULL, " +
                        "guessed INTEGER NOT NULL, " +
                        "is_last_word INTEGER NOT NULL, " +
                        "word_order INTEGER NOT NULL, " +
                        "FOREIGN KEY(game_id) REFERENCES " + TABLE_GAMES + "(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(owner_team_id) REFERENCES " + TABLE_TEAMS + "(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(assigned_team_id) REFERENCES " + TABLE_TEAMS + "(id) ON DELETE CASCADE" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEAMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
        onCreate(db);
    }

    public long saveCompletedGame(
            long finishedAtMillis,
            long durationSeconds,
            int roundTime,
            int pointsToWin,
            String difficulty,
            String winnerTeamName,
            List<Team> teams,
            List<GameWordResult> words
    ) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues gameValues = new ContentValues();
            gameValues.put("finished_at", finishedAtMillis);
            gameValues.put("duration_seconds", durationSeconds);
            gameValues.put("round_time", roundTime);
            gameValues.put("points_to_win", pointsToWin);
            gameValues.put("difficulty", difficulty == null ? "easy" : difficulty);
            gameValues.put("winner_team_name", winnerTeamName == null ? "" : winnerTeamName);

            long gameId = db.insertOrThrow(TABLE_GAMES, null, gameValues);

            List<Team> sortedTeams = new ArrayList<>(teams);
            Collections.sort(sortedTeams, (team1, team2) ->
                    Integer.compare(team2.getScore(), team1.getScore())
            );

            Map<Team, Long> teamIds = new IdentityHashMap<>();

            for (int i = 0; i < sortedTeams.size(); i++) {
                Team team = sortedTeams.get(i);
                int place = calculatePlace(sortedTeams, i);

                ContentValues teamValues = new ContentValues();
                teamValues.put("game_id", gameId);
                teamValues.put("name", team.getName());
                teamValues.put("score", team.getScore());
                teamValues.put("place", place);

                long teamId = db.insertOrThrow(TABLE_TEAMS, null, teamValues);
                teamIds.put(team, teamId);
            }

            if (words != null) {
                for (GameWordResult word : words) {
                    Long ownerTeamId = teamIds.get(word.getOwnerTeam());

                    if (ownerTeamId == null) {
                        continue;
                    }

                    Long assignedTeamId = null;

                    if (word.getAssignedTeam() != null) {
                        assignedTeamId = teamIds.get(word.getAssignedTeam());
                    }

                    ContentValues wordValues = new ContentValues();
                    wordValues.put("game_id", gameId);
                    wordValues.put("owner_team_id", ownerTeamId);
                    wordValues.put("text", word.getText());
                    wordValues.put("guessed", word.isGuessed() ? 1 : 0);
                    wordValues.put("is_last_word", word.isLastWord() ? 1 : 0);
                    wordValues.put("word_order", word.getOrderIndex());

                    if (assignedTeamId == null) {
                        wordValues.putNull("assigned_team_id");
                    } else {
                        wordValues.put("assigned_team_id", assignedTeamId);
                    }

                    db.insertOrThrow(TABLE_WORDS, null, wordValues);
                }
            }

            db.setTransactionSuccessful();
            return gameId;
        } finally {
            db.endTransaction();
        }
    }

    public List<HistoryGame> getGames() {
        List<HistoryGame> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_GAMES,
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        );

        try {
            while (cursor.moveToNext()) {
                result.add(new HistoryGame(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("finished_at")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("duration_seconds")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("round_time")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("points_to_win")),
                        cursor.getString(cursor.getColumnIndexOrThrow("difficulty")),
                        cursor.getString(cursor.getColumnIndexOrThrow("winner_team_name"))
                ));
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public List<HistoryTeam> getTeamsForGame(long gameId) {
        List<HistoryTeam> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TEAMS,
                null,
                "game_id = ?",
                new String[]{String.valueOf(gameId)},
                null,
                null,
                "place ASC, score DESC, name COLLATE NOCASE ASC"
        );

        try {
            while (cursor.moveToNext()) {
                result.add(new HistoryTeam(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("game_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("score")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("place"))
                ));
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public List<HistoryWord> getWordsForTeam(long gameId, long teamId) {
        List<HistoryWord> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String selection =
                "game_id = ? AND (" +
                        "(is_last_word = 0 AND owner_team_id = ?) OR " +
                        "(is_last_word = 1 AND guessed = 1 AND assigned_team_id = ?) OR " +
                        "(is_last_word = 1 AND guessed = 0 AND owner_team_id = ?)" +
                        ")";

        String[] args = {
                String.valueOf(gameId),
                String.valueOf(teamId),
                String.valueOf(teamId),
                String.valueOf(teamId)
        };

        Cursor cursor = db.query(
                TABLE_WORDS,
                null,
                selection,
                args,
                null,
                null,
                "word_order ASC"
        );

        try {
            while (cursor.moveToNext()) {
                Long assignedTeamId = null;

                int assignedColumnIndex = cursor.getColumnIndexOrThrow("assigned_team_id");
                if (!cursor.isNull(assignedColumnIndex)) {
                    assignedTeamId = cursor.getLong(assignedColumnIndex);
                }

                result.add(new HistoryWord(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("game_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("owner_team_id")),
                        assignedTeamId,
                        cursor.getString(cursor.getColumnIndexOrThrow("text")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("guessed")) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_last_word")) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow("word_order"))
                ));
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    private int calculatePlace(List<Team> sortedTeams, int position) {
        if (position == 0) {
            return 1;
        }

        int place = 1;

        for (int i = 1; i <= position; i++) {
            if (sortedTeams.get(i).getScore() < sortedTeams.get(i - 1).getScore()) {
                place = i + 1;
            }
        }

        return place;
    }
}