package com.example.lifesync.activities.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.lifesync.activities.models.ExpenseEntity;
import com.example.lifesync.activities.models.NoteEntity;
import com.example.lifesync.activities.models.TodoEntity;

/**
 * Single Room database for the entire app.
 * All modules share this one DB — each table is user-scoped via userId.
 *
 * Usage:
 *   AppDatabase db = AppDatabase.getInstance(context);
 *   db.noteDao().getAllNotes(userId);
 */
@Database(
        entities = {
                NoteEntity.class,
                ExpenseEntity.class,
                TodoEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DB_NAME = "smartassistant_db";

    public abstract NoteDao    noteDao();
    public abstract ExpenseDao expenseDao();
    public abstract TodoDao    todoDao();

    /** Thread-safe singleton */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration() // replace with Migration objects in prod
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}