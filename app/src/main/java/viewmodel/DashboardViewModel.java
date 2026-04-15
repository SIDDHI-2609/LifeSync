package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.lifesync.activities.database.ExpenseDao;
import com.example.lifesync.activities.models.NoteEntity;
import com.example.lifesync.activities.models.TodoEntity;

import java.util.List;

import repository.ExpenseRepository;
import repository.NoteRepository;
import repository.TodoRepository;

/**
 * Single ViewModel used by both MainActivity (dashboard) and DashboardActivity (full analytics).
 */
public class DashboardViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepo;
    private final TodoRepository todoRepo;
    private final NoteRepository noteRepo;

    // ── Expense ───────────────────────────────────────────────────────────────
    public final LiveData<List<ExpenseDao.TitleTotal>> categoryTotals;
    public final LiveData<List<ExpenseDao.DailyTotal>> dailyTotals;
    public final LiveData<Double>                      totalExpense;

    // ── Notes ─────────────────────────────────────────────────────────────────
    /** Last 3 notes — shown as previews on the dashboard */
    public final LiveData<List<NoteEntity>> recentNotes;

    // ── Todos ─────────────────────────────────────────────────────────────────
    public final LiveData<Integer>    totalTasks;
    public final LiveData<Integer>    completedTasks;

    /**
     * The next upcoming task that has an alarm in the future.
     * Derived from pendingTodos by picking the first (DAO sorts by alarmTimeMillis ASC).
     */
    public final LiveData<TodoEntity> upcomingAlarmTask;

    // ─────────────────────────────────────────────────────────────────────────

    public DashboardViewModel(@NonNull Application app) {
        super(app);
        expenseRepo = new ExpenseRepository(app);
        todoRepo    = new TodoRepository(app);
        noteRepo    = new NoteRepository(app);

        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);
        long sevenDaysAgo  = now - (7L * 24 * 60 * 60 * 1000);

        // Expense - Using TitleTotal for pie chart
        categoryTotals = expenseRepo.getTotalByTitleInRange(thirtyDaysAgo, now);
        dailyTotals    = expenseRepo.getDailyTotals(sevenDaysAgo);
        totalExpense   = expenseRepo.getTotalAmount();

        // Notes — take only the 3 most recent
        recentNotes = Transformations.map(noteRepo.getAllNotes(), notes -> {
            if (notes == null || notes.size() <= 3) return notes;
            return notes.subList(0, 3);
        });

        // Todos
        totalTasks     = todoRepo.getTotalCount();
        completedTasks = todoRepo.getCompletedCount();

        // Upcoming alarm task — first pending todo that has a future alarm
        upcomingAlarmTask = Transformations.map(todoRepo.getPendingTodos(), todos -> {
            if (todos == null) return null;
            for (TodoEntity t : todos) {
                if (t.alarmTimeMillis > now) return t; // already sorted ASC by alarm time
            }
            return null;
        });
    }

    public void syncAll() {
        expenseRepo.syncWithFirebase();
        todoRepo.syncWithFirebase();
        noteRepo.syncWithFirebase();
    }
}
