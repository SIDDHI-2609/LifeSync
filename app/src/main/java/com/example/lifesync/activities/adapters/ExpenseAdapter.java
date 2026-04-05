package com.example.lifesync.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.lifesync.R;
import com.example.lifesync.activities.models.ExpenseEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseActionListener {
        void onDelete(ExpenseEntity expense);
        void onEdit(ExpenseEntity expense);
    }

    private final Context                context;
    private       List<ExpenseEntity>    list = new ArrayList<>();
    private final OnExpenseActionListener listener;
    private final SimpleDateFormat        sdf =
            new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());

    public ExpenseAdapter(Context context, OnExpenseActionListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void submitList(List<ExpenseEntity> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder h, int position) {
        ExpenseEntity e = list.get(position);

        // Category icon
        h.tvCategoryIcon.setText(emojiFor(e.category));
        h.tvCategoryIcon.setBackgroundResource(bgFor(e.category));

        h.tvExpenseTitle.setText(e.title != null ? e.title : "Expense");
        h.tvCategory.setText(e.category != null ? e.category : "Other");
        h.tvAmount.setText(String.format(Locale.getDefault(), "- ₹%.2f", e.amount));
        h.tvExpenseDate.setText(sdf.format(new Date(e.date)));

        if (e.note != null && !e.note.isEmpty()) {
            h.tvExpenseNote.setVisibility(View.VISIBLE);
            h.tvExpenseNote.setText(e.note);
        } else {
            h.tvExpenseNote.setVisibility(View.GONE);
        }

        h.btnDelete.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onDelete(list.get(pos));
        });

        h.cardExpense.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onEdit(list.get(pos));
        });
    }

    @Override public int getItemCount() { return list.size(); }

    // ── Category helpers ──────────────────────────────────────────────────────

    private String emojiFor(String category) {
        if (category == null) return "📦";
        switch (category) {
            case "Food":      return "🍔";
            case "Transport": return "🚗";
            case "Bills":     return "🧾";
            case "Shopping":  return "🛍";
            case "Health":    return "💊";
            default:          return "📦";
        }
    }

    private int bgFor(String category) {
        if (category == null) return R.drawable.badge_bg_blue;
        switch (category) {
            case "Food":      return R.drawable.badge_bg_yellow;
            case "Transport": return R.drawable.badge_bg_blue;
            case "Bills":     return R.drawable.badge_bg_green;
            case "Shopping":  return R.drawable.badge_bg_blue;
            case "Health":    return R.drawable.badge_bg_green;
            default:          return R.drawable.badge_bg_blue;
        }
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        CardView    cardExpense;
        TextView    tvCategoryIcon, tvExpenseTitle, tvCategory,
                tvExpenseDate, tvExpenseNote, tvAmount;
        ImageButton btnDelete;

        ExpenseViewHolder(@NonNull View v) {
            super(v);
            cardExpense     = v.findViewById(R.id.cardExpense);
            tvCategoryIcon  = v.findViewById(R.id.tvCategoryIcon);
            tvExpenseTitle  = v.findViewById(R.id.tvExpenseTitle);
            tvCategory      = v.findViewById(R.id.tvCategory);
            tvExpenseDate   = v.findViewById(R.id.tvExpenseDate);
            tvExpenseNote   = v.findViewById(R.id.tvExpenseNote);
            tvAmount        = v.findViewById(R.id.tvAmount);
            btnDelete       = v.findViewById(R.id.btnDelete);
        }
    }
}


