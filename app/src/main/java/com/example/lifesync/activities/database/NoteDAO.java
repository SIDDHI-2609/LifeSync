package com.example.lifesync.activities.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.example.lifesync.activities.models.Note;
public class NoteDAO {
    @Dao
    public interface NoteDao {

        @Insert
        void insert(Note note);

        @Query("SELECT * FROM Note")
        List<Note> getAllNotes();

        @Delete
        void delete(Note note);
    }
}
