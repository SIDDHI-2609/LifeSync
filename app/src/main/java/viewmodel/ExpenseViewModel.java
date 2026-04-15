package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.lifesync.activities.database.ExpenseDao;
import com.example.lifesync.activities.models.ExpenseEntity;

import java.util.Calendar;
import java.util.List;

import repository.ExpenseRepository;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repo;

    // ── Time filter state ─────────────────────────────────────────────────────
    public enum TimeFilter { DAY, WEEK, MONTH, YEAR }

    private final MutableLiveData<TimeFilter> selectedTimeFilter =
            new MutableLiveData<>(TimeFilter.MONTH);

    // ── Exposed LiveData ──────────────────────────────────────────────────────
    public final LiveData<List<ExpenseEntity>>         allExpenses;
    public final LiveData<Double>                      totalAmount;

    /** Title-based pie chart data — reacts to time filter */
    public final LiveData<List<ExpenseDao.TitleTotal>> titleTotals;

    /** Bar chart data — reacts to time filter */
    public final LiveData<List<ExpenseDao.DailyTotal>> barChartData;

    /** Filtered expense list — reacts to time filter */
    public final LiveData<List<ExpenseEntity>>         filteredExpenses;

    /** Period total amount */
    public final LiveData<Double>                      periodTotal;

    /** Period expense count */
    public final LiveData<Integer>                     periodCount;

    /** Current time range (for display purposes) */
    public final LiveData<long[]> currentRange;

    public ExpenseViewModel(@NonNull Application app) {
        super(app);
        repo = new ExpenseRepository(app);

        allExpenses = repo.getAllExpenses();
        totalAmount = repo.getTotalAmount();

        // Compute date range from time filter
        currentRange = Transformations.map(selectedTimeFilter, this::getDateRange);

        // ── Pie Chart: title-based, within selected time range ────────────────
        titleTotals = Transformations.switchMap(selectedTimeFilter, filter -> {
            long[] range = getDateRange(filter);
            return repo.getTotalByTitleInRange(range[0], range[1]);
        });

        // ── Bar Chart: adapts to time filter ──────────────────────────────────
        barChartData = Transformations.switchMap(selectedTimeFilter, filter -> {
            long[] range = getDateRange(filter);
            if (filter == TimeFilter.YEAR) {
                return repo.getMonthlyTotals(range[0], range[1]);
            } else {
                return repo.getDailyTotalsInRange(range[0], range[1]);
            }
        });

        // ── Filtered expense list ─────────────────────────────────────────────
        filteredExpenses = Transformations.switchMap(selectedTimeFilter, filter -> {
            long[] range = getDateRange(filter);
            return repo.getByDateRange(range[0], range[1]);
        });

        // ── Period total ──────────────────────────────────────────────────────
        periodTotal = Transformations.switchMap(selectedTimeFilter, filter -> {
            long[] range = getDateRange(filter);
            return repo.getTotalAmountInRange(range[0], range[1]);
        });

        // ── Period count ──────────────────────────────────────────────────────
        periodCount = Transformations.switchMap(selectedTimeFilter, filter -> {
            long[] range = getDateRange(filter);
            return repo.getExpenseCountInRange(range[0], range[1]);
        });
    }

    // ── Time filter helpers ───────────────────────────────────────────────────

    public void setTimeFilter(TimeFilter filter) {
        selectedTimeFilter.setValue(filter);
    }

    public TimeFilter getTimeFilter() {
        return selectedTimeFilter.getValue();
    }

    /**
     * Returns [startMillis, endMillis] for the given time filter.
     */
    private long[] getDateRange(TimeFilter filter) {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd   = Calendar.getInstance();

        // End = end of today
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);

        switch (filter) {
            case DAY:
                // Start of today
                calStart.set(Calendar.HOUR_OF_DAY, 0);
                calStart.set(Calendar.MINUTE, 0);
                calStart.set(Calendar.SECOND, 0);
                calStart.set(Calendar.MILLISECOND, 0);
                break;

            case WEEK:
                // Start of this week (Monday)
                calStart.set(Calendar.DAY_OF_WEEK, calStart.getFirstDayOfWeek());
                calStart.set(Calendar.HOUR_OF_DAY, 0);
                calStart.set(Calendar.MINUTE, 0);
                calStart.set(Calendar.SECOND, 0);
                calStart.set(Calendar.MILLISECOND, 0);
                break;

            case MONTH:
                // Start of this month
                calStart.set(Calendar.DAY_OF_MONTH, 1);
                calStart.set(Calendar.HOUR_OF_DAY, 0);
                calStart.set(Calendar.MINUTE, 0);
                calStart.set(Calendar.SECOND, 0);
                calStart.set(Calendar.MILLISECOND, 0);
                break;

            case YEAR:
                // Start of this year
                calStart.set(Calendar.MONTH, Calendar.JANUARY);
                calStart.set(Calendar.DAY_OF_MONTH, 1);
                calStart.set(Calendar.HOUR_OF_DAY, 0);
                calStart.set(Calendar.MINUTE, 0);
                calStart.set(Calendar.SECOND, 0);
                calStart.set(Calendar.MILLISECOND, 0);
                break;
        }

        return new long[]{ calStart.getTimeInMillis(), calEnd.getTimeInMillis() };
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /** New method — no category needed */
    public void addExpense(String title, double amount, String note, long date) {
        repo.addExpense(title, amount, note, date);
    }

    /** Backward compatible */
    public void addExpense(String title, String category, double amount,
                           String note, long date) {
        repo.addExpense(title, category, amount, note, date);
    }

    public void updateExpense(ExpenseEntity e) { repo.updateExpense(e); }
    public void deleteExpense(ExpenseEntity e) { repo.deleteExpense(e); }
    public void deleteAll()                    { repo.deleteAllExpenses(); }
    public void sync()                         { repo.syncWithFirebase(); }
}