package com.example.lifesync.activities.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.lifesync.activities.models.NoteEntity;

import java.util.List;

@Dao
public interface NoteDao {

    // ── Insert / Update ───────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NoteEntity note);

    @Update
    void update(NoteEntity note);

    // ── Delete ────────────────────────────────────────────────────────────────
    @Delete
    void delete(NoteEntity note);

    @Query("DELETE FROM notes WHERE id = :noteId AND userId = :userId")
    void deleteById(String noteId, String userId);

    @Query("DELETE FROM notes WHERE userId = :userId")
    void deleteAllByUser(String userId);

    // ── Queries ───────────────────────────────────────────────────────────────

    /** All notes for a user, newest first. LiveData auto-updates UI on change. */
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY updatedAt DESC")
    LiveData<List<NoteEntity>> getAllNotes(String userId);

    /** One-shot fetch (for background sync workers) */
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY updatedAt DESC")
    List<NoteEntity> getAllNotesSync(String userId);

    @Query("SELECT * FROM notes WHERE id = :noteId AND userId = :userId LIMIT 1")
    NoteEntity getNoteById(String noteId, String userId);

    /** Returns notes not yet pushed to Firestore */
    @Query("SELECT * FROM notes WHERE userId = :userId AND isSynced = 0")
    List<NoteEntity> getUnsyncedNotes(String userId);

    @Query("UPDATE notes SET isSynced = 1 WHERE id = :noteId")
    void markSynced(String noteId);

    /** Full-text search by title or content */
    @Query("SELECT * FROM notes WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    LiveData<List<NoteEntity>> searchNotes(String userId, String query);
}
