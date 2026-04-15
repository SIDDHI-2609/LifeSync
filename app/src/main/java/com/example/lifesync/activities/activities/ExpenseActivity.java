package com.example.lifesync.activities.activities;

import android.graphics.Color;
import android.os.Bundle;
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
    private TextView     tvTotalAmount, tvPeriodAmount, tvPeriodCount, tvPeriodLabel;
    private TextView     tvEmpty, tvPieEmpty, tvBarEmpty, tvListTitle;
    private TextView     tvBarTitle, tvBarSubtitle;
    private PieChart     pieChart;
    private BarChart     barChart;
    private RecyclerView recyclerExpenses;
    private LinearLayout legendContainer;
    private MaterialButton btnDeleteAll;

    // Time filter tabs
    private TextView tabDay, tabWeek, tabMonth, tabYear;
    private TextView activeTab;

    // ── Data ──────────────────────────────────────────────────────────────────
    private ExpenseViewModel viewModel;
    private ExpenseAdapter   adapter;

    // ── Colors for pie chart ──────────────────────────────────────────────────
    private static final int[] CHART_COLORS = {
            0xFF4C6EF5, 0xFFFF6B6B, 0xFF4CAF50,
            0xFFFFB300, 0xFF7E57C2, 0xFF26C6DA,
            0xFFE91E63, 0xFF00BCD4, 0xFF8BC34A,
            0xFFFF5722, 0xFF3F51B5, 0xFF009688
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        bindViews();
        setupToolbar();
        setupCharts();
        setupRecycler();
        setupTimeTabs();
        setupFab();
        setupViewModel();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        tvTotalAmount  = findViewById(R.id.tvTotalAmount);
        tvPeriodAmount = findViewById(R.id.tvPeriodAmount);
        tvPeriodCount  = findViewById(R.id.tvPeriodCount);
        tvPeriodLabel  = findViewById(R.id.tvPeriodLabel);
        tvEmpty        = findViewById(R.id.tvEmpty);
        tvPieEmpty     = findViewById(R.id.tvPieEmpty);
        tvBarEmpty     = findViewById(R.id.tvBarEmpty);
        tvListTitle    = findViewById(R.id.tvListTitle);
        tvBarTitle     = findViewById(R.id.tvBarTitle);
        tvBarSubtitle  = findViewById(R.id.tvBarSubtitle);
        pieChart       = findViewById(R.id.pieChart);
        barChart       = findViewById(R.id.barChart);
        recyclerExpenses = findViewById(R.id.recyclerExpenses);
        legendContainer  = findViewById(R.id.legendContainer);
        btnDeleteAll     = findViewById(R.id.btnDeleteAll);

        tabDay   = findViewById(R.id.tabDay);
        tabWeek  = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);
        tabYear  = findViewById(R.id.tabYear);
        activeTab = tabMonth; // default
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
        pieChart.getLegend().setEnabled(false);
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

    // ── Time Filter Tabs ──────────────────────────────────────────────────────

    private void setupTimeTabs() {
        View.OnClickListener tabClick = v -> {
            ExpenseViewModel.TimeFilter filter;
            if (v == tabDay)        filter = ExpenseViewModel.TimeFilter.DAY;
            else if (v == tabWeek)  filter = ExpenseViewModel.TimeFilter.WEEK;
            else if (v == tabYear)  filter = ExpenseViewModel.TimeFilter.YEAR;
            else                    filter = ExpenseViewModel.TimeFilter.MONTH;

            selectTab((TextView) v, filter);
        };

        tabDay.setOnClickListener(tabClick);
        tabWeek.setOnClickListener(tabClick);
        tabMonth.setOnClickListener(tabClick);
        tabYear.setOnClickListener(tabClick);
    }

    private void selectTab(TextView tab, ExpenseViewModel.TimeFilter filter) {
        // Reset previous
        activeTab.setBackgroundResource(R.drawable.chip_unselected);
        activeTab.setTextColor(0xFF555555);

        // Activate new
        tab.setBackgroundResource(R.drawable.chip_selected);
        tab.setTextColor(Color.WHITE);
        activeTab = tab;

        // Update labels
        updateLabelsForFilter(filter);

        // Trigger ViewModel filter change
        viewModel.setTimeFilter(filter);
    }

    private void updateLabelsForFilter(ExpenseViewModel.TimeFilter filter) {
        switch (filter) {
            case DAY:
                tvPeriodLabel.setText("TODAY");
                tvBarTitle.setText("Hourly Spending");
                tvBarSubtitle.setText("Today");
                tvListTitle.setText("Today's Expenses");
                break;
            case WEEK:
                tvPeriodLabel.setText("THIS WEEK");
                tvBarTitle.setText("Daily Spending");
                tvBarSubtitle.setText("This week");
                tvListTitle.setText("This Week's Expenses");
                break;
            case MONTH:
                tvPeriodLabel.setText("THIS MONTH");
                tvBarTitle.setText("Daily Spending");
                tvBarSubtitle.setText("This month");
                tvListTitle.setText("This Month's Expenses");
                break;
            case YEAR:
                tvPeriodLabel.setText("THIS YEAR");
                tvBarTitle.setText("Monthly Spending");
                tvBarSubtitle.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                tvListTitle.setText("This Year's Expenses");
                break;
        }
    }

    // ── FAB → Bottom sheet to add expense (NO CATEGORY) ──────────────────────

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

        // Pre-fill if editing
        if (editExpense != null) {
            etAmount.setText(String.valueOf(editExpense.amount));
            etTitle.setText(editExpense.title);
            etNote.setText(editExpense.note);
            btnSave.setText("Update Expense");
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
                editExpense.title  = title;
                editExpense.amount = amount;
                editExpense.note   = note;
                viewModel.updateExpense(editExpense);
                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.addExpense(title, amount, note, System.currentTimeMillis());
                Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show();
            }
            sheet.dismiss();
        });

        sheet.show();
    }

    // ── ViewModel ─────────────────────────────────────────────────────────────

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        // ── Total amount (all time) ───────────────────────────────────────────
        viewModel.totalAmount.observe(this, total -> {
            double t = total != null ? total : 0;
            tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", t));
        });

        // ── Period total ──────────────────────────────────────────────────────
        viewModel.periodTotal.observe(this, total -> {
            double t = total != null ? total : 0;
            tvPeriodAmount.setText(String.format(Locale.getDefault(), "₹%.2f", t));
        });

        // ── Period count ──────────────────────────────────────────────────────
        viewModel.periodCount.observe(this, count -> {
            int c = count != null ? count : 0;
            tvPeriodCount.setText(c + " expense" + (c == 1 ? "" : "s"));
        });

        // ── Pie chart (TITLE-based) + custom legend ───────────────────────────
        viewModel.titleTotals.observe(this, totals -> {
            if (totals == null || totals.isEmpty()) {
                pieChart.setVisibility(View.GONE);
                tvPieEmpty.setVisibility(View.VISIBLE);
                legendContainer.removeAllViews();
                return;
            }
            pieChart.setVisibility(View.VISIBLE);
            tvPieEmpty.setVisibility(View.GONE);

            List<PieEntry> entries = new ArrayList<>();
            for (ExpenseDao.TitleTotal tt : totals) {
                String label = tt.title != null && !tt.title.isEmpty() ? tt.title : "Untitled";
                entries.add(new PieEntry((float) tt.total, label));
            }
            PieDataSet ds = new PieDataSet(entries, "");
            ds.setColors(CHART_COLORS);
            ds.setValueTextColor(Color.WHITE);
            ds.setValueTextSize(11f);
            ds.setSliceSpace(3f);
            ds.setSelectionShift(6f);
            pieChart.setData(new PieData(ds));
            pieChart.invalidate();

            // Custom legend
            legendContainer.removeAllViews();
            for (int i = 0; i < totals.size(); i++) {
                ExpenseDao.TitleTotal tt = totals.get(i);
                String label = tt.title != null && !tt.title.isEmpty() ? tt.title : "Untitled";
                legendContainer.addView(makeLegendRow(
                        label, tt.total,
                        CHART_COLORS[i % CHART_COLORS.length]));
            }
        });

        // ── Bar chart (reacts to time filter) ─────────────────────────────────
        viewModel.barChartData.observe(this, totals -> {
            if (totals == null || totals.isEmpty()) {
                barChart.setVisibility(View.GONE);
                tvBarEmpty.setVisibility(View.VISIBLE);
                return;
            }
            barChart.setVisibility(View.VISIBLE);
            tvBarEmpty.setVisibility(View.GONE);

            ExpenseViewModel.TimeFilter filter = viewModel.getTimeFilter();
            List<BarEntry> entries = new ArrayList<>();
            List<String>   labels  = new ArrayList<>();

            SimpleDateFormat sdf;
            switch (filter) {
                case DAY:
                    sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    break;
                case WEEK:
                    sdf = new SimpleDateFormat("EEE", Locale.getDefault());
                    break;
                case YEAR:
                    sdf = new SimpleDateFormat("MMM", Locale.getDefault());
                    break;
                default: // MONTH
                    sdf = new SimpleDateFormat("d/M", Locale.getDefault());
                    break;
            }

            for (int i = 0; i < totals.size(); i++) {
                ExpenseDao.DailyTotal dt = totals.get(i);
                entries.add(new BarEntry(i, (float) dt.total));
                labels.add(sdf.format(new Date(dt.date)));
            }

            BarDataSet ds = new BarDataSet(entries, "");
            ds.setColor(0xFF4C6EF5);
            ds.setDrawValues(false);

            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            barChart.getXAxis().setLabelCount(Math.min(labels.size(), 10), false);
            barChart.setData(new BarData(ds));
            barChart.animateY(600);
            barChart.invalidate();
        });

        // ── Filtered expense list ─────────────────────────────────────────────
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

        // ── Delete all button visibility ──────────────────────────────────────
        viewModel.allExpenses.observe(this, all -> {
            btnDeleteAll.setVisibility(
                    (all != null && !all.isEmpty()) ? View.VISIBLE : View.GONE);
        });

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

        // Set default labels
        updateLabelsForFilter(ExpenseViewModel.TimeFilter.MONTH);
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

    private View makeLegendRow(String title, double total, int color) {
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

        // Title name
        TextView tvTitle = new TextView(this);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvTitle.setText(title);
        tvTitle.setTextSize(13f);
        tvTitle.setTextColor(0xFF333333);

        // Amount
        TextView tvAmt = new TextView(this);
        tvAmt.setText(String.format(Locale.getDefault(), "₹%.2f", total));
        tvAmt.setTextSize(13f);
        tvAmt.setTextColor(0xFF4C6EF5);
        tvAmt.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(dot);
        row.addView(tvTitle);
        row.addView(tvAmt);
        return row;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}