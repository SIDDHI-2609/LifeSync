package com.example.lifesync.activities.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity
public class Todo {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public long time;
    public boolean isDone;
}
