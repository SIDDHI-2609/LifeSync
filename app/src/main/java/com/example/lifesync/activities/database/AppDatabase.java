package com.example.lifesync.activities.database;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.lifesync.activities.models.Note;

@Database(entities = {Note.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NoteDAO.NoteDao noteDao();
}