package com.example.lifesync.activities.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;
import com.example.lifesync.activities.models.TodoEntity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onCompletionToggled(TodoEntity task, boolean isCompleted);
        void onDeleteTask(TodoEntity task);
    }

    private final Context              context;
    private       List<TodoEntity>     taskList = new ArrayList<>();
    private final OnTaskActionListener listener;
    private final SimpleDateFormat     sdf =
            new SimpleDateFormat("EEE, dd MMM  hh:mm a", Locale.getDefault());

    public TodoAdapter(Context context, OnTaskActionListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    /** Called from Activity whenever LiveData delivers a new list */
    public void submitList(List<TodoEntity> newList) {
        this.taskList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder h, int position) {
        TodoEntity task = taskList.get(position);

        // ── Title ─────────────────────────────────────────────────────────────
        h.tvTitle.setText(task.title);

        // ── Description ───────────────────────────────────────────────────────
        if (task.description != null && !task.description.isEmpty()) {
            h.tvDescription.setVisibility(View.VISIBLE);
            h.tvDescription.setText(task.description);
        } else {
            h.tvDescription.setVisibility(View.GONE);
        }

        // ── Alarm time ────────────────────────────────────────────────────────
        if (task.alarmTimeMillis > 0) {
            h.layoutAlarmRow.setVisibility(View.VISIBLE);
            h.tvAlarmTime.setText(sdf.format(new Date(task.alarmTimeMillis)));
        } else {
            h.layoutAlarmRow.setVisibility(View.GONE);
        }

        // ── Completion style ──────────────────────────────────────────────────
        applyCompletionStyle(h, task.isCompleted);

        // Detach before setting to avoid triggering listener during bind
        h.cbCompleted.setOnCheckedChangeListener(null);
        h.cbCompleted.setChecked(task.isCompleted);
        h.cbCompleted.setOnCheckedChangeListener((btn, checked) -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onCompletionToggled(taskList.get(pos), checked);
        });

        // Mark Done button
        h.btnMarkDone.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) {
                TodoEntity t = taskList.get(pos);
                listener.onCompletionToggled(t, !t.isCompleted);
            }
        });

        // Delete button
        h.btnDeleteTask.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onDeleteTask(taskList.get(pos));
        });
    }

    private void applyCompletionStyle(TaskViewHolder h, boolean completed) {
        if (completed) {
            h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvTitle.setTextColor(0xFFAAAAAA);
            h.tvDescription.setTextColor(0xFFBBBBBB);
            h.tvAlarmTime.setTextColor(0xFFBBBBBB);
            h.btnMarkDone.setText("✓ Completed");
            h.btnMarkDone.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50));
            h.btnMarkDone.setEnabled(false);
            h.cardTask.setCardBackgroundColor(0xFFF9F9F9);
        } else {
            h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvTitle.setTextColor(0xFF1A1A2E);
            h.tvDescription.setTextColor(0xFF666666);
            h.tvAlarmTime.setTextColor(0xFF4C6EF5);
            h.btnMarkDone.setText("Mark as Complete");
            h.btnMarkDone.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4C6EF5));
            h.btnMarkDone.setEnabled(true);
            h.cardTask.setCardBackgroundColor(0xFFFFFFFF);
        }
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView       cardTask;
        CheckBox       cbCompleted;
        TextView       tvTitle, tvDescription, tvAlarmTime;
        View           layoutAlarmRow;
        MaterialButton btnMarkDone;
        ImageButton    btnDeleteTask;

        TaskViewHolder(@NonNull View v) {
            super(v);
            cardTask       = v.findViewById(R.id.cardTask);
            cbCompleted    = v.findViewById(R.id.cbCompleted);
            tvTitle        = v.findViewById(R.id.tvTitle);
            tvDescription  = v.findViewById(R.id.tvDescription);
            tvAlarmTime    = v.findViewById(R.id.tvAlarmTime);
            layoutAlarmRow = v.findViewById(R.id.layoutAlarmRow);
            btnMarkDone    = v.findViewById(R.id.btnMarkDone);
            btnDeleteTask  = v.findViewById(R.id.btnDeleteTask);
        }
    }
}