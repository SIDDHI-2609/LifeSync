package com.example.lifesync.activities.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

    // ── Form views ────────────────────────────────────────────────────────────
    private TextInputEditText etTaskTitle, etTaskDescription;
    private TextView tvAlarmTime, tvTaskCount, tvEmpty;
    private MaterialButton btnSetAlarm, btnSaveTask;
    private ImageButton btnCancelAlarm;

    // ── List views ────────────────────────────────────────────────────────────
    private RecyclerView recyclerTasks;
    private TodoAdapter adapter;
    private final List<TodoItem> taskList = new ArrayList<>();

    // ── State ─────────────────────────────────────────────────────────────────
    private Calendar selectedAlarmCalendar = null;
    private int nextTaskId = 1;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        initViews();
        setupRecyclerView();
        setupListeners();
        requestNotificationPermission();
        requestBatteryOptimizationExemption();
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    private void initViews() {
        etTaskTitle      = findViewById(R.id.etTaskTitle);
        etTaskDescription= findViewById(R.id.etTaskDescription);
        tvAlarmTime      = findViewById(R.id.tvAlarmTime);
        btnSetAlarm      = findViewById(R.id.btnSetAlarm);
        btnCancelAlarm   = findViewById(R.id.btnCancelAlarm);
        btnSaveTask      = findViewById(R.id.btnSaveTask);
        recyclerTasks    = findViewById(R.id.recyclerTasks);
        tvTaskCount      = findViewById(R.id.tvTaskCount);
        tvEmpty          = findViewById(R.id.tvEmpty);
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
    }

    // ── Date / Time Pickers ───────────────────────────────────────────────────

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, day);
                    showTimePicker(picked);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMinDate(now.getTimeInMillis());
        dlg.setTitle("Select Alarm Date");
        dlg.show();
    }

    private void showTimePicker(Calendar dateCalendar) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dlg = new TimePickerDialog(this,
                (view, hour, minute) -> {
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

                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "EEE, dd MMM  hh:mm a", Locale.getDefault());
                    tvAlarmTime.setText("⏰ " + sdf.format(selectedAlarmCalendar.getTime()));
                    tvAlarmTime.setTextColor(0xFF4C6EF5);
                    btnCancelAlarm.setVisibility(View.VISIBLE);
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false);
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
        taskList.add(0, task); // Add to top of list
        adapter.notifyItemInserted(0);
        recyclerTasks.scrollToPosition(0);

        // Schedule alarm if set
        if (alarmMs > 0) {
            AlarmHelper.setAlarm(this, nextTaskId, title, alarmMs);
        }

        nextTaskId++;
        updateListVisibility();
        resetForm();
    }

    // ── Completion Toggle (from adapter callback) ──────────────────────────────

    @Override
    public void onCompletionToggled(int position, boolean isCompleted) {
        TodoItem task = taskList.get(position);
        task.setCompleted(isCompleted);
        adapter.notifyItemChanged(position);

        if (isCompleted) {
            // ✅ Task done → cancel the alarm so it won't ring
            if (task.hasAlarm()) {
                AlarmHelper.cancelAlarm(this, task.getId());
            }
            Toast.makeText(this, "\"" + task.getTitle() + "\" completed! 🎉",
                    Toast.LENGTH_SHORT).show();
        } else {
            // ↩ Task un-completed → re-schedule alarm if time is still in future
            if (task.hasAlarm()
                    && task.getAlarmTimeMillis() > System.currentTimeMillis()) {
                AlarmHelper.setAlarm(this,
                        task.getId(),
                        task.getTitle(),
                        task.getAlarmTimeMillis());
                Toast.makeText(this, "Task re-opened. Alarm restored.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        updateListVisibility();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void resetForm() {
        etTaskTitle.setText("");
        etTaskDescription.setText("");
        clearAlarmSelection();
    }

    private void updateListVisibility() {
        long pending = taskList.stream().filter(t -> !t.isCompleted()).count();
        tvTaskCount.setText(taskList.size() + " task" + (taskList.size() == 1 ? "" : "s")
                + "  •  " + pending + " pending");

        if (taskList.isEmpty()) {
            recyclerTasks.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerTasks.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String pkg = getPackageName();
            if (pm != null && !pm.isIgnoringBatteryOptimizations(pkg)) {
                Intent intent = new Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + pkg));
                startActivity(intent);
            }
        }
    }
}