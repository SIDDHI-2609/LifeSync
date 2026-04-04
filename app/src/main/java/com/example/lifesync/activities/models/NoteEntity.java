package com.example.lifesync.activities.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room entity for the Notes module.
 * Each note is tied to a userId so users only see their own notes.
 */
@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey
    @NonNull
    public String id;           // Firestore document ID (also used as Room PK)

    public String userId;       // Firebase Auth UID — owner of this note
    public String title;
    public String content;
    public long   createdAt;    // epoch millis
    public long   updatedAt;
    public boolean isSynced;    // false = pending upload to Firestore

    public NoteEntity() {}

    public NoteEntity(@NonNull String id, String userId, String title,
                      String content, long createdAt, long updatedAt) {
        this.id        = id;
        this.userId    = userId;
        this.title     = title;
        this.content   = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isSynced  = false;
    }
}
