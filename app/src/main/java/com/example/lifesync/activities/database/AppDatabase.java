package com.example.lifesync.activities.database;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.lifesync.activities.models.Expense;
import com.example.lifesync.activities.models.Note;

@Database(entities = {Note.class, Expense.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NoteDAO.NoteDao noteDao();
    public abstract ExpenseDAO.ExpenseDao expenseDao();

}