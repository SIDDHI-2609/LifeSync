package com.example.lifesync.activities.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.ExpenseAdapter;
import com.example.lifesync.activities.database.ExpenseDao;
import com.example.lifesync.activities.models.ExpenseEntity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import viewmodel.ExpenseViewModel;

public class ExpenseActivity extends AppCompatActivity
        implements ExpenseAdapter.OnExpenseActionListener {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView    tvTotalAmount, tvMonthAmount, tvMonthCount;
    private TextView    tvEmpty, tvPieEmpty, tvBarEmpty, tvListTitle;
    private PieChart    pieChart;
    private BarChart    barChart;
    private RecyclerView recyclerExpenses;
    private LinearLayout legendContainer;
    private MaterialButton btnDeleteAll;

    // Filter chips
    private TextView chipAll, chipFood, chipTransport, chipBills,
            chipShopping, chipHealth, chipOther;
    private TextView activeChip;

    // ── Data ──────────────────────────────────────────────────────────────────
    private ExpenseViewModel viewModel;
    private ExpenseAdapter adapter;

    // ── Category colors map ───────────────────────────────────────────────────
    private static final int[] CHART_COLORS = {
            0xFF4C6EF5, 0xFFFF6B6B, 0xFF4CAF50,
            0xFFFFB300, 0xFF7E57C2, 0xFF26C6DA
    };

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        bindViews();
        setupToolbar();
        setupCharts();
        setupRecycler();
        setupChips();
        setupFab();
        setupViewModel();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        tvTotalAmount   = findViewById(R.id.tvTotalAmount);
        tvMonthAmount   = findViewById(R.id.tvMonthAmount);
        tvMonthCount    = findViewById(R.id.tvMonthCount);
        tvEmpty         = findViewById(R.id.tvEmpty);
        tvPieEmpty      = findViewById(R.id.tvPieEmpty);
        tvBarEmpty      = findViewById(R.id.tvBarEmpty);
        tvListTitle     = findViewById(R.id.tvListTitle);
        pieChart        = findViewById(R.id.pieChart);
        barChart        = findViewById(R.id.barChart);
        recyclerExpenses= findViewById(R.id.recyclerExpenses);
        legendContainer = findViewById(R.id.legendContainer);
        btnDeleteAll    = findViewById(R.id.btnDeleteAll);

        chipAll       = findViewById(R.id.chipAll);
        chipFood      = findViewById(R.id.chipFood);
        chipTransport = findViewById(R.id.chipTransport);
        chipBills     = findViewById(R.id.chipBills);
        chipShopping  = findViewById(R.id.chipShopping);
        chipHealth    = findViewById(R.id.chipHealth);
        chipOther     = findViewById(R.id.chipOther);
        activeChip    = chipAll;
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // ── Charts ────────────────────────────────────────────────────────────────

    private void setupCharts() {
        // Pie
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(42f);
        pieChart.setTransparentCircleRadius(47f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText("Spending");
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(0xFF1B1F3B);
        pieChart.getLegend().setEnabled(false);  // custom legend below
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.animateY(900);

        // Bar
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(0xFF8E9BB5);
        barChart.getAxisLeft().setTextSize(9f);
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(0xFFEEEEEE);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(true);

        XAxis x = barChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setDrawAxisLine(false);
        x.setTextColor(0xFF8E9BB5);
        x.setTextSize(9f);
        x.setGranularity(1f);
        barChart.animateY(700);
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private void setupRecycler() {
        adapter = new ExpenseAdapter(this, this);
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerExpenses.setAdapter(adapter);
        recyclerExpenses.setNestedScrollingEnabled(false);
    }

    // ── Filter chips ──────────────────────────────────────────────────────────

    private void setupChips() {
        View.OnClickListener chipClick = v -> {
            String cat = "All";
            if (v == chipFood)      cat = "Food";
            else if (v == chipTransport) cat = "Transport";
            else if (v == chipBills)     cat = "Bills";
            else if (v == chipShopping)  cat = "Shopping";
            else if (v == chipHealth)    cat = "Health";
            else if (v == chipOther)     cat = "Other";

            selectChip((TextView) v, cat);
        };

        chipAll.setOnClickListener(chipClick);
        chipFood.setOnClickListener(chipClick);
        chipTransport.setOnClickListener(chipClick);
        chipBills.setOnClickListener(chipClick);
        chipShopping.setOnClickListener(chipClick);
        chipHealth.setOnClickListener(chipClick);
        chipOther.setOnClickListener(chipClick);
    }

    private void selectChip(TextView chip, String category) {
        // Reset previous
        activeChip.setBackgroundResource(R.drawable.chip_unselected);
        activeChip.setTextColor(0xFF555555);

        // Activate new
        chip.setBackgroundResource(R.drawable.chip_selected);
        chip.setTextColor(Color.WHITE);
        activeChip = chip;

        tvListTitle.setText(category.equals("All") ? "All Expenses" : category + " Expenses");
        viewModel.setFilter(category);
    }

    // ── FAB → bottom sheet to add expense ────────────────────────────────────

    private void setupFab() {
        ExtendedFloatingActionButton fab = findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v -> showAddExpenseSheet(null));
    }

    private void showAddExpenseSheet(ExpenseEntity editExpense) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_expense, null);
        sheet.setContentView(sheetView);

        TextInputEditText etAmount = sheetView.findViewById(R.id.etAmount);
        TextInputEditText etTitle  = sheetView.findViewById(R.id.etTitle);
        TextInputEditText etNote   = sheetView.findViewById(R.id.etNote);
        MaterialButton    btnSave  = sheetView.findViewById(R.id.btnSaveExpense);

        // Category selector
        final String[] selectedCategory = {"Food"};
        TextView[] catViews = {
                sheetView.findViewById(R.id.catFood),
                sheetView.findViewById(R.id.catTransport),
                sheetView.findViewById(R.id.catBills),
                sheetView.findViewById(R.id.catShopping),
                sheetView.findViewById(R.id.catHealth),
                sheetView.findViewById(R.id.catOther)
        };
        String[] catNames = {"Food","Transport","Bills","Shopping","Health","Other"};

        // Default select Food
        activateCatChip(catViews[0], catViews);

        for (int i = 0; i < catViews.length; i++) {
            final int idx = i;
            catViews[i].setOnClickListener(v -> {
                selectedCategory[0] = catNames[idx];
                activateCatChip(catViews[idx], catViews);
            });
        }

        // Pre-fill if editing
        if (editExpense != null) {
            etAmount.setText(String.valueOf(editExpense.amount));
            etTitle.setText(editExpense.title);
            etNote.setText(editExpense.note);
            btnSave.setText("Update Expense");
            selectedCategory[0] = editExpense.category;
            for (int i = 0; i < catNames.length; i++) {
                if (catNames[i].equals(editExpense.category)) {
                    activateCatChip(catViews[i], catViews);
                    break;
                }
            }
        }

        btnSave.setOnClickListener(v -> {
            String amtStr = etAmount.getText() != null
                    ? etAmount.getText().toString().trim() : "";
            String title  = etTitle.getText() != null
                    ? etTitle.getText().toString().trim() : "";
            String note   = etNote.getText() != null
                    ? etNote.getText().toString().trim() : "";

            if (amtStr.isEmpty()) {
                etAmount.setError("Enter amount");
                return;
            }
            if (title.isEmpty()) {
                etTitle.setError("Enter title");
                return;
            }

            double amount;
            try { amount = Double.parseDouble(amtStr); }
            catch (NumberFormatException e) {
                etAmount.setError("Invalid amount");
                return;
            }

            if (editExpense != null) {
                editExpense.title    = title;
                editExpense.category = selectedCategory[0];
                editExpense.amount   = amount;
                editExpense.note     = note;
                viewModel.updateExpense(editExpense);
                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.addExpense(title, selectedCategory[0],
                        amount, note, System.currentTimeMillis());
                Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show();
            }
            sheet.dismiss();
        });

        sheet.show();
    }

    /** Highlights the selected category chip inside the bottom sheet */
    private void activateCatChip(TextView selected, TextView[] all) {
        for (TextView tv : all) {
            tv.setBackgroundResource(R.drawable.chip_unselected);
            tv.setTextColor(0xFF555555);
        }
        selected.setBackgroundResource(R.drawable.chip_selected);
        selected.setTextColor(Color.WHITE);
    }

    // ── ViewModel ─────────────────────────────────────────────────────────────

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // ── Total amount ──────────────────────────────────────────────────────
        viewModel.totalAmount.observe(this, total -> {
            double t = total != null ? total : 0;
            tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", t));
        });

        // ── Pie chart + custom legend ─────────────────────────────────────────
        viewModel.categoryTotals.observe(this, totals -> {
            if (totals == null || totals.isEmpty()) {
                pieChart.setVisibility(View.GONE);
                tvPieEmpty.setVisibility(View.VISIBLE);
                legendContainer.removeAllViews();
                return;
            }
            pieChart.setVisibility(View.VISIBLE);
            tvPieEmpty.setVisibility(View.GONE);

            List<PieEntry> entries = new ArrayList<>();
            for (ExpenseDao.CategoryTotal ct : totals) {
                entries.add(new PieEntry((float) ct.total, ct.category));
            }
            PieDataSet ds = new PieDataSet(entries, "");
            ds.setColors(CHART_COLORS);
            ds.setValueTextColor(Color.WHITE);
            ds.setValueTextSize(11f);
            ds.setSliceSpace(3f);
            ds.setSelectionShift(6f);
            pieChart.setData(new PieData(ds));
            pieChart.invalidate();

            // Custom legend rows below pie
            legendContainer.removeAllViews();
            for (int i = 0; i < totals.size(); i++) {
                ExpenseDao.CategoryTotal ct = totals.get(i);
                legendContainer.addView(makeLegendRow(
                        ct.category,
                        ct.total,
                        CHART_COLORS[i % CHART_COLORS.length]));
            }
        });

        // ── Bar chart ─────────────────────────────────────────────────────────
        viewModel.dailyTotals.observe(this, totals -> {
            if (totals == null || totals.isEmpty()) {
                barChart.setVisibility(View.GONE);
                tvBarEmpty.setVisibility(View.VISIBLE);
                return;
            }
            barChart.setVisibility(View.VISIBLE);
            tvBarEmpty.setVisibility(View.GONE);

            List<BarEntry> entries = new ArrayList<>();
            List<String>   labels  = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("d/M", Locale.getDefault());
            for (int i = 0; i < totals.size(); i++) {
                ExpenseDao.DailyTotal dt = totals.get(i);
                entries.add(new BarEntry(i, (float) dt.total));
                labels.add(sdf.format(new Date(dt.date)));
            }
            BarDataSet ds = new BarDataSet(entries, "");
            ds.setColor(0xFF4C6EF5);
            ds.setDrawValues(false);

            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            // Show max 10 labels to avoid clutter
            barChart.getXAxis().setLabelCount(Math.min(labels.size(), 10), false);
            barChart.setData(new BarData(ds));
            barChart.animateY(600);
            barChart.invalidate();
        });

        // ── This month stats ──────────────────────────────────────────────────
        viewModel.allExpenses.observe(this, all -> {
            if (all == null) return;
            Calendar cal = Calendar.getInstance();
            int curMonth = cal.get(Calendar.MONTH);
            int curYear  = cal.get(Calendar.YEAR);
            double monthTotal = 0;
            int    monthCount = 0;
            for (ExpenseEntity e : all) {
                cal.setTimeInMillis(e.date);
                if (cal.get(Calendar.MONTH) == curMonth
                        && cal.get(Calendar.YEAR) == curYear) {
                    monthTotal += e.amount;
                    monthCount++;
                }
            }
            tvMonthAmount.setText(String.format(Locale.getDefault(), "₹%.2f", monthTotal));
            tvMonthCount.setText(monthCount + " expense" + (monthCount == 1 ? "" : "s"));

            // Show/hide delete all
            btnDeleteAll.setVisibility(all.isEmpty() ? View.GONE : View.VISIBLE);
        });

        // ── Filtered list (reacts to chip selection) ──────────────────────────
        viewModel.filteredExpenses.observe(this, list -> {
            if (list == null || list.isEmpty()) {
                recyclerExpenses.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerExpenses.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                adapter.submitList(list);
            }
        });

        // Delete all button
        btnDeleteAll.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete All Expenses")
                        .setMessage("This will permanently delete all your expense records.")
                        .setPositiveButton("Delete All", (d, w) -> {
                            viewModel.deleteAll();
                            Toast.makeText(this, "All expenses deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        viewModel.sync();
    }

    // ── Adapter callbacks ─────────────────────────────────────────────────────

    @Override
    public void onDelete(ExpenseEntity expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Delete \"" + expense.title + "\" (₹" + expense.amount + ")?")
                .setPositiveButton("Delete", (d, w) -> {
                    viewModel.deleteExpense(expense);
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEdit(ExpenseEntity expense) {
        showAddExpenseSheet(expense);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Creates a single legend row: colored dot + category name + total */
    private View makeLegendRow(String category, double total, int color) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(32));
        rp.setMargins(0, 2, 0, 2);
        row.setLayoutParams(rp);

        // Color dot
        View dot = new View(this);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(dpToPx(10), dpToPx(10));
        dp.setMarginEnd(dpToPx(10));
        dot.setLayoutParams(dp);
        dot.setBackgroundColor(color);

        // Category name
        TextView tvCat = new TextView(this);
        tvCat.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvCat.setText(category);
        tvCat.setTextSize(13f);
        tvCat.setTextColor(0xFF333333);

        // Amount
        TextView tvAmt = new TextView(this);
        tvAmt.setText(String.format(Locale.getDefault(), "₹%.2f", total));
        tvAmt.setTextSize(13f);
        tvAmt.setTextColor(0xFF4C6EF5);
        tvAmt.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(dot);
        row.addView(tvCat);
        row.addView(tvAmt);
        return row;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
