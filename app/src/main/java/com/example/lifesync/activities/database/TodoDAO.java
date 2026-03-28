package com.example.lifesync.activities.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lifesync.activities.models.Todo;

import java.util.List;

public class TodoDAO {
    @Dao
    public interface TodoDao {

        @Insert
        void insert(Todo todo);

        @Delete
        void delete(Todo todo);

        @Update
        void update(Todo todo);

        @Query("SELECT * FROM Todo ORDER BY time ASC")
        List<Todo> getAllTodos();
    }


}
