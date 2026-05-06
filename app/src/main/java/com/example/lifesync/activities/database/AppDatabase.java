package com.example.lifesync.activities.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.lifesync.activities.models.ExpenseEntity;
import com.example.lifesync.activities.models.NoteEntity;
import com.example.lifesync.activities.models.TodoEntity;

/**
 * FIXED AppDatabase
 *
 * Root cause fixed:
 *   The old singleton was NEVER closed between logout and login.
 *   So when User A logged out and User B logged in on the same phone,
 *   the DAO queries ran with User B's userId but found 0 rows because
 *   the DB was still "owned" by the old singleton context from User A.
 *
 *   Fix: destroyInstance() closes the DB and nulls the singleton.
 *   Call it on logout. Call getInstance() fresh on login.
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
    private static final   String       DB_NAME = "lifesync_db";

    public abstract NoteDao    noteDao();
    public abstract ExpenseDao expenseDao();
    public abstract TodoDao    todoDao();

    // ── Get or create singleton ───────────────────────────────────────────────

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                    Log.d("AppDatabase", "Database instance created");
                }
            }
        }
        return INSTANCE;
    }

    // ── CRITICAL: Call this on LOGOUT ─────────────────────────────────────────
    // Closes the database connection and destroys the singleton so the next
    // login always gets a fresh instance pointing to the correct user.

    public static void destroyInstance() {
        synchronized (AppDatabase.class) {
            if (INSTANCE != null) {
                if (INSTANCE.isOpen()) {
                    INSTANCE.close();
                }
                INSTANCE = null;
                Log.d("AppDatabase", "Database instance destroyed");
            }
        }
    }
}