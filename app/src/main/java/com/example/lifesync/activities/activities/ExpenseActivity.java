package com.example.lifesync.activities.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.ExpenseAdapter;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.models.Expense;

import java.util.Calendar;
import java.util.List;

public class ExpenseActivity extends BaseActivity {

    AppDatabase db ;
    RecyclerView recyclerView;
    TextView tvTotal, tvMonthly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        setupToolbar();

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "lifesync-db").allowMainThreadQueries().build();
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etAmount = findViewById(R.id.etAmount);
        EditText etCategory = findViewById(R.id.etCategory);
        tvTotal = findViewById(R.id.tvTotal);
        tvMonthly = findViewById(R.id.tvMonthly);
        Button btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadExpenses();

        btnAdd.setOnClickListener(v -> {
            Expense expense = new Expense();
            expense.title = etTitle.getText().toString();
            expense.amount = Double.parseDouble(etAmount.getText().toString());
            expense.date = System.currentTimeMillis();
            expense.category = etCategory.getText().toString();

            db.expenseDao().insert(expense);

            Toast.makeText(this, "Expense Saved", Toast.LENGTH_SHORT).show();

            etTitle.setText("");
            etAmount.setText("");
            etCategory.setText("");
            loadExpenses();
            loadAnalytics();
        });

    }
    private void loadExpenses() {
        List<Expense> expenses = db.expenseDao().getAllExpenses();
        ExpenseAdapter adapter = new ExpenseAdapter(expenses, this::deleteExpense);
        recyclerView.setAdapter(adapter);
    }
    private void deleteExpense(Expense expense) {
        db.expenseDao().delete(expense);
        loadExpenses();
        loadAnalytics();
    }
    private void loadAnalytics() {
        List<Expense> expenses = db.expenseDao().getAllExpenses();
        double total = db.expenseDao().getTotalExpenses();
        long startOfMonth = getStartOfMonth();
        double monthly = db.expenseDao().getMonthlyExpenses(startOfMonth);
        tvTotal.setText("Total: ₹" + total);
        tvMonthly.setText("Monthly: ₹" + monthly);
    }
    private long getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }
}
