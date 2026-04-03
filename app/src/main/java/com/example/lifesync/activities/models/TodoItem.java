package com.example.lifesync.activities.models;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class TodoItem {
    private int id;
    private String title;
    private String description;
    private boolean isCompleted;
    private long alarmTimeMillis; // 0 = no alarm

    public TodoItem() {}

    public TodoItem(int id, String title, String description, long alarmTimeMillis) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = false;
        this.alarmTimeMillis = alarmTimeMillis;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public long getAlarmTimeMillis() { return alarmTimeMillis; }
    public void setAlarmTimeMillis(long alarmTimeMillis) { this.alarmTimeMillis = alarmTimeMillis; }

    public boolean hasAlarm() { return alarmTimeMillis > 0; }

    public String getFormattedAlarmTime() {
        if (!hasAlarm()) return "No alarm";
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy  hh:mm a", Locale.getDefault());
        return sdf.format(new Date(alarmTimeMillis));
    }
}