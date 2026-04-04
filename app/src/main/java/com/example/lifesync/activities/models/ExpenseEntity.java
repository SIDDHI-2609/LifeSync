package com.example.lifesync.activities.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room entity for the Expense Tracker module.
 */
@Entity(tableName = "expenses")
public class ExpenseEntity {

    @PrimaryKey
    @NonNull
    public String id;           // Firestore document ID

    public String userId;       // Firebase Auth UID
    public String title;        // e.g. "Lunch", "Rent"
    public String category;     // e.g. "Food", "Transport", "Bills", "Other"
    public double amount;       // in user's currency
    public String note;         // optional extra info
    public long   date;         // epoch millis of when expense occurred
    public long   createdAt;
    public boolean isSynced;

    public ExpenseEntity() {}

    public ExpenseEntity(@NonNull String id, String userId, String title,
                         String category, double amount, String note, long date) {
        this.id        = id;
        this.userId    = userId;
        this.title     = title;
        this.category  = category;
        this.amount    = amount;
        this.note      = note;
        this.date      = date;
        this.createdAt = System.currentTimeMillis();
        this.isSynced  = false;
    }
}
