package com.example.prototype;

public class Task {
    private String id;
    private String title;
    private String description;
    private String dueDate;
    private String time; // New field for time
    private String category;
    private boolean completed;

    // Updated constructor to include time
    public Task(String id, String title, String description, String dueDate, String time, String category, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.time = time;
        this.category = category;
        this.completed = completed;
    }

    // Getters for all fields
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDate() {
        return dueDate;
    }

    public String getTime() {
        return time; // Getter for time
    }

    public String getCategory() {
        return category;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
