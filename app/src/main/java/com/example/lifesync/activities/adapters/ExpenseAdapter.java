package com.example.lifesync.activities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;
import com.example.lifesync.activities.models.Expense;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {
    List<Expense> expenses;
    OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenses, OnDeleteClickListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, amount;
        Button btnDelete;

        public ViewHolder (View view) {
            super(view);
            title = view.findViewById(R.id.tvTitle);
            amount = view.findViewById(R.id.tvAmount);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.title.setText(expense.title);
        holder.amount.setText("₹" + expense.amount);
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(expense);
            }
        });
    }
    @Override
    public int getItemCount() {
        return expenses.size();
    }
}


