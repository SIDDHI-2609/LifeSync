package com.example.lifesync.activities.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room entity for the To-Do Reminder module.
 */
@Entity(tableName = "todos")
public class TodoEntity {

    @PrimaryKey
    @NonNull
    public String id;           // Firestore document ID

    public String userId;       // Firebase Auth UID
    public String title;
    public String description;
    public boolean isCompleted;
    public long   alarmTimeMillis;   // 0 = no alarm
    public long   createdAt;
    public boolean isSynced;

    public TodoEntity() {}

    public TodoEntity(@NonNull String id, String userId, String title,
                      String description, long alarmTimeMillis) {
        this.id              = id;
        this.userId          = userId;
        this.title           = title;
        this.description     = description;
        this.isCompleted     = false;
        this.alarmTimeMillis = alarmTimeMillis;
        this.createdAt       = System.currentTimeMillis();
        this.isSynced        = false;
    }
}
