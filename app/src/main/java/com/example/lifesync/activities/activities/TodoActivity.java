package com.example.lifesync.activities.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.TodoAdapter;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.models.Todo;

import java.util.Calendar;
import java.util.List;

import com.example.lifesync.activities.receivers.AlarmReceiver;

public class TodoActivity extends AppCompatActivity {
AppDatabase db;
RecyclerView recyclerView;
long selectedTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "lifesync-db").allowMainThreadQueries().build();
        EditText etTask = findViewById(R.id.etTask);
        Button btnTime = findViewById(R.id.btnTime);
        Button btnAdd = findViewById(R.id.btnAdd);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadTodos();

        //Time picker
        btnTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            new TimePickerDialog(this, (view, hour, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                selectedTime = cal.getTimeInMillis();
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });
        //Add task

        btnAdd.setOnClickListener(v -> {

            String task = etTask.getText().toString().trim();

            if (task.isEmpty()) {
                Toast.makeText(this, "Enter task", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTime == 0) {
                Toast.makeText(this, "Select time first", Toast.LENGTH_SHORT).show();
                return;
            }

            Todo todo = new Todo();
            todo.title = task;
            todo.time = selectedTime;
            todo.isDone = false;

            db.todoDao().insert(todo);

            try {
                Intent intent = new Intent(this, AlarmReceiver.class);
                intent.putExtra("title", todo.title);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        (int) System.currentTimeMillis(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager =
                        (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        todo.time,
                        pendingIntent
                );

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }

            etTask.setText("");
            selectedTime = 0;
            loadTodos();
        });

        }
        private void loadTodos() {
        List<Todo> todos = db.todoDao().getAllTodos();
        TodoAdapter adapter = new TodoAdapter(todos, this::deleteTodo, this::updateTodo);
        recyclerView.setAdapter(adapter);

    }
    private void deleteTodo(Todo todo) {
        db.todoDao().delete(todo);
        loadTodos();
    }
    private void updateTodo(Todo todo, boolean isChecked){
        todo.isDone = isChecked;
        db.todoDao().update(todo);
        loadTodos();

    }
}