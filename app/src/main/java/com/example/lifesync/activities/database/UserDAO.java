package com.example.lifesync.activities.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.lifesync.activities.models.User;

@Dao
public interface UserDAO {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);
}
