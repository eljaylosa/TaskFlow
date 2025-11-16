package com.example.prototype.model;

import java.util.Objects;

public class Hobby {
    private int id;
    private String name;
    private int streak;
    private String lastCheckedIn;
    private String weekStartDate; // New field for weekly summary
    private boolean isChecked; // For HobbyActivity selection

    // New full constructor
    public Hobby(int id, String name, int streak, String lastCheckedIn, String weekStartDate) {
        this.id = id;
        this.name = name;
        this.streak = streak;
        this.lastCheckedIn = lastCheckedIn;
        this.weekStartDate = weekStartDate;
        this.isChecked = false; // Default to not checked
    }

    // Keep old constructor for compatibility
    public Hobby(int id, String name, int streak, String lastCheckedIn) {
        this(id, name, streak, lastCheckedIn, null); // Delegate to new constructor
    }

    // Simple constructor for creating a new hobby
    public Hobby(int id, String name) {
        this(id, name, 0, "", null);
    }


    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStreak() {
        return streak;
    }

    public String getLastCheckedIn() {
        return lastCheckedIn;
    }

    public String getWeekStartDate() {
        return weekStartDate;
    }

    public boolean isChecked() {
        return isChecked;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void setLastCheckedIn(String lastCheckedIn) {
        this.lastCheckedIn = lastCheckedIn;
    }

    public void setWeekStartDate(String weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hobby hobby = (Hobby) o;
        return id == hobby.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
