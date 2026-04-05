package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.lifesync.R;
import com.example.lifesync.activities.database.ExpenseDao;
import com.example.lifesync.activities.models.NoteEntity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import viewmodel.DashboardViewModel;

public class MainActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView         tvGreeting, tvUserName;
    private TextView         tvTotalExpense, tvExpenseSummary;
    private TextView         tvNotesCount, tvNotesEmpty, tvTodoSummary, tvTodoEmpty;
    private TextView         tvNote1Title, tvNote1Preview;
    private TextView         tvNote2Title, tvNote2Preview;
    private TextView         tvNote3Title, tvNote3Preview;
    private LinearLayout     noteRow1, noteRow2, noteRow3;
    private LinearLayout     layoutUpcomingTask;
    private TextView         tvUpcomingTaskTitle, tvUpcomingTaskTime;
    private ProgressBar      progressTasks;
    private BarChart         barChart;

    private DashboardViewModel viewModel;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Redirect to login if not signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bindViews();
        setupToolbar(user);
        setupBarChart();
        setupCardClicks();
        setupFab();
        setupViewModel();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        tvGreeting           = findViewById(R.id.tvGreeting);
        tvUserName           = findViewById(R.id.tvUserName);
        tvTotalExpense       = findViewById(R.id.tvTotalExpense);
        tvExpenseSummary     = findViewById(R.id.tvExpenseSummary);
        tvNotesCount         = findViewById(R.id.tvNotesCount);
        tvNotesEmpty         = findViewById(R.id.tvNotesEmpty);
        tvTodoSummary        = findViewById(R.id.tvTodoSummary);
        tvTodoEmpty          = findViewById(R.id.tvTodoEmpty);
        tvNote1Title         = findViewById(R.id.tvNote1Title);
        tvNote1Preview       = findViewById(R.id.tvNote1Preview);
        tvNote2Title         = findViewById(R.id.tvNote2Title);
        tvNote2Preview       = findViewById(R.id.tvNote2Preview);
        tvNote3Title         = findViewById(R.id.tvNote3Title);
        tvNote3Preview       = findViewById(R.id.tvNote3Preview);
        noteRow1             = findViewById(R.id.noteRow1);
        noteRow2             = findViewById(R.id.noteRow2);
        noteRow3             = findViewById(R.id.noteRow3);
        layoutUpcomingTask   = findViewById(R.id.layoutUpcomingTask);
        tvUpcomingTaskTitle  = findViewById(R.id.tvUpcomingTaskTitle);
        tvUpcomingTaskTime   = findViewById(R.id.tvUpcomingTaskTime);
        progressTasks        = findViewById(R.id.progressTasks);
        barChart             = findViewById(R.id.barChart);
    }

    // ── Toolbar + profile popup ───────────────────────────────────────────────

    private void setupToolbar(FirebaseUser user) {
        // Greeting based on time of day
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good morning 👋"
                : hour < 17 ? "Good afternoon 👋"
                : "Good evening 👋";
        tvGreeting.setText(greeting);

        // Username: use display name or email prefix
        String name = user.getDisplayName();
        if (name == null || name.isEmpty()) {
            String email = user.getEmail();
            name = email != null ? email.split("@")[0] : "User";
        }
        // Capitalize first letter
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        tvUserName.setText(name);

        // Profile icon → popup menu
        findViewById(R.id.ivProfile).setOnClickListener(this::showProfileMenu);
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "⚙️  Settings");
        popup.getMenu().add(0, 2, 1, "🎨  Theme");
        popup.getMenu().add(0, 3, 2, "🚪  Logout");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    // TODO: startActivity(new Intent(this, SettingsActivity.class));
                    Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
                    return true;
                case 2:
                    // TODO: open theme picker
                    Toast.makeText(this, "Theme coming soon", Toast.LENGTH_SHORT).show();
                    return true;
                case 3:
                    logout();
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Bar chart setup ───────────────────────────────────────────────────────

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setTouchEnabled(false);  // dashboard is non-interactive, just preview

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(0xFF8E9BB5);
        xAxis.setTextSize(9f);
        xAxis.setGranularity(1f);
    }

    // ── Card click navigation ─────────────────────────────────────────────────

    private void setupCardClicks() {
        // Expense card → Expense Activity
        findViewById(R.id.cardExpense).setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseActivity.class)));

        // Notes card → Notes Activity
        findViewById(R.id.cardNotes).setOnClickListener(v ->
                startActivity(new Intent(this, NotesActivity.class)));

        // Todo card → Todo Activity
        findViewById(R.id.cardTodo).setOnClickListener(v ->
                startActivity(new Intent(this, TodoActivity.class)));
    }

    // ── FAB: quick-add popup ──────────────────────────────────────────────────

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, fab);
            popup.getMenu().add(0, 1, 0, "💸  Add Expense");
            popup.getMenu().add(0, 2, 1, "📝  Add Note");
            popup.getMenu().add(0, 3, 2, "✅  Add Task");
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1: startActivity(new Intent(this, ExpenseActivity.class)); return true;
                    case 2: startActivity(new Intent(this, NotesActivity.class));   return true;
                    case 3: startActivity(new Intent(this, TodoActivity.class));    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    // ── ViewModel + LiveData ──────────────────────────────────────────────────

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // ── Expense total ─────────────────────────────────────────────────────
        viewModel.totalExpense.observe(this, total -> {
            double amt = total != null ? total : 0.0;
            tvTotalExpense.setText(String.format(Locale.getDefault(), "₹%.2f", amt));
            tvExpenseSummary.setText(amt == 0
                    ? "No expenses recorded yet"
                    : "Total recorded spending");
        });

        // ── Bar chart data ────────────────────────────────────────────────────
        viewModel.dailyTotals.observe(this, totals -> {
            if (totals == null || totals.isEmpty()) return;
            List<BarEntry> entries = new ArrayList<>();
            List<String>   labels  = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            for (int i = 0; i < totals.size(); i++) {
                ExpenseDao.DailyTotal dt = totals.get(i);
                entries.add(new BarEntry(i, (float) dt.total));
                labels.add(sdf.format(new Date(dt.date)));
            }
            BarDataSet ds = new BarDataSet(entries, "");
            ds.setColor(0xFF4C6EF5);
            ds.setDrawValues(false);
//            ds.setBarBorderRadius(4f);

            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            barChart.setData(new BarData(ds));
            barChart.animateY(600);
            barChart.invalidate();
        });

        // ── Notes preview (up to 3) ───────────────────────────────────────────
        viewModel.recentNotes.observe(this, notes -> {
            if (notes == null || notes.isEmpty()) {
                tvNotesCount.setText("0 notes");
                tvNotesEmpty.setVisibility(View.VISIBLE);
                noteRow1.setVisibility(View.GONE);
                noteRow2.setVisibility(View.GONE);
                noteRow3.setVisibility(View.GONE);
                return;
            }
            tvNotesEmpty.setVisibility(View.GONE);
            tvNotesCount.setText(notes.size() + " note" + (notes.size() == 1 ? "" : "s"));
            bindNoteRow(0, notes, noteRow1, tvNote1Title, tvNote1Preview);
            bindNoteRow(1, notes, noteRow2, tvNote2Title, tvNote2Preview);
            bindNoteRow(2, notes, noteRow3, tvNote3Title, tvNote3Preview);
        });

        // ── Tasks summary + progress ──────────────────────────────────────────
        viewModel.totalTasks.observe(this, total -> updateTaskSummary());
        viewModel.completedTasks.observe(this, done -> updateTaskSummary());

        // ── Upcoming task with alarm ──────────────────────────────────────────
        viewModel.upcomingAlarmTask.observe(this, task -> {
            if (task == null) {
                layoutUpcomingTask.setVisibility(View.GONE);
                return;
            }
            layoutUpcomingTask.setVisibility(View.VISIBLE);
            tvUpcomingTaskTitle.setText(task.title);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM  hh:mm a", Locale.getDefault());
            tvUpcomingTaskTime.setText(sdf.format(new Date(task.alarmTimeMillis)));
        });

        // Sync everything on open
        viewModel.syncAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void bindNoteRow(int index, List<NoteEntity> notes,
                             LinearLayout row, TextView tvTitle, TextView tvPreview) {
        if (index < notes.size()) {
            NoteEntity note = notes.get(index);
            row.setVisibility(View.VISIBLE);
            tvTitle.setText(note.title != null ? note.title : "Untitled");
            String preview = note.content != null
                    ? note.content.replace("\n", " ") : "";
            tvPreview.setText(preview.length() > 30
                    ? preview.substring(0, 30) + "…" : preview);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateTaskSummary() {
        Integer total = viewModel.totalTasks.getValue();
        Integer done  = viewModel.completedTasks.getValue();
        int t = total != null ? total : 0;
        int d = done  != null ? done  : 0;
        int pending = t - d;

        tvTodoSummary.setText(pending + " pending · " + d + " done");

        if (t == 0) {
            tvTodoEmpty.setVisibility(View.VISIBLE);
            progressTasks.setProgress(0);
        } else {
            tvTodoEmpty.setVisibility(View.GONE);
            int progress = (int) ((d / (float) t) * 100);
            progressTasks.setProgress(progress);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data every time user comes back from another screen
        if (viewModel != null) viewModel.syncAll();
    }
}