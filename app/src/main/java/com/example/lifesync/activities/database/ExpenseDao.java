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

    // ── Live queries ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getAllExpenses(String userId);

    /** Filter by date range */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByDateRange(String userId, long from, long to);

    // ── Analytics: Title-based Pie Chart ──────────────────────────────────────

    /** Total spent per TITLE (all time) — used for Pie Chart */
    @Query("SELECT title, SUM(amount) as total FROM expenses WHERE userId = :userId GROUP BY title ORDER BY total DESC")
    LiveData<List<TitleTotal>> getTotalByTitle(String userId);

    /** Total spent per TITLE within date range — used for filtered Pie Chart */
    @Query("SELECT title, SUM(amount) as total FROM expenses " +
            "WHERE userId = :userId AND date BETWEEN :from AND :to " +
            "GROUP BY title ORDER BY total DESC")
    LiveData<List<TitleTotal>> getTotalByTitleInRange(String userId, long from, long to);

    // ── Analytics: Bar Chart — flexible date grouping ─────────────────────────

    /** Daily totals within a range — for Day/Week view bar chart */
    @Query("SELECT date, SUM(amount) as total FROM expenses " +
            "WHERE userId = :userId AND date >= :since " +
            "GROUP BY (date / 86400000) ORDER BY date ASC")
    LiveData<List<DailyTotal>> getDailyTotals(String userId, long since);

    /** Daily totals within a specific range */
    @Query("SELECT date, SUM(amount) as total FROM expenses " +
            "WHERE userId = :userId AND date BETWEEN :from AND :to " +
            "GROUP BY (date / 86400000) ORDER BY date ASC")
    LiveData<List<DailyTotal>> getDailyTotalsInRange(String userId, long from, long to);

    /** Monthly totals for year view — groups by month */
    @Query("SELECT date, SUM(amount) as total FROM expenses " +
            "WHERE userId = :userId AND date BETWEEN :from AND :to " +
            "GROUP BY ((date / 86400000) / 30) ORDER BY date ASC")
    LiveData<List<DailyTotal>> getMonthlyTotals(String userId, long from, long to);

    // ── Grand totals ──────────────────────────────────────────────────────────

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    LiveData<Double> getTotalAmount(String userId);

    /** Total in a date range */
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to")
    LiveData<Double> getTotalAmountInRange(String userId, long from, long to);

    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to")
    LiveData<Integer> getExpenseCountInRange(String userId, long from, long to);

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

    // ── Result classes ────────────────────────────────────────────────────────

    /** For title-based pie chart */
    class TitleTotal {
        public String title;
        public double total;
    }

    class DailyTotal {
        public long   date;
        public double total;
    }

    // Kept for backward compatibility if needed
    class CategoryTotal {
        public String category;
        public double total;
    }
}