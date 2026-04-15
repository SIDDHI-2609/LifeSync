package repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.database.ExpenseDao;
import com.example.lifesync.activities.database.FirebaseSyncManager;
import com.example.lifesync.activities.models.ExpenseEntity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private final ExpenseDao dao;
    private final FirebaseSyncManager sync;
    private final String            userId;
    private final ExecutorService   executor = Executors.newSingleThreadExecutor();

    public ExpenseRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dao    = db.expenseDao();
        this.sync   = new FirebaseSyncManager(context);
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return dao.getAllExpenses(userId);
    }

    public LiveData<List<ExpenseEntity>> getByDateRange(long from, long to) {
        return dao.getExpensesByDateRange(userId, from, to);
    }

    // ── Analytics: Title-based (Pie Chart) ────────────────────────────────────

    /** For Pie Chart — total per title (all time) */
    public LiveData<List<ExpenseDao.TitleTotal>> getTotalByTitle() {
        return dao.getTotalByTitle(userId);
    }

    /** For Pie Chart — total per title within a date range */
    public LiveData<List<ExpenseDao.TitleTotal>> getTotalByTitleInRange(long from, long to) {
        return dao.getTotalByTitleInRange(userId, from, to);
    }

    // ── Analytics: Bar Chart ──────────────────────────────────────────────────

    /** Daily totals for bar chart */
    public LiveData<List<ExpenseDao.DailyTotal>> getDailyTotals(long since) {
        return dao.getDailyTotals(userId, since);
    }

    /** Daily totals within range */
    public LiveData<List<ExpenseDao.DailyTotal>> getDailyTotalsInRange(long from, long to) {
        return dao.getDailyTotalsInRange(userId, from, to);
    }

    /** Monthly totals for year view */
    public LiveData<List<ExpenseDao.DailyTotal>> getMonthlyTotals(long from, long to) {
        return dao.getMonthlyTotals(userId, from, to);
    }

    // ── Totals ────────────────────────────────────────────────────────────────

    public LiveData<Double> getTotalAmount() {
        return dao.getTotalAmount(userId);
    }

    public LiveData<Double> getTotalAmountInRange(long from, long to) {
        return dao.getTotalAmountInRange(userId, from, to);
    }

    public LiveData<Integer> getExpenseCountInRange(long from, long to) {
        return dao.getExpenseCountInRange(userId, from, to);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Category defaults to "General" since user only provides title */
    public void addExpense(String title, double amount, String note, long date) {
        ExpenseEntity expense = new ExpenseEntity(
                UUID.randomUUID().toString(),
                userId, title, "General", amount, note, date);
        executor.execute(() -> {
            dao.insert(expense);
            sync.pushExpense(expense);
        });
    }

    /** Kept for backward compatibility */
    public void addExpense(String title, String category, double amount,
                           String note, long date) {
        ExpenseEntity expense = new ExpenseEntity(
                UUID.randomUUID().toString(),
                userId, title,
                (category != null && !category.isEmpty()) ? category : "General",
                amount, note, date);
        executor.execute(() -> {
            dao.insert(expense);
            sync.pushExpense(expense);
        });
    }

    public void updateExpense(ExpenseEntity expense) {
        expense.isSynced = false;
        executor.execute(() -> {
            dao.update(expense);
            sync.pushExpense(expense);
        });
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteExpense(ExpenseEntity expense) {
        executor.execute(() -> {
            dao.delete(expense);
            sync.deleteExpenseFromFirestore(expense.id);
        });
    }

    public void deleteAllExpenses() {
        executor.execute(() -> {
            List<ExpenseEntity> all = dao.getAllExpensesSync(userId);
            for (ExpenseEntity e : all) sync.deleteExpenseFromFirestore(e.id);
            dao.deleteAllByUser(userId);
        });
    }

    public void syncWithFirebase() { sync.syncAll(); }
}