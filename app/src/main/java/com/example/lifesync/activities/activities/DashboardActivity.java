package com.example.lifesync.activities.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.lifesync.R;
import com.example.lifesync.activities.database.ExpenseDao;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import viewmodel.DashboardViewModel;

public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvTotalExpense, tvTotalTasks, tvCompletedTasks, tvPendingTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        pieChart         = findViewById(R.id.pieChart);
        barChart         = findViewById(R.id.barChart);
        tvTotalExpense   = findViewById(R.id.tvTotalExpense);
        tvTotalTasks     = findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvPendingTasks   = findViewById(R.id.tvPendingTasks);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupPieChart();
        setupBarChart();
        observeData();

        // Sync with Firebase on open
        viewModel.syncAll();
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private void observeData() {
        // Pie chart data
        viewModel.categoryTotals.observe(this, totals -> {
            if (totals == null || totals.isEmpty()) return;
            List<PieEntry> entries = new ArrayList<>();
            for (ExpenseDao.TitleTotal tt : totals) {
                entries.add(new PieEntry((float) tt.total, tt.title));
            }
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(chartColors());
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(12f);
            dataSet.setSliceSpace(3f);
            pieChart.setData(new PieData(dataSet));
            pieChart.invalidate();
        });

        // Bar chart data
        viewModel.dailyTotals.observe(this, totals -> {
            if (totals == null || totals.isEmpty()) return;
            List<BarEntry>  entries = new ArrayList<>();
            List<String>    labels  = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            for (int i = 0; i < totals.size(); i++) {
                ExpenseDao.DailyTotal dt = totals.get(i);
                entries.add(new BarEntry(i, (float) dt.total));
                labels.add(sdf.format(new Date(dt.date)));
            }
            BarDataSet dataSet = new BarDataSet(entries, "Daily Expenses");
            dataSet.setColor(0xFF4C6EF5);
            dataSet.setValueTextColor(Color.DKGRAY);
            dataSet.setValueTextSize(10f);

            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            barChart.setData(new BarData(dataSet));
            barChart.invalidate();
        });

        // Summary cards
        viewModel.totalExpense.observe(this, total -> {
            double amount = total != null ? total : 0;
            tvTotalExpense.setText(String.format(Locale.getDefault(), "₹%.2f", amount));
        });

        viewModel.totalTasks.observe(this, total -> {
            tvTotalTasks.setText(String.valueOf(total != null ? total : 0));
            updatePending();
        });

        viewModel.completedTasks.observe(this, done -> {
            tvCompletedTasks.setText(String.valueOf(done != null ? done : 0));
            updatePending();
        });
    }

    private void updatePending() {
        try {
            int total = Integer.parseInt(tvTotalTasks.getText().toString());
            int done  = Integer.parseInt(tvCompletedTasks.getText().toString());
            tvPendingTasks.setText(String.valueOf(total - done));
        } catch (NumberFormatException ignored) {}
    }

    // ── Chart setup ───────────────────────────────────────────────────────────

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(14f);
        pieChart.getLegend().setEnabled(true);
        pieChart.animateY(800);
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setGranularity(1f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        barChart.animateY(800);
    }

    private int[] chartColors() {
        return new int[]{
                0xFF4C6EF5, 0xFFFF6B6B, 0xFF4CAF50,
                0xFFFFB300, 0xFF7E57C2, 0xFF26C6DA
        };
    }
}