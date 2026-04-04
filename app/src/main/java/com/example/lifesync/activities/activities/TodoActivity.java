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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.TodoAdapter;
import com.example.lifesync.activities.helper.AlarmHelper;
import com.example.lifesync.activities.models.TodoItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TodoActivity extends AppCompatActivity
        implements TodoAdapter.OnTaskActionListener {

    private TextInputEditText etTaskTitle, etTaskDescription;
    private TextView          tvAlarmTime, tvTaskCount, tvEmpty;
    private MaterialButton    btnSetAlarm, btnSaveTask, btnDeleteAll;
    private ImageButton       btnCancelAlarm;
    private RecyclerView      recyclerTasks;
    private TodoAdapter       adapter;
    private final List<TodoItem> taskList = new ArrayList<>();

    private Calendar selectedAlarmCalendar = null;
    private int      nextTaskId = 1;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        initViews();
        setupRecyclerView();
        setupListeners();
        requestPermissions();
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    private void initViews() {
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

    private void setupRecyclerView() {
        adapter = new TodoAdapter(this, taskList, this);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSetAlarm.setOnClickListener(v -> showDatePicker());
        btnCancelAlarm.setOnClickListener(v -> clearAlarmSelection());
        btnSaveTask.setOnClickListener(v -> saveTask());

        // ── Delete All: show confirmation dialog ──────────────────────────────
        btnDeleteAll.setOnClickListener(v -> {
            if (taskList.isEmpty()) return;
            new AlertDialog.Builder(this)
                    .setTitle("Delete All Tasks")
                    .setMessage("Are you sure you want to delete all " + taskList.size() + " tasks?")
                    .setPositiveButton("Delete All", (dialog, which) -> deleteAllTasks())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // ── Permissions ───────────────────────────────────────────────────────────

    private void requestPermissions() {
        // Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Battery optimization exemption (pops up system dialog)
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

    // ── Date / Time Pickers ───────────────────────────────────────────────────

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this,
                (v, year, month, day) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, day);
                    showTimePicker(picked);
                },
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
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

    // ── Save Task ─────────────────────────────────────────────────────────────

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

        TodoItem task = new TodoItem(nextTaskId, title, desc, alarmMs);
        taskList.add(0, task);
        adapter.notifyItemInserted(0);
        recyclerTasks.scrollToPosition(0);

        if (alarmMs > 0) AlarmHelper.setAlarm(this, nextTaskId, title, alarmMs);

        nextTaskId++;
        updateUI();
        resetForm();
    }

    // ── Completion Toggle ─────────────────────────────────────────────────────

    @Override
    public void onCompletionToggled(int position, boolean isCompleted) {
        TodoItem task = taskList.get(position);
        task.setCompleted(isCompleted);
        adapter.notifyItemChanged(position);

        // Save completion state so AlarmReceiver can check it even when app is killed
        getSharedPreferences("todo_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("task_completed_" + task.getId(), isCompleted)
                .apply();

        if (isCompleted) {
            if (task.hasAlarm()) AlarmHelper.cancelAlarm(this, task.getId());
            Toast.makeText(this, "\"" + task.getTitle() + "\" completed! 🎉",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Re-open: restore alarm if still in the future
            if (task.hasAlarm() && task.getAlarmTimeMillis() > System.currentTimeMillis()) {
                AlarmHelper.setAlarm(this, task.getId(), task.getTitle(),
                        task.getAlarmTimeMillis());
                Toast.makeText(this, "Task re-opened. Alarm restored.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        updateUI();
    }

    // ── Delete Single Task ────────────────────────────────────────────────────

    public void onDeleteTask(int position) {
        TodoItem task = taskList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Cancel alarm if pending
                    if (task.hasAlarm()) AlarmHelper.cancelAlarm(this, task.getId());
                    // Remove completion pref
                    getSharedPreferences("todo_prefs", MODE_PRIVATE)
                            .edit().remove("task_completed_" + task.getId()).apply();
                    taskList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateUI();
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Delete All Tasks ──────────────────────────────────────────────────────

    private void deleteAllTasks() {
        // Cancel every alarm
        for (TodoItem task : taskList) {
            if (task.hasAlarm()) AlarmHelper.cancelAlarm(this, task.getId());
            getSharedPreferences("todo_prefs", MODE_PRIVATE)
                    .edit().remove("task_completed_" + task.getId()).apply();
        }
        taskList.clear();
        adapter.notifyDataSetChanged();
        updateUI();
        Toast.makeText(this, "All tasks deleted", Toast.LENGTH_SHORT).show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void resetForm() {
        etTaskTitle.setText("");
        etTaskDescription.setText("");
        clearAlarmSelection();
    }

    private void updateUI() {
        long pending = taskList.stream().filter(t -> !t.isCompleted()).count();
        tvTaskCount.setText(taskList.size() + " task" + (taskList.size() == 1 ? "" : "s")
                + "  •  " + pending + " pending");

        boolean hasTasks = !taskList.isEmpty();
        recyclerTasks.setVisibility(hasTasks ? View.VISIBLE : View.GONE);
        tvEmpty.setVisibility(hasTasks ? View.GONE : View.VISIBLE);
        btnDeleteAll.setVisibility(hasTasks ? View.VISIBLE : View.GONE);
    }
}