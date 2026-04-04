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

    public LiveData<List<ExpenseEntity>> getByCategory(String category) {
        return dao.getExpensesByCategory(userId, category);
    }

    public LiveData<List<ExpenseEntity>> getByDateRange(long from, long to) {
        return dao.getExpensesByDateRange(userId, from, to);
    }

    /** For Pie Chart — total per category */
    public LiveData<List<ExpenseDao.CategoryTotal>> getTotalByCategory() {
        return dao.getTotalByCategory(userId);
    }

    /** For Bar Chart — daily totals last 30 days */
    public LiveData<List<ExpenseDao.DailyTotal>> getDailyTotals() {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        return dao.getDailyTotals(userId, thirtyDaysAgo);
    }

    public LiveData<Double> getTotalAmount() {
        return dao.getTotalAmount(userId);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public void addExpense(String title, String category, double amount,
                           String note, long date) {
        ExpenseEntity expense = new ExpenseEntity(
                UUID.randomUUID().toString(),
                userId, title, category, amount, note, date);
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
