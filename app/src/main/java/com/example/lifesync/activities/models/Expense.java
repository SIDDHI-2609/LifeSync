package com.example.lifesync.activities.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public Double amount;
    public Long date;
    public String category;

}
