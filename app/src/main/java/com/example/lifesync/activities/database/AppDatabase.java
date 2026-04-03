package com.example.lifesync.activities.database;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.lifesync.activities.models.Expense;
import com.example.lifesync.activities.models.Note;
import com.example.lifesync.activities.models.User;

@Database(entities = {Note.class, Expense.class, User.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NoteDAO.NoteDao noteDao();
    public abstract ExpenseDAO.ExpenseDao expenseDao();
//    public abstract TodoDAO.TodoDao todoDao();
    public abstract UserDAO userDao();

}