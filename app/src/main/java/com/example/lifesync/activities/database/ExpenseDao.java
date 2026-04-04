package com.example.lifesync.activities.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.lifesync.activities.models.ExpenseEntity;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExpenseEntity expense);

    @Update
    void update(ExpenseEntity expense);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("DELETE FROM expenses WHERE id = :expenseId AND userId = :userId")
    void deleteById(String expenseId, String userId);

    @Query("DELETE FROM expenses WHERE userId = :userId")
    void deleteAllByUser(String userId);

    // ── Live queries (auto-refresh UI) ────────────────────────────────────────

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getAllExpenses(String userId);

    /** Filter by category */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = :category ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByCategory(String userId, String category);

    /** Filter by date range (e.g. current month) */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByDateRange(String userId, long from, long to);

    // ── Analytics queries (for Dashboard charts) ──────────────────────────────

    /** Total spent per category — used for Pie Chart */
    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE userId = :userId GROUP BY category")
    LiveData<List<CategoryTotal>> getTotalByCategory(String userId);

    /** Total spent per day for the last 30 days — used for Bar Chart */
    @Query("SELECT date, SUM(amount) as total FROM expenses WHERE userId = :userId AND date >= :since GROUP BY date ORDER BY date ASC")
    LiveData<List<DailyTotal>> getDailyTotals(String userId, long since);

    /** Grand total for a user */
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    LiveData<Double> getTotalAmount(String userId);

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    double getTotalExpenses(String userId);

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date >= :since")
    double getMonthlyExpenses(String userId, long since);

    // ── Sync helpers ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM expenses WHERE userId = :userId AND isSynced = 0")
    List<ExpenseEntity> getUnsyncedExpenses(String userId);

    @Query("UPDATE expenses SET isSynced = 1 WHERE id = :expenseId")
    void markSynced(String expenseId);

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    List<ExpenseEntity> getAllExpensesSync(String userId);

    // ── Nested result classes for aggregate queries ───────────────────────────

    class CategoryTotal {
        public String category;
        public double total;
    }

    class DailyTotal {
        public long   date;
        public double total;
    }
}
