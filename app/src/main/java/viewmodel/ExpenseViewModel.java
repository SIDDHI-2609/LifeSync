package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.lifesync.activities.database.ExpenseDao;
import com.example.lifesync.activities.models.ExpenseEntity;

import java.util.List;

import repository.ExpenseRepository;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repo;

    // ── Filter state ──────────────────────────────────────────────────────────
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("All");

    // ── Exposed LiveData ──────────────────────────────────────────────────────
    public final LiveData<List<ExpenseEntity>>          allExpenses;
    public final LiveData<List<ExpenseDao.CategoryTotal>> categoryTotals;
    public final LiveData<List<ExpenseDao.DailyTotal>>    dailyTotals;
    public final LiveData<Double>                         totalAmount;

    /** Filtered list — reacts to selectedCategory changes */
    public final LiveData<List<ExpenseEntity>> filteredExpenses;

    public ExpenseViewModel(@NonNull Application app) {
        super(app);
        repo = new ExpenseRepository(app);

        allExpenses    = repo.getAllExpenses();
        categoryTotals = repo.getTotalByCategory();
        dailyTotals    = repo.getDailyTotals();
        totalAmount    = repo.getTotalAmount();

        // Switch source based on selected category
        filteredExpenses = Transformations.switchMap(selectedCategory, cat -> {
            if (cat == null || cat.equals("All")) return repo.getAllExpenses();
            return repo.getByCategory(cat);
        });
    }

    public void setFilter(String category) { selectedCategory.setValue(category); }
    public String getFilter() { return selectedCategory.getValue(); }

    public void addExpense(String title, String category, double amount,
                           String note, long date) {
        repo.addExpense(title, category, amount, note, date);
    }

    public void updateExpense(ExpenseEntity e) { repo.updateExpense(e); }
    public void deleteExpense(ExpenseEntity e) { repo.deleteExpense(e); }
    public void deleteAll()                    { repo.deleteAllExpenses(); }
    public void sync()                         { repo.syncWithFirebase(); }
}
