package com.example.lifesync.activities.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String mobile;
    public String email;
    public String name;
    public String password;
}
