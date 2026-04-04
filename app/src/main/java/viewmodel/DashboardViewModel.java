package viewmodel;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.lifesync.activities.database.ExpenseDao;

import java.util.List;

import repository.ExpenseRepository;
import repository.NoteRepository;
import repository.TodoRepository;

/**
 * ViewModel for the Dashboard screen.
 * Exposes LiveData that the Dashboard Activity observes to build charts.
 *
 * Usage in DashboardActivity:
 *
 *   DashboardViewModel vm = new ViewModelProvider(this).get(DashboardViewModel.class);
 *   vm.getCategoryTotals().observe(this, totals -> buildPieChart(totals));
 *   vm.getDailyTotals().observe(this,    totals -> buildBarChart(totals));
 *   vm.getTotalExpense().observe(this,   total  -> tvTotal.setText("₹" + total));
 *   vm.getTotalTasks().observe(this,     count  -> tvTasks.setText(count + " tasks"));
 *   vm.getCompletedTasks().observe(this, count  -> tvDone.setText(count + " done"));
 */
public class DashboardViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepo;
    private final TodoRepository todoRepo;
    private final NoteRepository noteRepo;

    // ── Expense analytics ─────────────────────────────────────────────────────
    public final LiveData<List<ExpenseDao.CategoryTotal>> categoryTotals;
    public final LiveData<List<ExpenseDao.DailyTotal>>    dailyTotals;
    public final LiveData<Double>              totalExpense;

    // ── To-Do analytics ───────────────────────────────────────────────────────
    public final LiveData<Integer> totalTasks;
    public final LiveData<Integer> completedTasks;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        expenseRepo    = new ExpenseRepository(application);
        todoRepo       = new TodoRepository(application);
        noteRepo       = new NoteRepository(application);

        categoryTotals = expenseRepo.getTotalByCategory();
        dailyTotals    = expenseRepo.getDailyTotals();
        totalExpense   = expenseRepo.getTotalAmount();
        totalTasks     = todoRepo.getTotalCount();
        completedTasks = todoRepo.getCompletedCount();
    }

    public void syncAll() {
        expenseRepo.syncWithFirebase();
        todoRepo.syncWithFirebase();
        noteRepo.syncWithFirebase();
    }
}
