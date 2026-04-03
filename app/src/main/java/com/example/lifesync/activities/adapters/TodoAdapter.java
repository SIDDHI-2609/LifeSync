package com.example.lifesync.activities.adapters;


import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
        /** Called when the user taps "Mark as Complete" or unchecks the checkbox */
        void onCompletionToggled(int position, boolean isCompleted);
    }

    private final Context context;
    private final List<TodoItem> taskList;
    private final OnTaskActionListener listener;

    public TodoAdapter(Context context, List<TodoItem> taskList, OnTaskActionListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TodoItem task = taskList.get(position);

        // ── Title ─────────────────────────────────────────────────────────────
        holder.tvTitle.setText(task.getTitle());

        // ── Description ───────────────────────────────────────────────────────
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(task.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // ── Alarm time ────────────────────────────────────────────────────────
        if (task.hasAlarm()) {
            holder.layoutAlarmRow.setVisibility(View.VISIBLE);
            holder.tvAlarmTime.setText(task.getFormattedAlarmTime());
        } else {
            holder.layoutAlarmRow.setVisibility(View.GONE);
        }

        // ── Completion state ──────────────────────────────────────────────────
        applyCompletionStyle(holder, task.isCompleted());

        // Prevent checkbox listener firing during bind
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());

        // ── Listeners ─────────────────────────────────────────────────────────

        // Checkbox toggles completion
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_ID) return;
            listener.onCompletionToggled(adapterPos, isChecked);
        });

        // Button also toggles completion (only active when not completed)
        holder.btnMarkDone.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_ID) return;
            boolean newState = !taskList.get(adapterPos).isCompleted();
            listener.onCompletionToggled(adapterPos, newState);
        });
    }

    /** Applies visual styling based on completion state */
    private void applyCompletionStyle(TaskViewHolder holder, boolean completed) {
        if (completed) {
            // Strike-through title
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(0xFFAAAAAA);

            // Dim description & alarm
            holder.tvDescription.setTextColor(0xFFBBBBBB);
            holder.tvAlarmTime.setTextColor(0xFFBBBBBB);

            // Update button
            holder.btnMarkDone.setText("✓ Completed");
            holder.btnMarkDone.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // green
            holder.btnMarkDone.setEnabled(false);

            // Dim the card slightly
            holder.cardTask.setCardBackgroundColor(0xFFF9F9F9);
        } else {
            // Remove strike-through
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(0xFF1A1A2E);

            holder.tvDescription.setTextColor(0xFF666666);
            holder.tvAlarmTime.setTextColor(0xFF4C6EF5);

            holder.btnMarkDone.setText("Mark as Complete");
            holder.btnMarkDone.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4C6EF5)); // blue
            holder.btnMarkDone.setEnabled(true);

            holder.cardTask.setCardBackgroundColor(0xFFFFFFFF);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardTask;
        CheckBox cbCompleted;
        TextView tvTitle, tvDescription, tvAlarmTime;
        View layoutAlarmRow;
        MaterialButton btnMarkDone;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask       = itemView.findViewById(R.id.cardTask);
            cbCompleted    = itemView.findViewById(R.id.cbCompleted);
            tvTitle        = itemView.findViewById(R.id.tvTitle);
            tvDescription  = itemView.findViewById(R.id.tvDescription);
            tvAlarmTime    = itemView.findViewById(R.id.tvAlarmTime);
            layoutAlarmRow = itemView.findViewById(R.id.layoutAlarmRow);
            btnMarkDone    = itemView.findViewById(R.id.btnMarkDone);
        }
    }
}