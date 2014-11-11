package ru.ifmo.md.colloquium2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ru.ifmo.md.colloquium2.DatabaseColumns.CANDIDATES;
import static ru.ifmo.md.colloquium2.DatabaseColumns.NAME;
import static ru.ifmo.md.colloquium2.DatabaseColumns.NUMBER;
import static ru.ifmo.md.colloquium2.DatabaseColumns._ID;

/**
 * Created by dimatomp on 11.11.14.
 */
public class DataStorage extends SQLiteOpenHelper {
    private static DataStorage instance;

    private DataStorage(Context context) {
        super(context, "candidates.db", null, 1);
    }

    private static DataStorage getInstance(Context context) {
        if (instance == null)
            instance = new DataStorage(context.getApplicationContext());
        return instance;
    }

    public static Cursor getCandidates(Context context, boolean sortedByResults, boolean withPercentage) {
        return getInstance(context).getReadableDatabase().rawQuery("SELECT " + _ID + ", " + NAME + ", " + NUMBER
                + (withPercentage ? ", " + NUMBER + " * 100 / (SELECT SUM(" + NUMBER + ") FROM " + CANDIDATES + ") AS Percentage" : "") +
                " FROM " + CANDIDATES + (sortedByResults ? " ORDER BY " + NUMBER + " DESC" : "") + ";", null);
    }

    public static void addCandidate(Context context, String candidateName) {
        ContentValues values = new ContentValues(1);
        values.put(NAME, candidateName);
        getInstance(context).getWritableDatabase().insert(CANDIDATES, null, values);
    }

    public static void editCandidate(Context context, String nameBefore, String nameAfter) {
        ContentValues values = new ContentValues(1);
        values.put(NAME, nameAfter);
        getInstance(context).getWritableDatabase().update(CANDIDATES, values, NAME + " = ?", new String[]{nameBefore});
    }

    public static void startVoting(Context context) {
        ContentValues values = new ContentValues(1);
        values.put(NUMBER, 0);
        getInstance(context).getWritableDatabase().update(CANDIDATES, values, null, null);
    }

    public static void vote(Context context, long candId) {
        getInstance(context).getWritableDatabase().execSQL("UPDATE " + CANDIDATES +
                " SET " + NUMBER + " = " + NUMBER + " + 1 WHERE " + _ID + " = " + candId + ";");
    }

    public static void deleteAll(Context context) {
        getInstance(context).getWritableDatabase().delete(CANDIDATES, null, null);
    }

    public static void removeCandidate(Context context, String name) {
        getInstance(context).getWritableDatabase().delete(CANDIDATES, NAME + " = ?", new String[]{name});
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CANDIDATES + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT, " +
                NUMBER + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CANDIDATES + ";");
        onCreate(db);
    }
}
