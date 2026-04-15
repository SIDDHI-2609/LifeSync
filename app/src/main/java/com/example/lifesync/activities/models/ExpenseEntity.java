package com.example.lifesync.activities.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room entity for the Expense Tracker module.
 * Category field is kept for backward compatibility but is now optional/unused.
 */
@Entity(tableName = "expenses")
public class ExpenseEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String  userId;
    public String  title;        // e.g. "Lunch", "Rent", "Gym" — used for pie chart grouping
    public String  category;     // kept for backward compatibility, defaults to "General"
    public double  amount;
    public String  note;
    public long    date;         // epoch millis
    public long    createdAt;
    public boolean isSynced;

    public ExpenseEntity() {}

    public ExpenseEntity(@NonNull String id, String userId, String title,
                         String category, double amount, String note, long date) {
        this.id        = id;
        this.userId    = userId;
        this.title     = title;
        this.category  = category;   // will default to "General"
        this.amount    = amount;
        this.note      = note;
        this.date      = date;
        this.createdAt = System.currentTimeMillis();
        this.isSynced  = false;
    }
}