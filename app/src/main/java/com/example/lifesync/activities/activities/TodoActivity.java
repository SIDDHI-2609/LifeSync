package com.example.lifesync.activities.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.TodoAdapter;
import com.example.lifesync.activities.helper.AlarmHelper;
import com.example.lifesync.activities.models.TodoEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import viewmodel.TodoViewModel;

public class TodoActivity extends AppCompatActivity
        implements TodoAdapter.OnTaskActionListener {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextInputEditText etTaskTitle, etTaskDescription;
    private TextView          tvAlarmTime, tvTaskCount, tvEmpty;
    private MaterialButton    btnSetAlarm, btnSaveTask, btnDeleteAll;
    private ImageButton       btnCancelAlarm;
    private RecyclerView      recyclerTasks;

    // ── Data ──────────────────────────────────────────────────────────────────
    private TodoViewModel viewModel;
    private TodoAdapter adapter;

    // ── Alarm selection ───────────────────────────────────────────────────────
    private Calendar selectedAlarmCalendar = null;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        bindViews();
        setupRecyclerView();
        setupListeners();
        setupViewModel();        // ← wires Room LiveData
        requestPermissions();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        etTaskTitle       = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        tvAlarmTime       = findViewById(R.id.tvAlarmTime);
        btnSetAlarm       = findViewById(R.id.btnSetAlarm);
        btnCancelAlarm    = findViewById(R.id.btnCancelAlarm);
        btnSaveTask       = findViewById(R.id.btnSaveTask);
        recyclerTasks     = findViewById(R.id.recyclerTasks);
        tvTaskCount       = findViewById(R.id.tvTaskCount);
        tvEmpty           = findViewById(R.id.tvEmpty);
        btnDeleteAll      = findViewById(R.id.btnDeleteAll);
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        // Adapter no longer needs a List passed in — it gets data via submitList()
        adapter = new TodoAdapter(this, this);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setAdapter(adapter);
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners() {
        btnSetAlarm.setOnClickListener(v -> showDatePicker());
        btnCancelAlarm.setOnClickListener(v -> clearAlarmSelection());
        btnSaveTask.setOnClickListener(v -> saveTask());

        btnDeleteAll.setOnClickListener(v -> {
            List<TodoEntity> current = viewModel.allTodos.getValue();
            int count = current != null ? current.size() : 0;
            if (count == 0) return;
            new AlertDialog.Builder(this)
                    .setTitle("Delete All Tasks")
                    .setMessage("Are you sure you want to delete all " + count + " tasks?")
                    .setPositiveButton("Delete All", (dialog, which) -> {
                        // Cancel all alarms first
                        if (current != null) {
                            for (TodoEntity t : current) {
                                if (t.alarmTimeMillis > 0)
                                    AlarmHelper.cancelAlarm(this, t.id.hashCode());
                            }
                        }
                        viewModel.deleteAllTodos();
                        Toast.makeText(this, "All tasks deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        viewModel.allTodos.observe(this, todos -> {
            // This fires immediately on launch (loads saved data) and after every
            // insert / update / delete — the RecyclerView always stays in sync.
            adapter.submitList(todos);
            updateUI(todos);
        });

        // Pull latest from Firestore on open (merges with local Room data)
        viewModel.sync();
    }

    // ── Save Task (writes to Room → syncs to Firebase) ────────────────────────

    private void saveTask() {
        String title = etTaskTitle.getText() != null
                ? etTaskTitle.getText().toString().trim() : "";
        String desc  = etTaskDescription.getText() != null
                ? etTaskDescription.getText().toString().trim() : "";

        if (title.isEmpty()) {
            etTaskTitle.setError("Please enter a task title");
            etTaskTitle.requestFocus();
            return;
        }

        long alarmMs = selectedAlarmCalendar != null
                ? selectedAlarmCalendar.getTimeInMillis() : 0;

        // addTodo() writes to Room on a background thread and returns the UUID
        String taskId = viewModel.addTodo(title, desc, alarmMs);

        // Schedule alarm using hashCode of UUID as the int request code
        if (alarmMs > 0) {
            AlarmHelper.setAlarm(this, taskId.hashCode(), title, alarmMs);
        }

        // Save completion state key for AlarmReceiver
        getSharedPreferences("todo_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("task_completed_" + taskId.hashCode(), false)
                .apply();

        Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
        resetForm();
        // No need to manually add to list — LiveData observer fires automatically
    }

    // ── Completion Toggle ─────────────────────────────────────────────────────

    @Override
    public void onCompletionToggled(TodoEntity task, boolean isCompleted) {
        // Update Room (background thread) → LiveData fires → adapter refreshes
        viewModel.setCompleted(task, isCompleted);

        // Update SharedPreferences so AlarmReceiver can check completion
        getSharedPreferences("todo_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("task_completed_" + task.id.hashCode(), isCompleted)
                .apply();

        if (isCompleted) {
            if (task.alarmTimeMillis > 0)
                AlarmHelper.cancelAlarm(this, task.id.hashCode());
            Toast.makeText(this, "\"" + task.title + "\" completed! 🎉",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Re-opened — restore alarm if still in the future
            if (task.alarmTimeMillis > 0
                    && task.alarmTimeMillis > System.currentTimeMillis()) {
                AlarmHelper.setAlarm(this, task.id.hashCode(),
                        task.title, task.alarmTimeMillis);
                Toast.makeText(this, "Task re-opened. Alarm restored.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── Delete Single Task ────────────────────────────────────────────────────

    @Override
    public void onDeleteTask(TodoEntity task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Delete \"" + task.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (task.alarmTimeMillis > 0)
                        AlarmHelper.cancelAlarm(this, task.id.hashCode());
                    getSharedPreferences("todo_prefs", MODE_PRIVATE)
                            .edit().remove("task_completed_" + task.id.hashCode()).apply();
                    viewModel.deleteTodo(task);
                    // LiveData fires automatically — no notifyItemRemoved needed
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Date / Time Pickers ───────────────────────────────────────────────────

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this,
                (v, year, month, day) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, day);
                    showTimePicker(picked);
                },
                now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMinDate(now.getTimeInMillis());
        dlg.setTitle("Select Alarm Date");
        dlg.show();
    }

    private void showTimePicker(Calendar dateCalendar) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dlg = new TimePickerDialog(this,
                (v, hour, minute) -> {
                    dateCalendar.set(Calendar.HOUR_OF_DAY, hour);
                    dateCalendar.set(Calendar.MINUTE, minute);
                    dateCalendar.set(Calendar.SECOND, 0);
                    dateCalendar.set(Calendar.MILLISECOND, 0);

                    if (dateCalendar.getTimeInMillis() <= now.getTimeInMillis()) {
                        Toast.makeText(this, "Please select a future time",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedAlarmCalendar = dateCalendar;
                    SimpleDateFormat sdf =
                            new SimpleDateFormat("EEE, dd MMM  hh:mm a", Locale.getDefault());
                    tvAlarmTime.setText("⏰ " + sdf.format(selectedAlarmCalendar.getTime()));
                    tvAlarmTime.setTextColor(0xFF4C6EF5);
                    btnCancelAlarm.setVisibility(View.VISIBLE);
                },
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
        dlg.setTitle("Select Alarm Time");
        dlg.show();
    }

    private void clearAlarmSelection() {
        selectedAlarmCalendar = null;
        tvAlarmTime.setText("No alarm");
        tvAlarmTime.setTextColor(0xFF888888);
        btnCancelAlarm.setVisibility(View.GONE);
    }

    // ── UI state ──────────────────────────────────────────────────────────────

    private void updateUI(List<TodoEntity> todos) {
        int total   = todos != null ? todos.size() : 0;
        long pending = todos != null
                ? todos.stream().filter(t -> !t.isCompleted).count() : 0;

        tvTaskCount.setText(total + " task" + (total == 1 ? "" : "s")
                + "  •  " + pending + " pending");

        boolean hasTasks = total > 0;
        recyclerTasks.setVisibility(hasTasks ? View.VISIBLE : View.GONE);
        tvEmpty.setVisibility(hasTasks ? View.GONE : View.VISIBLE);
        btnDeleteAll.setVisibility(hasTasks ? View.VISIBLE : View.GONE);
    }

    private void resetForm() {
        etTaskTitle.setText("");
        etTaskDescription.setText("");
        clearAlarmSelection();
    }

    // ── Permissions ───────────────────────────────────────────────────────────

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.os.PowerManager pm =
                    (android.os.PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                android.content.Intent intent = new android.content.Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
}