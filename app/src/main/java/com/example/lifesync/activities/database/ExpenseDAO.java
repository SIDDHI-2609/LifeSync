package com.example.lifesync.activities.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lifesync.activities.models.CategoryTotal;
import com.example.lifesync.activities.models.Expense;

import java.util.List;

public interface ExpenseDAO {
    @Dao
    public interface ExpenseDao {

        @Insert
        void insert(Expense expense);

        @Delete
        void delete(Expense expense);

        @Query("SELECT * FROM Expense ORDER BY date DESC")
        List<Expense> getAllExpenses();

        @Update
        void update(Expense expense);

        @Query("SELECT SUM(amount) FROM Expense")
        double getTotalExpenses();

        @Query("SELECT SUM(amount) FROM Expense WHERE date >= :startOfMonth")
        double getMonthlyExpenses(long startOfMonth);

        @Query("SELECT category, SUM(amount) AS total FROM Expense GROUP BY category")
        List<CategoryTotal> getCategoryTotals();
    }
}
