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
import com.example.lifesync.activities.models.TodoItem;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onCompletionToggled(int position, boolean isCompleted);
        void onDeleteTask(int position);   // NEW
    }

    private final Context             context;
    private final List<TodoItem>      taskList;
    private final OnTaskActionListener listener;

    public TodoAdapter(Context context, List<TodoItem> taskList, OnTaskActionListener listener) {
        this.context  = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder h, int position) {
        TodoItem task = taskList.get(position);

        // Title
        h.tvTitle.setText(task.getTitle());

        // Description
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            h.tvDescription.setVisibility(View.VISIBLE);
            h.tvDescription.setText(task.getDescription());
        } else {
            h.tvDescription.setVisibility(View.GONE);
        }

        // Alarm time
        if (task.hasAlarm()) {
            h.layoutAlarmRow.setVisibility(View.VISIBLE);
            h.tvAlarmTime.setText(task.getFormattedAlarmTime());
        } else {
            h.layoutAlarmRow.setVisibility(View.GONE);
        }

        // Completion style
        applyCompletionStyle(h, task.isCompleted());

        // Checkbox — detach listener before setting value to avoid recursive calls
        h.cbCompleted.setOnCheckedChangeListener(null);
        h.cbCompleted.setChecked(task.isCompleted());
        h.cbCompleted.setOnCheckedChangeListener((btn, checked) -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onCompletionToggled(pos, checked);
        });

        // Mark Done button
        h.btnMarkDone.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID)
                listener.onCompletionToggled(pos, !taskList.get(pos).isCompleted());
        });

        // Delete button (individual task) — task stays until user taps this
        h.btnDeleteTask.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onDeleteTask(pos);
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

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView     cardTask;
        CheckBox     cbCompleted;
        TextView     tvTitle, tvDescription, tvAlarmTime;
        View         layoutAlarmRow;
        MaterialButton btnMarkDone;
        ImageButton  btnDeleteTask;   // NEW

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