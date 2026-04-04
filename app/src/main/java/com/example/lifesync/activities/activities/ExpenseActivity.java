package com.example.lifesync.activities.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.ExpenseAdapter;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.models.ExpenseEntity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class ExpenseActivity extends BaseActivity {

    AppDatabase db ;
    RecyclerView recyclerView;
    TextView tvTotal, tvMonthly;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        setupToolbar();

        // Get Current User ID from Firebase
        userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "smartassistant_db").allowMainThreadQueries().build();

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etAmount = findViewById(R.id.etAmount);
        EditText etCategory = findViewById(R.id.etCategory);
        tvTotal = findViewById(R.id.tvTotal);
        tvMonthly = findViewById(R.id.tvMonthly);
        Button btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadExpenses();
        loadAnalytics();

        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = etCategory.getText().toString().trim();

            if (title.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                ExpenseEntity expense = new ExpenseEntity(
                        UUID.randomUUID().toString(),
                        userId,
                        title,
                        category,
                        amount,
                        "", // note
                        System.currentTimeMillis()
                );

                db.expenseDao().insert(expense);

                Toast.makeText(this, "Expense Saved", Toast.LENGTH_SHORT).show();

                etTitle.setText("");
                etAmount.setText("");
                etCategory.setText("");
                loadExpenses();
                loadAnalytics();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadExpenses() {
        // Use the sync method and pass userId
        List<ExpenseEntity> expenses = db.expenseDao().getAllExpensesSync(userId);
        ExpenseAdapter adapter = new ExpenseAdapter(expenses, this::deleteExpense);
        recyclerView.setAdapter(adapter);
    }

    private void deleteExpense(ExpenseEntity expense) {
        db.expenseDao().delete(expense);
        loadExpenses();
        loadAnalytics();
    }

    private void loadAnalytics() {
        // Pass userId to Dao methods
        double total = db.expenseDao().getTotalExpenses(userId);
        long startOfMonth = getStartOfMonth();
        double monthly = db.expenseDao().getMonthlyExpenses(userId, startOfMonth);
        tvTotal.setText("Total: ₹" + total);
        tvMonthly.setText("Monthly: ₹" + monthly);
    }

    private long getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
