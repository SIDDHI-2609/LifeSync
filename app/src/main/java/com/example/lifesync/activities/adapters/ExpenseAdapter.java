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

    private final Context                 context;
    private       List<ExpenseEntity>     list = new ArrayList<>();
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

        // Title icon — first letter or emoji based on first char
        String displayTitle = (e.title != null && !e.title.isEmpty()) ? e.title : "Expense";
        h.tvCategoryIcon.setText(getEmojiForTitle(displayTitle));
        h.tvCategoryIcon.setBackgroundResource(R.drawable.badge_bg_blue);

        h.tvExpenseTitle.setText(displayTitle);

        // Show the date instead of category
        h.tvCategory.setText(sdf.format(new Date(e.date)));

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
            if (pos != RecyclerView.NO_POSITION) listener.onDelete(list.get(pos));
        });

        h.cardExpense.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onEdit(list.get(pos));
        });
    }

    @Override public int getItemCount() { return list.size(); }

    /**
     * Generate a meaningful emoji based on common expense title keywords.
     * Falls back to first letter of the title.
     */
    private String getEmojiForTitle(String title) {
        if (title == null) return "💰";
        String lower = title.toLowerCase();

        if (lower.contains("food") || lower.contains("lunch") || lower.contains("dinner")
                || lower.contains("breakfast") || lower.contains("meal"))
            return "🍔";
        if (lower.contains("transport") || lower.contains("uber") || lower.contains("cab")
                || lower.contains("fuel") || lower.contains("petrol") || lower.contains("bus")
                || lower.contains("metro") || lower.contains("auto"))
            return "🚗";
        if (lower.contains("bill") || lower.contains("electricity") || lower.contains("water")
                || lower.contains("gas") || lower.contains("wifi") || lower.contains("internet")
                || lower.contains("recharge") || lower.contains("phone"))
            return "🧾";
        if (lower.contains("shop") || lower.contains("amazon") || lower.contains("flipkart")
                || lower.contains("cloth") || lower.contains("buy"))
            return "🛍";
        if (lower.contains("health") || lower.contains("medicine") || lower.contains("doctor")
                || lower.contains("hospital") || lower.contains("gym") || lower.contains("medical"))
            return "💊";
        if (lower.contains("rent") || lower.contains("emi") || lower.contains("loan"))
            return "🏠";
        if (lower.contains("movie") || lower.contains("entertainment") || lower.contains("netflix")
                || lower.contains("game"))
            return "🎬";
        if (lower.contains("grocery") || lower.contains("vegetable") || lower.contains("milk")
                || lower.contains("fruit"))
            return "🛒";
        if (lower.contains("coffee") || lower.contains("tea") || lower.contains("chai"))
            return "☕";
        if (lower.contains("education") || lower.contains("book") || lower.contains("course")
                || lower.contains("fee") || lower.contains("tuition"))
            return "📚";

        return "💰"; // default
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